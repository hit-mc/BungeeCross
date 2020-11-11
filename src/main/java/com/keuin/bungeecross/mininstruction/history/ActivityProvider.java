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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Recording, and providing players' activities.
 */
public class ActivityProvider {

    private AtomicBoolean modified = new AtomicBoolean(false);
    private final TreeMap<Long, InGamePlayer> history; // lastActiveTimestamp -> player. All access to this ADT must be serialized.
    private final Map<InGamePlayer, Long> reverseLookUpTable = new HashMap<>(); // reversed look-up-table, providing a player -> last-seen-time mapping. This is non-persistent.
    private final String jsonFileName;
    private final HistorySavingThread savingThread = new HistorySavingThread();
    private final Logger logger = Logger.getLogger(ActivityProvider.class.getName());
    private final int AUTO_SAVE_INTERVAL = 3;
    private int autoSaveCounter = AUTO_SAVE_INTERVAL;

    private final Object saveNotifier = new Object();

    public ActivityProvider(String jsonFile) throws IOException {
        this(jsonFile, true);
    }

    public ActivityProvider(String jsonFile, boolean loadFromFile) throws IOException {
        if (loadFromFile) {
            try (Reader reader = new BufferedReader(new FileReader(jsonFile))) {
                Gson gson = new Gson();
                Type gsonType = new TypeToken<TreeMap<Long, InGamePlayer>>(){}.getType();
                // String gsonString = gson.toJson(data, gsonType);
                history = gson.fromJson(reader, gsonType);
            }
        } else {
            history = new TreeMap<>();
        }
        this.jsonFileName = jsonFile;
        initializeLookUpTable();
        savingThread.start();
    }

    /**
     * Initialize the look-up-table from history TreeMap.
     */
    private void initializeLookUpTable() {
        reverseLookUpTable.clear();
        synchronized (history) {
            history.forEach((ts, player) -> {
                if (!reverseLookUpTable.containsKey(player) || (reverseLookUpTable.get(player) < ts))
                    reverseLookUpTable.put(player, ts);
            });
        }
    }

    /**
     * Add a player activity to the look-up-table.
     * @param player the player.
     * @param ts activity time stamp. If the time stamp is newer than the existing one, it will be updated.
     */
    private void updateLookUpTable(InGamePlayer player, Long ts) {
        if (reverseLookUpTable.getOrDefault(player, 0L) < ts)
            reverseLookUpTable.put(player, ts);
    }

    /**
     * Get the set of active players in a certain time range.
     * @param timeRange the time range backed from current time.
     * @param unit the time unit.
     * @return a set containing all active players. You are not allowed to modify it.
     */
    public Collection<InGamePlayer> getActivePlayers(long timeRange, TimeUnit unit) {
        long ts = (new Date()).toInstant().getEpochSecond();
        long minActiveTs = unit.toSeconds(timeRange) + ts; // the minimal timestamp to show
        synchronized (history) {
            Map<Long, InGamePlayer> activePlayersMap = history.tailMap(minActiveTs);
            return new HashSet<>(activePlayersMap.values());
        }
    }

    public LocalDateTime getRecentActiveTime(InGamePlayer player) {
//        for (Long ts : history.descendingKeySet()) {
//            if (history.get(ts).equals(player))
//                return LocalDateTime.ofEpochSecond(ts,0, OffsetDateTime.now().getOffset());
//        }

        // optimized using the reverse LUT.
        return LocalDateTime.ofEpochSecond(reverseLookUpTable.getOrDefault(player, 0L), 0, OffsetDateTime.now().getOffset());
    }

    /**
     * Record the activity of a player.
     * @param player the player's UUID.
     */
    public void logPlayerActivity(InGamePlayer player) {
        long ts = (new Date()).toInstant().getEpochSecond();
        synchronized (history) {
            history.put(ts, player);
        }
        modified.set(true);
        updateLookUpTable(player, ts);
        --autoSaveCounter;
        if (autoSaveCounter == 0) {
            autoSaveCounter = AUTO_SAVE_INTERVAL;
            save();
        }
    }

    /**
     * Save modification to disk.
     */
    public void save() {
        synchronized (saveNotifier) {
            saveNotifier.notifyAll();
        }
    }

    public void close() {
        savingThread.disable();
        save();
    }

    /**
     * This thread saves the history asynchronously.
     */
    private class HistorySavingThread extends Thread {

        private final AtomicBoolean running = new AtomicBoolean(true);

        @Override
        public void run() {
            while (running.get()) {

                try {
                    synchronized (saveNotifier) {
                        saveNotifier.wait();
                    }
                } catch (InterruptedException ignored) {
                }

                if (!modified.getAndSet(false))
                    continue;

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Type gsonType = new TypeToken<TreeMap<Long, InGamePlayer>>(){}.getType();
                String jsonString;
                synchronized (history) {
                    jsonString = gson.toJson(history, gsonType);
                }
                try(BufferedOutputStream outputStream = new BufferedOutputStream(
                        Files.newOutputStream(Paths.get(jsonFileName))
                )) {
                    outputStream.write(jsonString.getBytes(StandardCharsets.UTF_8));
                    logger.info("Activity saved to file " + jsonFileName);
                } catch (IOException e) {
                    logger.severe(String.format("Failed to save activity history: %s", e));
                }

            }
            logger.info("History saving thread is stopping.");
        }

        public void disable() {
            running.set(false);
        }
    }

}
