package com.keuin.bungeecross.message.user;

import java.util.UUID;

public class PlayerUser implements MessageUser {

    // Immutable

    private final String playerName;
    private final UUID uuid;

    public PlayerUser(String playerName, UUID uuid) {
        this.playerName = playerName;
        this.uuid = uuid;
    }

    @Override
    public String getId() {
        return playerName;
    }

    @Override
    public String getName() {
        return playerName;
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
}
