package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.user.MessageUser;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class InGameMessage implements Message {
    private final String message;
    private final MessageUser sender;
    private final ProxiedPlayer proxiedPlayer;

    public InGameMessage(String message, MessageUser sender, ProxiedPlayer proxiedPlayer) {
        this.message = message;
        this.sender = sender;
        this.proxiedPlayer = proxiedPlayer;
        if(message == null || sender == null)
            throw new IllegalArgumentException("message and sender must not be null.");
    }

    public String getMessage() {
        return message;
    }

    public MessageUser getSender() {
        return sender;
    }

    public ProxiedPlayer getProxiedPlayer() {
        return proxiedPlayer;
    }

    @Override
    public String toString() {
        return String.format("<%s> %s", sender.getName(), message);
    }
}