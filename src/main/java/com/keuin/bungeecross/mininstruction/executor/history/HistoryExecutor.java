package com.keuin.bungeecross.mininstruction.executor.history;

import com.keuin.bungeecross.message.EchoMessage;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.executor.AbstractInstructionExecutor;
import com.keuin.bungeecross.mininstruction.history.ActivityProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HistoryExecutor extends AbstractInstructionExecutor {

    private final static HistoryExecutor INSTANCE = new HistoryExecutor(
            "show the players who have joined the server recently.",
            new String[0]
    );

    private static ActivityProvider activityProvider;
    private static final String instruction = "history";


    public static HistoryExecutor getInstance(ActivityProvider activityProvider) {
        HistoryExecutor.activityProvider = activityProvider;
        return INSTANCE;
    }

    private HistoryExecutor(String description, String[] params) {
        super(instruction, description, params);
    }


    @Override
    public void execute(MessageRepeater echoRepeater) {
        // TODO
        if (activityProvider == null) {
            echoRepeater.repeat(new EchoMessage(getCommand(), new ComponentBuilder("Activity provider is not available. Cannot show history right now.").color(ChatColor.RED).create()));
        } else {
            // TODO: Introduce time range as the 1st parameter.
            Set<InGamePlayer> activePlayers = activityProvider.getActivePlayers(1, TimeUnit.DAYS);
            if (activePlayers.isEmpty()) {
                echo(echoRepeater, new ComponentBuilder("There is no active player in the last 24h.").color(ChatColor.RED).create());
                return;
            }

            echo(echoRepeater, "Active players:");
            for (InGamePlayer player : activePlayers) {
                echo(echoRepeater, player.getName());
            }
        }
    }

}
