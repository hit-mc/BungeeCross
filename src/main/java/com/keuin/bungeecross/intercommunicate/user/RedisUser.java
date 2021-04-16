package com.keuin.bungeecross.intercommunicate.user;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class RedisUser implements MessageUser {

    // Immutable

    private final String userName;
    private final String uniqueId;
    private final String location;

    public RedisUser(@NotNull String userName) {
        this(userName, userName, "QQ");
    }

    public RedisUser(@NotNull String userName, @NotNull String uniqueId, String location) {
        this.userName = Objects.requireNonNull(userName);
        this.uniqueId = Objects.requireNonNull(uniqueId);
        this.location = location;
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
    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("%s@%s", getName(), getLocation());
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
