package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.user.MessageUser;

import java.util.UUID;

public class TestableMessageUser implements MessageUser {

    private final String name;
    private final UUID uuid;
    private final String id;
    private final String location;

    public TestableMessageUser(String name, UUID uuid, String id, String location) {
        this.name = name;
        this.uuid = uuid;
        this.id = id;
        this.location = location;
    }

    public static TestableMessageUser create(String name, UUID uuid, String id, String location) {
        return new TestableMessageUser(name, uuid, id, location);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLocation() {
        return location;
    }
}
