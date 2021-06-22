package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class UpExecutor extends AbstractInstructionExecutor {

    private final ProxyServer proxyServer;
    private final long timeoutMillis = 3000;
    private final Logger logger = Logger.getLogger(UpExecutor.class.getName());

    public UpExecutor(@NotNull ProxyServer proxyServer) {
        super("up", "check status of all servers.", new String[0]);
        this.proxyServer = Objects.requireNonNull(proxyServer);
    }

    @Override
    protected ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
        final Map<String, ServerInfo> serverMap = proxyServer.getServers();
        final Map<String, Boolean> pingResult = Collections.synchronizedMap(new TreeMap<>());

        // number of not arrived responses
        final AtomicInteger counterRemainingResponses = new AtomicInteger(serverMap.size());
        // the main thread waits, the last response callback notifies
        final Object responseAlarm = new Object();

        for (Map.Entry<String, ServerInfo> entry : serverMap.entrySet()) {
            final String serverName = entry.getKey();
            logger.info(String.format("Pinging server %s...", serverName));
            entry.getValue().ping((result, error) -> {
                pingResult.put(serverName, error == null);
                if (error == null) {
                    logger.info(String.format("Server %s is up.", serverName));
                } else {
                    logger.info(String.format("Server %s is down: %s %s", serverName,
                            error.getClass().getName(), error.getMessage()));
                }
                if (counterRemainingResponses.decrementAndGet() == 0) {
                    // this is the last response
                    // all servers are up
                    synchronized (responseAlarm) {
                        responseAlarm.notify();
                    }
                }
            });
        }

        try {
            // to handle spurious wakeup
            int remainingCount;
            while ((remainingCount = counterRemainingResponses.get()) != 0) {
                logger.info(String.format("Waiting response from %d servers...", remainingCount));
                synchronized (responseAlarm) {
                    responseAlarm.wait(timeoutMillis);
                }
            }
        } catch (InterruptedException ignored) {
            // all up
            // do nothing here
        }
        logger.info("Finished pinging.");

        // if not interrupted, means timed out, and at least one server has down
        var builder = new ComponentBuilder()
                .append(new ComponentBuilder("Servers:").color(ChatColor.WHITE).create());

        pingResult.forEach(
                (name, isUp) -> builder.append(new ComponentBuilder("\n" + name).color(ChatColor.WHITE).create())
                        .append(new TextComponent(": "))
                        .append(isUp ?
                                new ComponentBuilder("[UP]").color(ChatColor.GREEN).bold(true).create()
                                : new ComponentBuilder("[DOWN]").color(ChatColor.RED).bold(true).create()
                        )
        );

        echo(echoRepeater, builder.create());
        return ExecutionResult.SUCCESS;
    }
}
