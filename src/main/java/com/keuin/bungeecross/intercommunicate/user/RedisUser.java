package com.keuin.bungeecross.intercommunicate.user;

import com.keuin.bungeecross.util.Locations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public RedisUser(@NotNull String userName, @NotNull String uniqueId, @NotNull String location) {
        this.userName = Objects.requireNonNull(userName);
        this.uniqueId = Objects.requireNonNull(uniqueId);
        this.location = Objects.requireNonNull(location);
    }

    @Override
    public @NotNull String getName() {
        return userName;
    }

    @Override
    public @Nullable UUID getUUID() {
        return null;
    }

    @Override
    public @NotNull String getId() {
        return uniqueId;
    }

    @Override
    public @NotNull String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        var location = getLocation();
        return Locations.locate(getLocation(), getName());
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
