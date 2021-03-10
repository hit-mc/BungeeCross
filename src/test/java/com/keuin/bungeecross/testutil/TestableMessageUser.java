package com.keuin.bungeecross.testutil;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;

import java.util.Objects;
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

    public static TestableMessageUser createSkeleton() {
        return new TestableMessageUser("name", UUID.randomUUID(), "id", "location");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestableMessageUser that = (TestableMessageUser) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(id, that.id) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid, id, location);
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
