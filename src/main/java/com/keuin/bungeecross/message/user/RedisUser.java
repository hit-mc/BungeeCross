package com.keuin.bungeecross.message.user;

import java.util.UUID;

public class RedisUser implements MessageUser {

    // Immutable

    private final String userName;
    private final String uniqueId;
    private final String location = "QQ";

    public RedisUser(String userName) {
        this(userName, userName);
    }

    public RedisUser(String userName, String uniqueId) {
        this.userName = userName;
        this.uniqueId = uniqueId;
    }

    @Override
    public String getName() {
        return String.format("%s@%s", userName, location);
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
    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "RedisUser{" +
                "userName='" + userName + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                '}';
    }
}
