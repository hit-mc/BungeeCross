package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.util.PrettyComponents;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ListExecutor extends AbstractInstructionExecutor {

    private static final ListExecutor INSTANCE = new ListExecutor(
            "show online players in all servers.",
            new String[0]
    );

    private ListExecutor(String description, String[] params) {
        super("list", description, params);
    }

    public static ListExecutor getInstance() {
        return INSTANCE;
    }

    @Override
    public void execute(MessageRepeater echoRepeater) {
        ProxyServer proxy = ProxyServer.getInstance();
        int onlinePlayers = proxy.getOnlineCount();

        echo(echoRepeater, new ComponentBuilder(String.format(
                "There %s %d %s online%s",
                onlinePlayers <= 1 ? "is" : "are",
                onlinePlayers,
                onlinePlayers <= 1 ? "player" : "players",
                onlinePlayers == 0 ? "." : ":"
        )).color(ChatColor.WHITE).create());

        // players
        List<BaseComponent> players = new ArrayList<>();
        proxy.getPlayers().forEach(player -> players.addAll(Arrays.asList(getPlayerPrettyComponent(player))));
        if (!players.isEmpty())
            echo(echoRepeater, players.toArray(new BaseComponent[0]));
    }

    private BaseComponent[] getPlayerPrettyComponent(ProxiedPlayer player) {
        ComponentBuilder prettyBuilder = new ComponentBuilder();

        // build server text
        BaseComponent componentServer = PrettyComponents.createNavigableServerButton(player.getServer().getInfo().getName());

        // build player text
        TextComponent componentPlayer = new TextComponent(" " + player.getName());
        componentPlayer.setColor(ChatColor.WHITE);
        componentPlayer.setUnderlined(false);
        componentPlayer.setHoverEvent(null);

        // build pretty text
        prettyBuilder.append(componentPlayer);
        prettyBuilder.append(new TextComponent(" "));
        prettyBuilder.append(componentServer);
        return prettyBuilder.create();
    }
}
