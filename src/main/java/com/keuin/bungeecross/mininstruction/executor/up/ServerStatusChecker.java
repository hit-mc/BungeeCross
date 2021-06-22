package com.keuin.bungeecross.mininstruction.executor.up;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ServerStatusChecker {

    private static final Logger logger = Logger.getLogger(ServerStatusChecker.class.getName());
    private final Set<ServerInfo> servers;
    private final Plugin plugin;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Map<ServerInfo, ServerStatus> pingResult = new ConcurrentHashMap<>();
    private final AtomicInteger pingCountdown;
    private final int timeoutMillis;

    private ServerStatusChecker(@NotNull Collection<ServerInfo> servers, @NotNull Plugin plugin, int timeoutMillis) {
        this.servers = new HashSet<>(servers);
        this.plugin = plugin;
        this.timeoutMillis = timeoutMillis;
        this.pingCountdown = new AtomicInteger(this.servers.size());
        for (ServerInfo server : this.servers) {
            // initialize with TIMED OUT status,
            // which is default when no response received in waiting time range
            pingResult.put(server, ServerStatus.TIMED_OUT);
        }
    }

    /**
     * Get a new checker checking all servers of the given plugin.
     * @param plugin the plugin.
     * @return the checker.
     */
    public static ServerStatusChecker newChecker(@NotNull Plugin plugin, int timeoutMillis) {
        return new ServerStatusChecker(plugin.getProxy().getServers().values(), plugin, timeoutMillis);
    }

    /**
     * Perform ping asynchronously
     * @param callback the callback. Will be invoked asynchronously.
     */
    public void ping(Consumer<Map<ServerInfo, ServerStatus>> callback) {
        if (isRunning.getAndSet(true)) {
            // already running, do not start new threads
            return;
        }

        final Object finishEvent = new Object();
        final Consumer<Runnable> scheduler = (Runnable consumer) ->
                plugin.getProxy().getScheduler().runAsync(plugin, consumer);

        // async ping
        scheduler.accept(() -> servers.forEach(server ->
                server.ping((result, error) -> {
                    var isUp = result != null && error == null;
                    var builder = new StringBuilder();
                    builder.append(String.format("Server %s is %s.", server.getName(), isUp ? "up" : "down")).append(". ");
                    if (result != null) {
                        builder.append("MOTD: ").append(result.getDescriptionComponent().toPlainText()).append(" ");
                    }
                    if (error != null) {
                        builder.append("Error: ").append(error.getClass().getName()).append(" ").append(error.getMessage());
                    }
                    logger.info(builder.toString());
                    pingResult.put(server, isUp ? ServerStatus.ONLINE : ServerStatus.OFFLINE);
                    if (pingCountdown.decrementAndGet() == 0) {
                        synchronized (finishEvent) {
                            finishEvent.notifyAll();
                        }
                    }
                })));

        // async wait and invoke callback
        scheduler.accept(() -> {
            // wait until all server respond
            // or timed out
            var startTime = System.currentTimeMillis();
            int remaining;
            while ((remaining = pingCountdown.get()) != 0
                    && (System.currentTimeMillis() - startTime <= timeoutMillis)) {
                try {
                    synchronized (finishEvent) {
                        finishEvent.wait(1000, 0);
                    }
                } catch (InterruptedException ignored) {
                }
            }

            // all server have responded, or at least one server has timed out
            // copy to ignore further result
            callback.accept(new HashMap<>(pingResult));
        });
    }
}
