package com.keuin.bungeecross.intercommunicate.user;

import com.keuin.bungeecross.util.Locations;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ConsoleUser implements MessageUser {

    private static final String LOCATION = "SERVER";
    private final String instruction;

    ConsoleUser(@NotNull String instruction) {
        this.instruction = Objects.requireNonNull(instruction);
    }

    @Override
    public @NotNull String getName() {
        return instruction;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public @NotNull String getId() {
        return instruction;
    }

    @Override
    public String getLocation() {
        return LOCATION;
    }

    @Override
    public String toString() {
        return Locations.locate(LOCATION, instruction);
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
