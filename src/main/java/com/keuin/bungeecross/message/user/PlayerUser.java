package com.keuin.bungeecross.message.user;

import java.util.Objects;
import java.util.UUID;

public class PlayerUser implements MessageUser {

    // Immutable

    private final String playerName;
    private final UUID uuid;
    private final String location;

    public PlayerUser(String playerName, UUID uuid, String location) {
        this.playerName = playerName;
        this.uuid = uuid;
        this.location = location;
    }

    @Override
    public String getId() {
        return playerName;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getName() {
        return String.format("%s@%s", playerName, location);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String toString() {
        return "MessagePlayer{" +
                "playerName='" + playerName + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
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
