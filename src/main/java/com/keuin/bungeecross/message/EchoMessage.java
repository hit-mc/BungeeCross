package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.user.MessageUser;

import java.util.UUID;

public class EchoMessage implements Message {

    private final String echo;
    private final String instruction;
    private final MessageUser consoleUser;
    private final String LOCATION = "SERVER";

    public EchoMessage(String echo, String instruction) {
        this.echo = echo;
        this.instruction = instruction;
        this.consoleUser = new MessageUser() {
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
        };
    }

    public String getInstruction() {
        return instruction;
    }

    @Override
    public String getMessage() {
        return echo;
    }

    @Override
    public MessageUser getSender() {
        return consoleUser;
    }
}
