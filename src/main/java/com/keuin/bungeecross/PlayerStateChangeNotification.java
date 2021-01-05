package com.keuin.bungeecross;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;

public class PlayerStateChangeNotification {
    private final ProxyServer proxy;

    public PlayerStateChangeNotification(ProxyServer proxy) {
        this.proxy = proxy;
    }

    private void notifyPlayerMessageToOtherServers(ProxiedPlayer player, ServerInfo server, BaseComponent[] message) {
        for (ServerInfo serverInfo : proxy.getServers().values()) {
            // for all other servers
            if (!serverInfo.getName().equals(server.getName()))
                for (ProxiedPlayer dest : serverInfo.getPlayers())
                    if (dest != null && !Objects.equals(dest.getUniqueId(), player.getUniqueId()))
                        dest.sendMessage(message); // repeat the join message
        }
    }

    private BaseComponent[] getPlayerJoinOrDisconnectServerMessage(ProxiedPlayer player, ServerInfo server, PlayerServerAction action) {
        return (new ComponentBuilder(action.getNotificationString(player.getName(), server.getName())))
                .italic(true).color(ChatColor.YELLOW).create();
    }

    private void notifyPlayerMessageToOtherServers(ProxiedPlayer player, ServerInfo server, PlayerServerAction action) {
        notifyPlayerMessageToOtherServers(
                player,
                server,
                getPlayerJoinOrDisconnectServerMessage(player, server, action)
        );
    }

    public void notifyPlayerJoinServer(ProxiedPlayer player, ServerInfo server) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(server);
        notifyPlayerMessageToOtherServers(player, server, PlayerServerAction.JOIN);
    }

    public void notifyPlayerDisconnectServer(ProxiedPlayer player, ServerInfo server) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(server);
        notifyPlayerMessageToOtherServers(player, server, PlayerServerAction.DISCONNECT);
    }

    private enum PlayerServerAction {
        JOIN("joined") {
            @Override
            public String getNotificationString(String playerName, String serverName) {
                Objects.requireNonNull(playerName);
                Objects.requireNonNull(serverName);
                return playerName + " joined server [" + serverName + "]";
            }
        },
        DISCONNECT("disconnected") {
            @Override
            public String getNotificationString(String playerName, String serverName) {
                return playerName + " disconnected from server [" + serverName + "]";
            }
        };

        private final String string;

        PlayerServerAction(String string) {
            this.string = string;
        }

        public abstract String getNotificationString(String playerName, String serverName);

        @Override
        public String toString() {
            return string;
        }
    }

}
