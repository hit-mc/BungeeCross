package com.keuin.bungeecross.mininstruction.executor.history;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import com.keuin.bungeecross.mininstruction.executor.AbstractInstructionExecutor;
import com.keuin.bungeecross.mininstruction.history.ActivityProvider;
import com.keuin.bungeecross.util.PrettyComponents;
import com.keuin.bungeecross.util.date.DateUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.Collection;
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
    public ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
        // TODO
        if (activityProvider == null) {
            echo(echoRepeater, new ComponentBuilder(
                    "Activity provider is not available. Cannot show history right now."
            ).color(ChatColor.RED).create());
            return ExecutionResult.FAILED;
        }
        if (proxy == null) {
            echo(echoRepeater, "Proxy server is not available. Cannot show history right now.");
            return ExecutionResult.FAILED;
        }
        Collection<InGamePlayer> activePlayers = activityProvider.getActivePlayers(1, TimeUnit.DAYS);
        if (activePlayers.isEmpty()) {
            echo(echoRepeater, new ComponentBuilder("There is no active player in the last 24h.").color(ChatColor.RED).create());
            return ExecutionResult.SUCCESS;
        }

        echo(echoRepeater, "Active players:");
        for (InGamePlayer player : activePlayers) {
            ComponentBuilder builder = new ComponentBuilder();
            builder.append(new ComponentBuilder(player.getName()).color(ChatColor.WHITE).create());
            if (proxy.getPlayer(player.getUniqueId()) != null) {
                // online
                String playerServer = proxy.getPlayer(player.getUniqueId()).getServer().getInfo().getName();
                builder.append(PrettyComponents.createNavigableServerButton(playerServer, " [ONLINE@%s]"));
            } else {
                String activeTime = DateUtil.getMonthDayHourMinuteString(activityProvider.getRecentActiveTime(player));
                builder.append(new ComponentBuilder(String.format(" [Last seen at %s]", activeTime)).color(ChatColor.BLUE).create());
            }
            echo(echoRepeater, builder.create());
        }

        return ExecutionResult.SUCCESS;
    }

}
