package com.keuin.bungeecross;

import com.keuin.bungeecross.message.ingame.ConcreteInGameChatProcessor;
import com.keuin.bungeecross.message.ingame.InGameChatProcessor;
import com.keuin.bungeecross.message.redis.RedisConfig;
import com.keuin.bungeecross.message.relayer.InGameRelayer;
import com.keuin.bungeecross.message.relayer.RedisManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import java.util.logging.Logger;

public class BungeeCross extends Plugin {

    public static Logger logger = null;

    private ProxyServer proxyServer;
    private InGameRelayer inGameRelayer;
    private RedisManager redisManager;
    private InGameChatProcessor inGameChatProcessor = null;

    private static final String relayMessagePrefix = "#";
    private static final String inGameCommandPrefix = "!BC";

    @Override
    public void onEnable() {
        // enable logger
        logger = getLogger();

        // get proxy server
        proxyServer = ProxyServer.getInstance();

        // load redis config


        // initialize relayer
        inGameRelayer = new InGameRelayer(proxyServer);
        redisManager = new RedisManager(redisConfig, inGameRelayer);
        inGameChatProcessor = new ConcreteInGameChatProcessor(relayMessagePrefix, inGameCommandPrefix, inGameRelayer, redisManager, false, logger);

        // register events
        getProxy().getPluginManager().registerListener(this, new Events(inGameChatProcessor));

        // start redis thread
        redisManager.start();

//        // Start the relay thread
//        getProxy().getScheduler().runAsync(this, this::messageRelayThread);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        logger.info("Stopping RedisManager...");
        redisManager.stop();
    }

    /**
     * The body of message relay thread.
     * This thread processes the message queue, send them into the target TCP connection,
     * and put all received messages into the
     */
    private void messageRelayThread() {

    }
}
