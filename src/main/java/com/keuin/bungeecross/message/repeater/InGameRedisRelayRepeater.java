package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.InBoundMessageDispatcher;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class InGameRedisRelayRepeater implements InBoundMessageDispatcher {

    private final Collection<ServerInfo> servers;

    public InGameRedisRelayRepeater(ProxyServer proxyServer) {
        if (proxyServer == null)
            throw new IllegalArgumentException("proxy server must not be null");
        servers = proxyServer.getServers().values();
    }

    @Override
    public void repeatInboundMessage(Message message) {
        servers.forEach(server -> broadcastInServer(message, server));
    }

    private void broadcastInServer(Message message, ServerInfo server) {
        BaseComponent[] msg = message.toChatInGameRepeatFormat();
        server.getPlayers().forEach(p -> Optional.ofNullable(p).ifPresent(player -> player.sendMessage(msg)));
    }

    @Override
    public String toString() {
        return String.format("InGameRedisRelayRepeater(servers=%s)", this.servers.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InGameRedisRelayRepeater that = (InGameRedisRelayRepeater) o;
        return servers.equals(that.servers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servers);
    }
}
