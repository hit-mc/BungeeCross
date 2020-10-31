package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.repeater.MessageRepeater;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.config.ServerInfo;
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

        // basic info
        ServerInfo playerServer = player.getServer().getInfo();
        ChatColor serverTextColor = ChatColor.GREEN;

        // build hover text
        ComponentBuilder hoverTextBuilder = new ComponentBuilder();
        hoverTextBuilder.append("Go to server ");
        hoverTextBuilder.append((new ComponentBuilder("[" + playerServer.getName() + "]")).color(serverTextColor).create());

        // build server text
        TextComponent componentServer = new TextComponent("["+player.getServer().getInfo().getName()+"]");
        componentServer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverTextBuilder.create())));
        componentServer.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        String.format("/server %s", playerServer.getName())
                )
        );

        componentServer.setColor(serverTextColor);
        componentServer.setUnderlined(true);

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
