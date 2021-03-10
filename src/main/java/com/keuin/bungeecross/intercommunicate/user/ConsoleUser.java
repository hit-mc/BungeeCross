package com.keuin.bungeecross.intercommunicate.user;

import java.util.Objects;
import java.util.UUID;

public class ConsoleUser implements MessageUser {

    private static final String LOCATION = "SERVER";
    private final String instruction;

    ConsoleUser(String instruction) {
        this.instruction = instruction;
    }

    @Override
    public String getName() {
        return String.format("%s@%s", instruction, getLocation());
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public String getId() {
        return instruction;
    }

    @Override
    public String getLocation() {
        return LOCATION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsoleUser that = (ConsoleUser) o;
        return Objects.equals(instruction, that.instruction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(LOCATION, instruction);
    }
}
