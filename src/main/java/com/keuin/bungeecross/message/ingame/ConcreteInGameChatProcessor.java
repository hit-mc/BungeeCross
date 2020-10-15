package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.relayer.InGameRelayer;
import com.keuin.bungeecross.message.relayer.RedisManager;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ConcreteInGameChatProcessor implements InGameChatProcessor {

    private final Logger logger;

    private final String relayMessagePrefix;
    private final String inGameCommandPrefix;
    private final boolean relayCommandMessage;

    private final RedisManager redisManager;
    private final InGameRelayer inGameRelayer;

    private final MinInstructionInterpreter interpreter;
    private final ChatProcessorDispatcher dispatcher = new ChatProcessorDispatcher();

    /**
     * Construct a in-game chat message processor,
     * which handles message transforming between servers and mc-redis.
     * @param relayMessagePrefix the prefix marks that the message should be sent to Redis.
     * @param relayCommandMessage whether the command message should be relayed to other servers.
     */
    public ConcreteInGameChatProcessor(String relayMessagePrefix, String inGameCommandPrefix, InGameRelayer inGameRelayer, RedisManager redisManager, boolean relayCommandMessage, Logger logger, MinInstructionInterpreter interpreter) {
        this.relayMessagePrefix = relayMessagePrefix;
        if(relayMessagePrefix == null)
            throw new IllegalArgumentException("relayMessagePrefix must not be null.");
        this.inGameCommandPrefix = inGameCommandPrefix;
        if(inGameCommandPrefix == null)
            throw new IllegalArgumentException("inGameCommandPrefix must not be null.");
        this.relayCommandMessage = relayCommandMessage;
        this.inGameRelayer = inGameRelayer;
        if(inGameRelayer == null)
            throw new IllegalArgumentException("inGameRelayer must not be null.");
        this.redisManager = redisManager; // Connect to Redis server here.
        if(redisManager == null)
            throw new IllegalArgumentException("redisManager must not be null.");
        this.interpreter = interpreter;
        if(interpreter == null)
            throw new IllegalArgumentException("interpreter must not be null.");

        this.logger = logger;
    }

    private synchronized void process(InGameMessage message) {
        logger.info(String.format("InGameChatProcessor: processing message %s", message.toString()));
        boolean isCommand = false;
        // process as a command
        if(message.getMessage().startsWith(inGameCommandPrefix)) {
            logger.info("Process as a command");
            isCommand = true;
            String cmd = message.getMessage();
            // trim interval blankspace
            cmd = cmd.substring(inGameCommandPrefix.length() + ((cmd.length() > inGameCommandPrefix.length()) ? 1 : 0));
            BaseComponent[] echo = interpreter.execute(cmd);
            echo(message.getSender().getUUID(), echo);
        }

        if(!isCommand || relayCommandMessage) {
            logger.info("Relay to other servers.");
            // relay to other servers
            inGameRelayer.relay(message);
        }

        // relay to redis
        if(!isCommand && message.getMessage().startsWith(relayMessagePrefix)) {
            logger.info("Relay to Redis.");
            redisManager.relay(message);
        }

        logger.info("InGameChatProcessor: finish.");
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
