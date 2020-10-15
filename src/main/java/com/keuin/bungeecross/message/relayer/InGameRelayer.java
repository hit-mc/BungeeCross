package com.keuin.bungeecross.message.relayer;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.InBoundMessageDispatcher;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.UUID;

public class InGameRelayer implements MessageRelayer, InBoundMessageDispatcher {

    private final Collection<ServerInfo> servers;

    public InGameRelayer(ProxyServer proxyServer) {
        servers = proxyServer.getServers().values();
    }

    /**
     * Relay message to all other servers.
     * @param message the message to be relayed.
     */
    @Override
    public void relay(Message message) {
        UUID senderUUID = message.getSender().getUUID();
        for (ServerInfo targetServer : servers) {
            boolean relay = true; // whether we should relay message to this server
            for (ProxiedPlayer player : targetServer.getPlayers()) {
                if(senderUUID.equals(player.getUniqueId())) {
                    relay = false;
                    break;
                }
            }
            if(relay) {
                broadcastInServer(message, targetServer);
            }
        }
    }

    @Override
    public void relayInboundMessage(Message message) {
        servers.forEach((server) -> broadcastInServer(message, server));
    }

    private void broadcastInServer(Message message, ServerInfo server) {
        for (ProxiedPlayer player : server.getPlayers()) {
            player.sendMessage(new ComponentBuilder(message.toString()).color(ChatColor.DARK_PURPLE).create());
        }
    }

}
