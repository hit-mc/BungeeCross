package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.relayer.InGameRelayer;
import com.keuin.bungeecross.message.relayer.RedisManager;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.logging.Logger;

public class ConcreteInGameChatProcessor implements InGameChatProcessor {

    private final Logger logger;

    private final String relayMessagePrefix;
    private final String inGameCommandPrefix;
    private final boolean relayCommandMessage;

    private final RedisManager redisManager;
    private final InGameRelayer inGameRelayer;

    /**
     * Construct a in-game chat message processor,
     * which handles message transforming between servers and mc-redis.
     * @param relayMessagePrefix the prefix marks that the message should be sent to Redis.
     * @param relayCommandMessage whether the command message should be relayed to other servers.
     */
    public ConcreteInGameChatProcessor(String relayMessagePrefix, String inGameCommandPrefix, InGameRelayer inGameRelayer, RedisManager redisManager, boolean relayCommandMessage, Logger logger) {
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

        this.logger = logger;
    }

    public void process(InGameMessage message) {
        logger.info(String.format("InGameChatProcessor: processing message %s", message.toString()));
        boolean isCommand = false;
        // process as a command
        if(message.getMessage().startsWith(inGameCommandPrefix)) {
            logger.info("Process as a command");
            isCommand = true;
            String echo = MinInstructionInterpreter.execute(message.getMessage().substring(inGameCommandPrefix.length()));
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
    private void echo(UUID playerUUID, String message) {
        ProxyServer proxy = ProxyServer.getInstance();
        for (ServerInfo server : proxy.getServers().values()) {
            for (ProxiedPlayer player : server.getPlayers()) {
                if(playerUUID.equals(player.getUniqueId())) {
                    player.sendMessage(new ComponentBuilder(message).color(ChatColor.UNDERLINE).create());
                    return;
                }
            }
        }
    }
}
