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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Recording, and providing players' activities.
 */
public class ActivityProvider {

    private final TreeMap<Long, InGamePlayer> history; // lastActiveTimestamp -> player. All access to this ADT must be serialized.
    private final String jsonFileName;


    public ActivityProvider(String jsonFile) throws IOException {
        this(jsonFile, true);
    }

    public ActivityProvider(String jsonFile, boolean loadFromFile) throws IOException {
        if (loadFromFile) {
            try (Reader reader = new BufferedReader(new FileReader(jsonFile))) {
                Gson gson = new Gson();
                Type gsonType = new TypeToken<TreeMap<Long, UUID>>(){}.getType();
                // String gsonString = gson.toJson(data, gsonType);
                history = gson.fromJson(reader, gsonType);
            }
        } else {
            history = new TreeMap<>();
        }
        this.jsonFileName = jsonFile;
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

    public synchronized LocalDateTime getRecentActiveTime(InGamePlayer player) {
        for (Long ts : history.descendingKeySet()) {
            if (history.get(ts).equals(player))
                return LocalDateTime.ofEpochSecond(ts,0, OffsetDateTime.now().getOffset());
        }
        return null; // TODO: fix this null
    }

    /**
     * Record the logging in event of a player.
     * @param player the player's UUID.
     */
    public synchronized void playerLoggedIn(InGamePlayer player) {
        long ts = (new Date()).toInstant().getEpochSecond();
        history.put(ts, player);
    }

    /**
     * Save modification to disk.
     */
    public synchronized void save() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type gsonType = new TypeToken<TreeMap<Long, UUID>>(){}.getType();
        String jsonString = gson.toJson(history, gsonType);
        try(BufferedOutputStream outputStream = new BufferedOutputStream(
                Files.newOutputStream(Paths.get(jsonFileName))
        )) {
            outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
        }
    }

}
