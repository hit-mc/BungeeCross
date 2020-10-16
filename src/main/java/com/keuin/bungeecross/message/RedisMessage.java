package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.user.MessageUser;

public class RedisMessage implements Message {

    private final MessageUser sender;
    private final String message;

    public RedisMessage(MessageUser sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public MessageUser getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return String.format("<%s> %s", sender.getName(), message);
    }
}
