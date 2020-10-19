package com.keuin.bungeecross.mininstruction.history;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.keuin.bungeecross.mininstruction.executor.history.InGamePlayer;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * Recording, and providing players' activities.
 */
public class ActivityProvider {

    private final TreeMap<Long, InGamePlayer> history; // lastActiveTimestamp -> player. All access to this ADT must be serialized.
    private final String jsonFileName;


    public ActivityProvider(String jsonFile) throws IOException {
        try (Reader reader = new BufferedReader(new FileReader(jsonFile))) {
            Gson gson = new Gson();
            Type gsonType = new TypeToken<TreeMap<Long, UUID>>(){}.getType();
            // String gsonString = gson.toJson(data, gsonType);
            history = gson.fromJson(reader, gsonType);
            this.jsonFileName = jsonFile;
        }
    }


    /**
     * Get the set of active players in a certain time range.
     * @param timeRange the time range backed from current time.
     * @param unit the time unit.
     * @return a set containing all active players. You are not allowed to modify it.
     */
    public synchronized Set<InGamePlayer> getActivePlayers(long timeRange, TimeUnit unit) {
        long minActiveTs = unit.toMillis(timeRange);
        Map<Long, InGamePlayer> activePlayersMap =  history.tailMap(minActiveTs);
        return new HashSet<>(activePlayersMap.values());
    }

    /**
     * Record the activity of a certain player.
     * @param playerUniqueId the player's UUID.
     */
    public synchronized void recordActivity(InGamePlayer playerUniqueId) {
        history.put(System.currentTimeMillis(), playerUniqueId);
    }

    /**
     * Save modification to disk.
     */
    public synchronized void save() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type gsonType = new TypeToken<TreeMap<Long, UUID>>(){}.getType();
        String jsonString = gson.toJson(history, gsonType);
        try(BufferedOutputStream outputStream = new BufferedOutputStream(
                Files.newOutputStream(Paths.get(jsonFileName),CREATE_NEW, APPEND)
        )) {
            outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        }
    }

}
