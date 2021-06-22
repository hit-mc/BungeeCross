package com.keuin.bungeecross.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.keuin.bungeecross.config.mutable.MutableBungeeCrossConfig;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class ConfigManager {

    public static final ConfigManager INSTANCE = new ConfigManager();
    private static final Logger logger = Logger.getLogger(ConfigManager.class.getName());
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // the only instance of root config
    // thus any modifications on the config can be propagated to everywhere instantly
    private final MutableBungeeCrossConfig config = new MutableBungeeCrossConfig();

    private ConfigManager() {
    }

    /**
     * Load global config.
     */
    public void loadConfig(@NotNull String configurationFileName) throws IOException, JsonParseException {
        File configFile = new File(configurationFileName);
        try (var configReader = Files.newBufferedReader(configFile.toPath())) {
            config.copyFrom(gson.fromJson(configReader, MutableBungeeCrossConfig.class));
        }
    }

    /**
     * Generate default config file.
     */
    public void generateDefaultConfig(String configFileName) throws IOException {
        try (var writer = Files.newBufferedWriter(new File(configFileName).toPath(),
                StandardCharsets.UTF_8, CREATE_NEW)) {
            // generate default config
            gson.toJson(new MutableBungeeCrossConfig(), writer);
        } catch (FileAlreadyExistsException e) {
            throw new FileAlreadyExistsException("Config file \"" + configFileName + "\" exists. " +
                    "Refuse to generate default config.");
        }
    }

    /**
     * Get the root config instance.
     * This instance and all its sub-nodes, are thread-safe and always updated.
     * Please do not cache config locally and always read config directly from the instance.
     * This method always return the same instance during the whole execution.
     * @return the root config.
     */
    public BungeeCrossConfig getRootConfig() {
        return config;
    }
}
