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

public class UpExecutor extends AbstractInstructionExecutor {

    private final ProxyServer proxyServer;
    private final long timeoutMillis = 3000;

    public UpExecutor(@NotNull ProxyServer proxyServer) {
        super("up", "check status of all servers.", new String[0]);
        this.proxyServer = Objects.requireNonNull(proxyServer);
    }

    @Override
    protected ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
        final Map<String, ServerInfo> serverMap = proxyServer.getServers();
        final Map<String, Boolean> pingResult = Collections.synchronizedMap(new TreeMap<>());

        final AtomicInteger counterRemainingResponses = new AtomicInteger(serverMap.size()); // number of not arrived responses
        final Object responseAlarm = new Object(); // the main thread waits, the last response callback notifies

        for (Map.Entry<String, ServerInfo> entry : serverMap.entrySet()) {
            final String serverName = entry.getKey();
            entry.getValue().ping((result, error) -> {
                pingResult.put(serverName, error == null);
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
            while (counterRemainingResponses.get() != 0) {
                synchronized (responseAlarm) {
                    responseAlarm.wait(timeoutMillis);
                }
            }
        } catch (InterruptedException ignored) {
            // all up
            // do nothing here
        }
        // if not interrupted, means timed out, and at least one server has down
        ComponentBuilder builder = new ComponentBuilder()
                .append(new ComponentBuilder("Servers:").color(ChatColor.WHITE).create());

        boolean[] isFirst = new boolean[]{true};
        pingResult.forEach((name, isUp) -> {
            if (isFirst[0]) {
                isFirst[0] = false;
                builder.append("\n");
            }
            builder.append(new ComponentBuilder(name).color(ChatColor.WHITE).create())
                    .append(new TextComponent(": "))
                    .append(isUp ?
                            new ComponentBuilder("[UP]").color(ChatColor.GREEN).bold(true).create()
                            : new ComponentBuilder("[DOWN]").color(ChatColor.RED).bold(true).create()
                    ).append(new ComponentBuilder("\n").color(ChatColor.WHITE).create());
        });

        echo(echoRepeater, builder.create());
        return ExecutionResult.SUCCESS;
    }
}
