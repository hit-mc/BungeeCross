package com.keuin.bungeecross;

import com.google.gson.JsonParseException;
import com.keuin.bungeecross.config.ConfigManager;
import com.keuin.bungeecross.intercommunicate.msghandler.InGameChatHandler;
import com.keuin.bungeecross.intercommunicate.pubsub.BrokerManager;
import com.keuin.bungeecross.intercommunicate.repeater.CrossServerChatRepeater;
import com.keuin.bungeecross.intercommunicate.repeater.InGameRedisRelayRepeater;
import com.keuin.bungeecross.microapi.BungeeMicroApi;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;
import com.keuin.bungeecross.mininstruction.dispatcher.ConcreteInstructionDispatcher;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import com.keuin.bungeecross.mininstruction.history.ActivityProvider;
import com.keuin.bungeecross.recentmsg.ConcreteRecentMessageManager;
import com.keuin.bungeecross.recentmsg.RecentMessageManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;

public class BungeeCross extends Plugin {

    private final Logger logger = Logger.getLogger(BungeeCross.class.getName());
    private static String VERSION = "";
    private static String BUILD_TIME = "";

    private ProxyServer proxyServer;
    private CrossServerChatRepeater crossServerChatRepeater;
    private InGameRedisRelayRepeater inGameRedisRelayRepeater;
    private BrokerManager brokerManager;
    private InGameChatHandler inGameChatProcessor;
    private MinInstructionInterpreter interpreter;
    private ActivityProvider activityProvider;
    private InstructionDispatcher instructionDispatcher;
    private BungeeMicroApi microApi;
    private RecentMessageManager recentMessageManager;

    private static final String repeatMessagePrefix = "#";
    private static final String inGameCommandPrefix = "!BC";
    private static final String configurationFileName = "bungeecross.json";
    private static final String activityPersistenceFileName = "activity.json";

    public static String getVersion() {
        return VERSION;
    }

    public static String getBuildTime() {
        return BUILD_TIME;
    }

    {
        try {
            // get version string
            Properties properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
            VERSION = properties.getProperty("version");
            BUILD_TIME = properties.getProperty("build.date");
        } catch (IOException e) {
            logger.warning("Failed to get version string: " + e.getMessage());
        }
    }

    private boolean loadConfig() {
        try {
            ConfigManager.INSTANCE.loadConfig(configurationFileName);
            return true;
        } catch (FileNotFoundException e) {
            logger.severe(String.format("BungeeCross config file %s does not exist.",
                    BungeeCross.configurationFileName));
            return false;
        } catch (JsonParseException e) {
            logger.severe(String.format("Malformed JSON config file : %s", e.getMessage()));
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            logger.severe(String.format("Cannot read config file: %s", e.getMessage()));
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onEnable() {
        try {
            // try to generate default config
            try {
                ConfigManager.INSTANCE.generateDefaultConfig(configurationFileName);
                logger.warning(String.format("A template config file %s has been generated. " +
                        "Please edit it and reload BungeeCross.", configurationFileName));
                // default config generated
                return;
            } catch (FileAlreadyExistsException ignored) {
                // config file already exists
                // continue loading
            }

            // load config
            if (!loadConfig()) {
                logger.severe("Cannot load config. BungeeCross will be disabled.");
                return;
            }

            // get proxy server
            proxyServer = ProxyServer.getInstance();

            // initialize repeater
            crossServerChatRepeater = new CrossServerChatRepeater(proxyServer);
            inGameRedisRelayRepeater = new InGameRedisRelayRepeater(proxyServer);

            File file = new File(activityPersistenceFileName);
            if (!file.exists()) {
                // file does not exist
                logger.info("Activity persistence file " + activityPersistenceFileName + "does not exist. Reset activity statistics.");
                activityProvider = new ActivityProvider(activityPersistenceFileName, false);
            } else {
                activityProvider = new ActivityProvider(activityPersistenceFileName);
            }

            interpreter = new MinInstructionInterpreter(this, activityProvider, proxyServer);
            instructionDispatcher = new ConcreteInstructionDispatcher(interpreter);
//            redisManager.setInstructionDispatcher(instructionDispatcher);
            brokerManager = new BrokerManager(inGameRedisRelayRepeater, instructionDispatcher);
            inGameChatProcessor = new InGameChatHandler(repeatMessagePrefix, inGameCommandPrefix, crossServerChatRepeater,
                    brokerManager, instructionDispatcher);
            recentMessageManager = new ConcreteRecentMessageManager(ConfigManager.INSTANCE.getRootConfig()
                    .getHistoryMessageLifeSeconds());

            // register history message logger
            brokerManager.registerHistoryLogger(recentMessageManager);
            inGameChatProcessor.registerHistoryLogger(recentMessageManager);

            // register events
            getProxy().getPluginManager().registerListener(this, new BungeeEventHandler(this, inGameChatProcessor, activityProvider, recentMessageManager));

            // start redis thread
            brokerManager.start();

            // start micro api server
            int port;
            if ((port = ConfigManager.INSTANCE.getRootConfig().getMicroApiPort()) <= 0) {
                logger.info(String.format(
                        "Illegal MicroApi port: %d. MicroApi will be disabled.",
                        port
                ));
            } else {
                microApi = new BungeeMicroApi(port, brokerManager);
            }

//        // Start the repeat thread
//        getProxy().getScheduler().runAsync(this, this::messageRepeatThread);
        } catch (IOException e) {
            logger.severe("Failed to initialize: " + e);
        }
    }

    @Override
    public void onDisable() {
        logger.info("Stopping MicroApi server...");
        Optional.ofNullable(microApi).ifPresent(BungeeMicroApi::stop);

        // Unregister event listeners
        logger.info("Unregistering events...");
        getProxy().getPluginManager().unregisterListeners(this);

        logger.info("Saving activity history...");
        activityProvider.close();

        logger.info("Stopping RedisManager...");
        Optional.ofNullable(brokerManager).ifPresent(BrokerManager::stop);

        logger.info("Stopping InGameChatHandler...");
        Optional.ofNullable(inGameChatProcessor).ifPresent(InGameChatHandler::close);

        logger.info("Stopping InstructionDispatcher...");
        Optional.ofNullable(instructionDispatcher).ifPresent(InstructionDispatcher::close);

        logger.info("Calling super disable routine...");
        super.onDisable();
    }


    public static String generateTopicId() {
        var rng = new SecureRandom();
        var rand = new byte[8];
        rng.nextBytes(rand);
        long acc = 0;
        for (int i = 0; i != 8; ++i) {
            acc = (acc << 8) + (rand[i] & 0xFFL);
        }
        return Long.toHexString(acc);
    }

}
