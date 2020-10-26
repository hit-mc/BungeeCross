package com.keuin.bungeecross.mininstruction.executor.history;

import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.executor.AbstractInstructionExecutor;
import com.keuin.bungeecross.mininstruction.history.ActivityProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HistoryExecutor extends AbstractInstructionExecutor {

    private final static HistoryExecutor INSTANCE = new HistoryExecutor(
            "show the players who have joined the server recently.",
            new String[0]
    );

    private static ActivityProvider activityProvider;
    private static ProxyServer proxy;
    private static final String instruction = "history";


    public static HistoryExecutor getInstance(ActivityProvider activityProvider, ProxyServer proxy) {
        HistoryExecutor.activityProvider = activityProvider;
        HistoryExecutor.proxy = proxy;
        return INSTANCE;
    }

    private HistoryExecutor(String description, String[] params) {
        super(instruction, description, params);
    }


    @Override
    public void execute(MessageRepeater echoRepeater) {
        // TODO
        if (activityProvider == null) {
            echo(echoRepeater, new ComponentBuilder(
                    "Activity provider is not available. Cannot show history right now."
            ).color(ChatColor.RED).create());
            return;
        }
        if (proxy == null) {
            echo(echoRepeater, "Proxy server is not available. Cannot show history right now.");
            return;
        }
        // TODO: Introduce time range as the 1st parameter.
        Set<InGamePlayer> activePlayers = activityProvider.getActivePlayers(1, TimeUnit.DAYS);
        if (activePlayers.isEmpty()) {
            echo(echoRepeater, new ComponentBuilder("There is no active player in the last 24h.").color(ChatColor.RED).create());
            return;
        }

        echo(echoRepeater, "Active players:");
        for (InGamePlayer player : activePlayers) {
            ComponentBuilder builder = new ComponentBuilder();
            builder.append(new ComponentBuilder(player.getName()).color(ChatColor.WHITE).create());
            if (proxy.getPlayer(player.getUniqueId()) != null) {
                // online
                builder.append(new ComponentBuilder("[ONLINE]").color(ChatColor.GREEN).create());
            } else {
                builder.append(new ComponentBuilder("[]").color(ChatColor.BLUE).create());
            }
            echo(echoRepeater, builder.create());
        }
    }

}
