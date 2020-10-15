package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.repeater.InGameRepeater;
import com.keuin.bungeecross.message.repeater.RedisManager;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ConcreteInGameChatProcessor implements InGameChatProcessor {

    private final Logger logger = Logger.getLogger(ConcreteInGameChatProcessor.class.getName());

    private final String repeatMessagePrefix;
    private final String inGameCommandPrefix;
    private final boolean repeatCommandMessage;

    private final RedisManager redisManager;
    private final InGameRepeater inGameRepeater;

    private final MinInstructionInterpreter interpreter;
    private final ChatProcessorDispatcher dispatcher = new ChatProcessorDispatcher();

    /**
     * Construct a in-game chat message processor,
     * which handles message transforming between servers and mc-redis.
     * @param repeatMessagePrefix the prefix marks that the message should be sent to Redis.
     * @param repeatCommandMessage whether the command message should be repeated to other servers.
     */
    public ConcreteInGameChatProcessor(String repeatMessagePrefix, String inGameCommandPrefix, InGameRepeater inGameRepeater, RedisManager redisManager, boolean repeatCommandMessage, MinInstructionInterpreter interpreter) {
        this.repeatMessagePrefix = repeatMessagePrefix;
        if(repeatMessagePrefix == null)
            throw new IllegalArgumentException("repeatMessagePrefix must not be null.");
        this.inGameCommandPrefix = inGameCommandPrefix;
        if(inGameCommandPrefix == null)
            throw new IllegalArgumentException("inGameCommandPrefix must not be null.");
        this.repeatCommandMessage = repeatCommandMessage;
        this.inGameRepeater = inGameRepeater;
        if(inGameRepeater == null)
            throw new IllegalArgumentException("inGameRepeater must not be null.");
        this.redisManager = redisManager; // Connect to Redis server here.
        if(redisManager == null)
            throw new IllegalArgumentException("redisManager must not be null.");
        this.interpreter = interpreter;
        if(interpreter == null)
            throw new IllegalArgumentException("interpreter must not be null.");
    }

    private synchronized void process(InGameMessage message) {
        logger.info(String.format("InGameChatProcessor: processing message %s", message.toString()));
        boolean isCommand = false;
        // process as a command
        if(message.getMessage().toLowerCase().startsWith(inGameCommandPrefix.toLowerCase())) {
            logger.info("Process as a command");
            isCommand = true;
            String cmd = message.getMessage();
            // trim interval blank space
            cmd = cmd.substring(inGameCommandPrefix.length() + ((cmd.length() > inGameCommandPrefix.length()) ? 1 : 0));
            BaseComponent[] echo = interpreter.execute(cmd);
            echo(message.getSender().getUUID(), echo);
        }

        if(!isCommand || repeatCommandMessage) {
            logger.info("Repeat to other servers.");
            // repeat to other servers
            inGameRepeater.repeat(message);
        }

        // repeat to redis
        if(!isCommand
                && message.getMessage().startsWith(repeatMessagePrefix)
                && message.getMessage().length() > repeatMessagePrefix.length()
        ) {
            logger.info("Repeat to Redis.");
            redisManager.repeat(new InGameMessage(
                    message.getMessage().substring(repeatMessagePrefix.length()), // remove the heading repeat prefix
                    message.getSender()
            ));
        }
    }

    /**
     * Send text message to a player.
     * @param playerUUID the player's uuid.
     * @param message the message string.
     */
    private void echo(UUID playerUUID, BaseComponent[] message) {
        if (message == null || message.length == 0)
            return;
        ProxyServer proxy = ProxyServer.getInstance();
        for (ServerInfo server : proxy.getServers().values()) {
            for (ProxiedPlayer player : server.getPlayers()) {
                if(playerUUID.equals(player.getUniqueId())) {
                    player.sendMessage(message);
                    return;
                }
            }
        }
    }

    @Override
    public void issue(InGameMessage message) {
        // add to queue
        if(!dispatcher.isAlive()) {
            dispatcher.start();
        }
        dispatcher.issue(message);
    }

    @Override
    public void close() {
        if (dispatcher.isAlive())
            dispatcher.interrupt();
    }

    private class ChatProcessorDispatcher extends Thread {

        private final LinkedBlockingQueue<InGameMessage> messageQueue = new LinkedBlockingQueue<>();
        private final AtomicBoolean running = new AtomicBoolean(true);

        @Override
        public void run() {
            running.set(true);
            try {
                while(running.get()) {
                    InGameMessage message = messageQueue.take();
                    process(message);
                }
            } catch (InterruptedException ignored) {
                logger.info("ChatProcessorDispatcher is interrupted. Quitting.");
                return;
            }
            logger.info("ChatProcessorDispatcher is quitting.");
        }

        public void issue(InGameMessage message) {
            try {
                messageQueue.put(message);
            } catch (InterruptedException ignored) {
                logger.warning(String.format("Interrupted while issuing message %s.", message));
            }
        }
    }
}
