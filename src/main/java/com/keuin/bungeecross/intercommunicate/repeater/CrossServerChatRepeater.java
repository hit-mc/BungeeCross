package com.keuin.bungeecross.intercommunicate.repeater;

import com.keuin.bungeecross.intercommunicate.message.Message;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class CrossServerChatRepeater extends MessageRepeater {

    private final Collection<ServerInfo> servers;

    public CrossServerChatRepeater(ProxyServer proxyServer) {
        if (proxyServer == null)
            throw new IllegalArgumentException("proxy server must not be null");
        servers = proxyServer.getServers().values();
    }

    /**
     * Repeat message to all other servers.
     * @param message the message to be repeated.
     */
    @Override
    public void repeat(Message message) {
        servers.stream().filter(server -> {
            boolean repeat = true; // whether we should repeat message to this server
            UUID senderUUID = message.getSender().getUUID();
            for (ProxiedPlayer player : server.getPlayers()) {
                if (player != null && Objects.equals(senderUUID, player.getUniqueId())) {
                    repeat = false;
                    break;
                }
            }
            return repeat;
        }).forEach(server -> broadcastInServer(message, server));
    }

    @Override
    public String toString() {
        return String.format("InGameBroadcast(servers=%s)", this.servers.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrossServerChatRepeater that = (CrossServerChatRepeater) o;
        return servers.equals(that.servers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servers);
    }
}
