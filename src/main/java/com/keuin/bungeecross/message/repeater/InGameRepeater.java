package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.InBoundMessageDispatcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.UUID;

public class InGameRepeater implements MessageRepeater, InBoundMessageDispatcher {

    private final Collection<ServerInfo> servers;

    public InGameRepeater(ProxyServer proxyServer) {
        servers = proxyServer.getServers().values();
    }

    /**
     * Repeat message to all other servers.
     * @param message the message to be repeated.
     */
    @Override
    public void repeat(Message message) {
        UUID senderUUID = message.getSender().getUUID();
        for (ServerInfo targetServer : servers) {
            boolean repeat = true; // whether we should repeat message to this server
            for (ProxiedPlayer player : targetServer.getPlayers()) {
                if(senderUUID.equals(player.getUniqueId())) {
                    repeat = false;
                    break;
                }
            }
            if(repeat) {
                broadcastInServer(message, targetServer);
            }
        }
    }

    @Override
    public void repeatInboundMessage(Message message) {
        servers.forEach((server) -> broadcastInServer(message, server));
    }

    private void broadcastInServer(Message message, ServerInfo server) {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(new ComponentBuilder(String.format("<%s> ", message.getSender().getName())).color(ChatColor.LIGHT_PURPLE).create());
        builder.append(new ComponentBuilder(message.getMessage()).color(ChatColor.GRAY).create());
        BaseComponent[] sentMessage = builder.create();

        for (ProxiedPlayer player : server.getPlayers()) {
            if(player != null) {
                player.sendMessage(sentMessage);
            }
        }
    }

}
