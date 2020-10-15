package com.keuin.bungeecross.message.user;

import java.util.UUID;

public class RedisUser implements MessageUser {

    // Immutable

    private final String userName;
    private final String uniqueId; // may be null

    public RedisUser(String userName) {
        this(userName, null);
    }

    public RedisUser(String userName, String uniqueId) {
        this.userName = userName;
        this.uniqueId = uniqueId;
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public String getId() {
        return uniqueId;
    }

    @Override
    public String toString() {
        return "RedisUser{" +
                "userName='" + userName + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                '}';
    }
}
