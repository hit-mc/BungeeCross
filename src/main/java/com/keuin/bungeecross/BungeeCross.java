package com.keuin.bungeecross;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.keuin.bungeecross.message.ingame.ConcreteInGameChatProcessor;
import com.keuin.bungeecross.message.ingame.InGameChatProcessor;
import com.keuin.bungeecross.message.redis.RedisConfig;
import com.keuin.bungeecross.message.redis.RedisManager;
import com.keuin.bungeecross.message.repeater.InGameBroadcastRepeater;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class BungeeCross extends Plugin {

    private final Logger logger = Logger.getLogger(BungeeCross.class.getName());
    public static String VERSION = "";

    private BungeeCrossConfig config;

    private ProxyServer proxyServer;
    private InGameBroadcastRepeater inGameBroadcastRepeater;
    private RedisManager redisManager;
    private InGameChatProcessor inGameChatProcessor;
    private MinInstructionInterpreter interpreter;
    private InstructionDispatcher instructionDispatcher;

    private static final String repeatMessagePrefix = "#";
    private static final String inGameCommandPrefix = "!BC";
    private static final String configurationFileName = "bungeecross.json";

    {
        try {
            // get version string
            Properties properties = new Properties();
            properties.load(this.getClass(). getClassLoader().getResourceAsStream("project.properties"));
            VERSION = properties.getProperty("version");
//            System.out.println(properties.getProperty("artifactId"));
        } catch (IOException e) {
            logger.warning("Failed to get version string: " + e);
        }
    }

    @Override
    public void onEnable() {
        // get proxy server
        proxyServer = ProxyServer.getInstance();

        // generate default config file (skeleton)
        File configFile = new File(configurationFileName);
        if (!configFile.exists()) {
            logger.warning(String.format("Config file %s does not exist. A skeleton file will be generated. Please edit it and reload BungeeCross.", configurationFileName));
            generateDefaultConfig();
            return;
        }

        // load global config
        if (!loadConfig() || config == null) {
            logger.severe(String.format("Cannot read config from file: %s. BungeeCross is disabled.", configurationFileName));
            return;
        }

        // load redis config
//        RedisConfig redisConfig = new RedisConfig("121.36.38.51", 6379, "04mg0oB5$", "mc", "qq");

        // initialize repeater
        inGameBroadcastRepeater = new InGameBroadcastRepeater(proxyServer);
        redisManager = new RedisManager(config.getRedis(), inGameBroadcastRepeater);
        interpreter = new MinInstructionInterpreter(redisManager, this);
        instructionDispatcher = new InstructionDispatcher(interpreter);
        redisManager.setInstructionDispatcher(instructionDispatcher);
        inGameChatProcessor = new ConcreteInGameChatProcessor(repeatMessagePrefix, inGameCommandPrefix, inGameBroadcastRepeater, redisManager, instructionDispatcher);

        // register events
        getProxy().getPluginManager().registerListener(this, new Events(this, inGameChatProcessor));

        // start redis thread
        redisManager.start();

//        // Start the repeat thread
//        getProxy().getScheduler().runAsync(this, this::messageRepeatThread);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        logger.info("Stopping RedisManager...");
        if (redisManager != null)
            redisManager.stop();

        logger.info("Stopping InGameChatProcessor...");
        if (inGameChatProcessor != null)
            inGameChatProcessor.close();

        logger.info("Stopping InstructionDispatcher");
        if (instructionDispatcher != null)
            instructionDispatcher.close();
    }

    /**
     * Load global config.
     * @return whether it succeeds.
     */
    private boolean loadConfig() {
        try {
            File configFile = new File(BungeeCross.configurationFileName);
            if (!configFile.exists()) {
                logger.severe(String.format("Specific config file %s does not exist.", BungeeCross.configurationFileName));
                return false;
            }
            Reader configReader = Files.newBufferedReader(configFile.toPath());
            config = (new Gson()).fromJson(configReader, BungeeCrossConfig.class);
        } catch (IOException e) {
            logger.severe(String.format("Failed to read config file %s: %s.", BungeeCross.configurationFileName, e));
            return false;
        } catch (JsonParseException e) {
            logger.severe(String.format("Malformed config file %s: %s.", BungeeCross.configurationFileName, e));
            return false;
        }
        return true;
    }

    /**
     * Generate default config file.
     */
    private void generateDefaultConfig() {
        // generate default config programmatically
        BungeeCrossConfig defaultConfig = new BungeeCrossConfig(
                new RedisConfig("",6379,"","","")
        );
        String jsonString = (new GsonBuilder().setPrettyPrinting().create()).toJson(defaultConfig);

        // save to file
        try(BufferedOutputStream outputStream = new BufferedOutputStream(
                Files.newOutputStream(Paths.get(BungeeCross.configurationFileName),CREATE_NEW, APPEND)
        )) {
            outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.severe(String.format("Failed to generate default config file %s: %s.", BungeeCross.configurationFileName, e));
        }
    }

}
