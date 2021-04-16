package com.keuin.bungeecross.intercommunicate.user;

import com.keuin.bungeecross.util.Locations;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class PlayerUser implements MessageUser {

    // Immutable

    private final String playerName;
    private final UUID uuid;
    private final String location;

    public PlayerUser(String playerName, UUID uuid, String location) {
        this.playerName = Objects.requireNonNull(playerName);
        this.uuid = Objects.requireNonNull(uuid);
        this.location = Objects.requireNonNull(location);
    }

    public static PlayerUser fromProxiedPlayer(ProxiedPlayer proxiedPlayer) {
        return new PlayerUser(
                proxiedPlayer.getName(),
                proxiedPlayer.getUniqueId(),
                proxiedPlayer.getServer().getInfo().getName()
        );
    }

    @Override
    public @NotNull String getId() {
        return playerName;
    }

    @Override
    public @NotNull String getLocation() {
        return location;
    }

    @Override
    public @NotNull String getName() {
        return playerName;
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return Locations.locate(location, playerName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerUser that = (PlayerUser) o;
        return Objects.equals(playerName, that.playerName) &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, uuid, location);
    }
}
