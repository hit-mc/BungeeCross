package com.keuin.bungeecross.message.user;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisUser redisUser = (RedisUser) o;
        return Objects.equals(userName, redisUser.userName) && Objects.equals(uniqueId, redisUser.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, uniqueId, location);
    }
}
