package com.keuin.bungeecross.intercommunicate.user;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable
 */
public interface MessageUser {
    static MessageUser createNameOnlyUser(String name) {
        Objects.requireNonNull(name);
        return new MessageUser() {
            @Override
            public @NotNull String getName() {
                return name;
            }

            @Override
            public UUID getUUID() {
                return null;
            }

            @Override
            public @NotNull String getId() {
                return name;
            }

            @Override
            public String getLocation() {
                return null;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    /**
     * Get the user's friendly display name.
     *
     * @return the display name.
     */
    @NotNull
    String getName();

    /**
     * Get user's UUID.
     * @return the UUID. If this user does not have a UUID (such as a Redis user), will return null.
     */
    @Nullable
    UUID getUUID();

    /**
     * Get the user's internal id (such as QQ Number, Minecraft player id).
     * @return the id.
     */
    @NotNull
    String getId();

    /**
     * Get where the user is, such as the server name, or the Redis peer name (QQ).
     *
     * @return the location name.
     */
    @Nullable
    String getLocation();
}
