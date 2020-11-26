package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.Message;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InGameCommandEchoRepeater implements MessageRepeater {

    private final String playerId;
    private final UUID playerUUID;
    private final ProxiedPlayer proxiedPlayer;
    private final ProxyServer proxyServer;

    public InGameCommandEchoRepeater(String playerId, ProxyServer proxyServer) {
        this.playerId = playerId;
        this.proxyServer = proxyServer;
        this.playerUUID = null;
        this.proxiedPlayer = null;
        if (playerId == null)
            throw new IllegalArgumentException("playerId must not be null.");
        if (proxyServer == null)
            throw new IllegalArgumentException("proxyServer must not be null.");
    }

    public InGameCommandEchoRepeater(UUID playerUUID, ProxyServer proxyServer) {
        this.playerUUID = playerUUID;
        this.proxyServer = proxyServer;
        this.playerId = null;
        this.proxiedPlayer = null;
        if (playerUUID == null)
            throw new IllegalArgumentException("playerUUID must not be null.");
        if (proxyServer == null)
            throw new IllegalArgumentException("proxyServer must not be null.");
    }

    public InGameCommandEchoRepeater(ProxiedPlayer proxiedPlayer) {
        this.proxiedPlayer = proxiedPlayer;
        this.proxyServer = null;
        this.playerId = null;
        this.playerUUID = null;
        if (proxiedPlayer == null)
            throw new IllegalArgumentException("proxiedPlayer must not be null.");
    }

    @Override
    public void repeat(Message message) {
        ProxiedPlayer player;
        if (proxiedPlayer != null) {
            player = proxiedPlayer;
        } else if (playerUUID != null) {
            // get player by id
            assert proxyServer != null;
            player = proxyServer.getPlayer(playerUUID);
        } else {
            assert playerId != null;
            assert proxyServer != null;
            player = proxyServer.getPlayer(playerId);
        }
        Optional.ofNullable(player).ifPresent(p -> p.sendMessage(message.getRichTextMessage()));
    }

    @Override
    public String toString() {
        if (this.playerId != null)
            return String.format("Player(name=%s)", this.playerId);
        else if (this.playerUUID != null)
            return String.format("Player(uuid=%s)", this.playerUUID);
        else if (this.proxiedPlayer != null)
            return String.format("Player(name=%s, uuid=%s)", this.proxiedPlayer.getName(), this.proxiedPlayer.getUniqueId());
        return "null";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InGameCommandEchoRepeater that = (InGameCommandEchoRepeater) o;
        return Objects.equals(playerId, that.playerId) &&
                Objects.equals(playerUUID, that.playerUUID) &&
                Objects.equals(proxiedPlayer, that.proxiedPlayer) &&
                Objects.equals(proxyServer, that.proxyServer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, playerUUID, proxiedPlayer, proxyServer);
    }

    //    @Override
//    public boolean setBuffer(boolean enabled) {
//        return false;
//    }
//
//    @Override
//    public boolean isBufferEnabled() {
//        return false;
//    }
//
//    @Override
//    public void flush() {
//    }
}
