package com.keuin.bungeecross.message.user;

import java.util.UUID;

/**
 * Immutable
 */
public interface MessageUser {
    /**
     * Get the user's friendly display name.
     * @return the display name.
     */
    String getName();

    /**
     * Get user's UUID.
     * @return the UUID. If this user does not have a UUID (such as a Redis user), will return null.
     */
    UUID getUUID();

    /**
     * Get the user's internal id (such as QQ Number, Minecraft player id).
     * @return the id.
     */
    String getId();

    /**
     * Get where the user is, such as the server name, or the Redis peer name (QQ).
     *
     * @return the location name.
     */
    String getLocation();

    static MessageUser build(String name, String id, String location) {
        return new MessageUser() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public UUID getUUID() {
                return null;
            }

            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getLocation() {
                return location;
            }
        };
    }
}
