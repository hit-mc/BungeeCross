package com.keuin.bungeecross.mininstruction.executor;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.Chat;

import javax.xml.soap.Text;
import java.util.Map;

public class ListExecutor implements InstructionExecutor {

    private static final ListExecutor INSTANCE = new ListExecutor();
    private static final String commandString = "list";

    public static ListExecutor getInstance() {
        return INSTANCE;
    }

    @Override
    public BaseComponent[] execute() {
        ProxyServer proxy = ProxyServer.getInstance();
        ComponentBuilder builder = new ComponentBuilder();
        int onlinePlayers = proxy.getOnlineCount();
        builder.append(new ComponentBuilder(String.format(
                "There %s %d %s online%s\n",
                onlinePlayers <= 1 ? "is" : "are",
                onlinePlayers,
                onlinePlayers <= 1 ? "player" : "players",
                onlinePlayers == 0 ? "." : ":"
                )).color(ChatColor.WHITE).create());

        // players
        proxy.getPlayers().forEach(player -> builder.append(getPlayerPrettyComponent(player)));

        return builder.create();
    }

    @Override
    public String getCommand() {
        return commandString;
    }

    @Override
    public String getUsage() {
        return ": show online players in all servers.";
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
        componentServer.setHoverEvent(
                new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        hoverTextBuilder.create()
                )
        );
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
        prettyBuilder.append(componentServer);
        return prettyBuilder.create();
    }
}
