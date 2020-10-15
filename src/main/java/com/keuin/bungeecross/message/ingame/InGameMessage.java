package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.user.MessageUser;

public class InGameMessage implements Message {
    private final String message;
    private final MessageUser sender;

    public InGameMessage(String message, MessageUser sender) {
        this.message = message;
        this.sender = sender;
        if(message == null || sender == null)
            throw new IllegalArgumentException("message and sender must not be null.");
    }

    public String getMessage() {
        return message;
    }

    public MessageUser getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return String.format("<%s> %s", sender.getName(), message);
    }
}