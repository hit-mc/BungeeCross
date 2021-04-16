package com.keuin.bungeecross.intercommunicate.message;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.intercommunicate.user.PlayerUser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * The message sent by a game player.
 */
public class InGameMessage extends Message {

    private final String message;
    private final MessageUser sender;
    private final ProxiedPlayer proxiedPlayer;

    public InGameMessage(String message, ProxiedPlayer proxiedPlayer) {
        this.message = message;
        this.sender = PlayerUser.fromProxiedPlayer(proxiedPlayer);
        this.proxiedPlayer = proxiedPlayer;
        if (message == null)
            throw new IllegalArgumentException("message and sender must not be null.");
    }

    public String getMessage() {
        return message;
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        return new BaseComponent[]{new TextComponent(message)}; // copy to persist invariance
    }

    public MessageUser getSender() {
        return sender;
    }

    @Override
    public boolean ifCanBeJoined() {
        return false;
    }

    public ProxiedPlayer getProxiedPlayer() {
        return proxiedPlayer;
    }

    @Override
    public String toString() {
        return String.format("<%s> %s", sender, message);
    }
}