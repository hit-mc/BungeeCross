package com.keuin.bungeecross.mininstruction.executor;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Map;

public class ListExecutor implements InstructionExecutor {

    private static final ListExecutor INSTANCE = new ListExecutor();
    private static final String commandString = "list";

    public static ListExecutor getInstance() {
        return INSTANCE;
    }

    @Override
    public BaseComponent[] execute() {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(new ComponentBuilder("Online players:\n").color(ChatColor.WHITE).create());
        ProxyServer proxy = ProxyServer.getInstance();
        for (Map.Entry<String, ServerInfo> server : proxy.getServers().entrySet()) {
            // server name
            builder.append(new ComponentBuilder(String.format("%s:", server.getKey())).color(ChatColor.AQUA).create());

            // players
            StringBuilder players = new StringBuilder();
            server.getValue().getPlayers().forEach(player -> players.append(String.format(" %s", player.getName())));
            builder.append(players.toString());

            builder.append("\n");
        }
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
}
