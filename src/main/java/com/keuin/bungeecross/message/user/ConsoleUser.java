package com.keuin.bungeecross.message.user;

import java.util.UUID;

public class ConsoleUser implements MessageUser {

    private final String LOCATION = "SERVER";
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

}
