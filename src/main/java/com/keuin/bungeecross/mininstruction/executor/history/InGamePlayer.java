package com.keuin.bungeecross.mininstruction.executor.history;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;
import java.util.UUID;

public final class InGamePlayer {

    private final UUID UniqueId;
    private final String name;

    public static InGamePlayer fromProxiedPlayer(ProxiedPlayer proxiedPlayer) {
        if (proxiedPlayer == null)
            throw new IllegalArgumentException("Proxied player cannot be null");
        return new InGamePlayer(proxiedPlayer.getUniqueId(), proxiedPlayer.getName());
    }

    public InGamePlayer(UUID uniqueId, String name) {
        UniqueId = uniqueId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getUniqueId() {
        return UniqueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InGamePlayer that = (InGamePlayer) o;
        return UniqueId.equals(that.UniqueId) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(UniqueId, name);
    }
}
