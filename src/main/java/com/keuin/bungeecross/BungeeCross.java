package com.keuin.bungeecross;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.keuin.bungeecross.message.ingame.ConcreteInGameChatProcessor;
import com.keuin.bungeecross.message.ingame.InGameChatProcessor;
import com.keuin.bungeecross.message.redis.RedisConfig;
import com.keuin.bungeecross.message.relayer.InGameRelayer;
import com.keuin.bungeecross.message.relayer.RedisManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class BungeeCross extends Plugin {

    public static Logger logger = null;

    private BungeeCrossConfig config;

    private ProxyServer proxyServer;
    private InGameRelayer inGameRelayer;
    private RedisManager redisManager;
    private InGameChatProcessor inGameChatProcessor = null;

    private static final String relayMessagePrefix = "#";
    private static final String inGameCommandPrefix = "!BC";
    private static final String configurationFileName = "bungeecross.json";

    @Override
    public void onEnable() {
        // enable logger
        logger = getLogger();

        // get proxy server
        proxyServer = ProxyServer.getInstance();

        // generate default config file (skeleton)
        File configFile = new File(configurationFileName);
        if (!configFile.exists()) {
            logger.warning(String.format("Config file %s does not exist. A skeleton file will be generated. Please edit it and reload BungeeCross.", configurationFileName));
            generateDefaultConfig(configurationFileName);
            return;
        }

        // load global config
        if (!loadConfig(configurationFileName) || config == null) {
            logger.severe(String.format("Cannot read config from file: %s. BungeeCross is disabled.", configurationFileName));
            return;
        }

        // load redis config
//        RedisConfig redisConfig = new RedisConfig("121.36.38.51", 6379, "04mg0oB5$", "mc", "qq");

        // initialize relayer
        inGameRelayer = new InGameRelayer(proxyServer);
        redisManager = new RedisManager(config.getRedis(), inGameRelayer);
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
     * Load global config.
     * @return whether it succeeds.
     */
    private boolean loadConfig(String configFileName) {
        try {
            File configFile = new File(configFileName);
            if (!configFile.exists()) {
                logger.severe(String.format("Specific config file %s does not exist.", configFileName));
                return false;
            }
            Reader configReader = Files.newBufferedReader(configFile.toPath());
            config = (new Gson()).fromJson(configReader, BungeeCrossConfig.class);
        } catch (IOException e) {
            logger.severe(String.format("Failed to read config file %s: %s.", configFileName, e));
            return false;
        } catch (JsonParseException e) {
            logger.severe(String.format("Malformed config file %s: %s.", configFileName, e));
            return false;
        }
        return true;
    }

    /**
     * Generate default config file.
     * @param configurationFileName file name.
     * @return whether it succeeds.
     */
    private boolean generateDefaultConfig(String configurationFileName) {
        // if file already exists
        File file = new File(configurationFileName);
        if(file.exists())
            return false;

        // generate default config programtically
        BungeeCrossConfig defaultConfig = new BungeeCrossConfig(
                new RedisConfig("",6379,"","","")
        );
        String jsonString = (new Gson()).toJson(defaultConfig);

        // save to file
        try(BufferedOutputStream outputStream = new BufferedOutputStream(
                Files.newOutputStream(file.toPath(), CREATE_NEW, APPEND)
        )) {
            outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.severe(String.format("Failed to generate default config file %s: %s.", configurationFileName, e));
            return false;
        }

        return true;
    }

    /**
     * The body of message relay thread.
     * This thread processes the message queue, send them into the target TCP connection,
     * and put all received messages into the
     */
    private void messageRelayThread() {

    }
}
