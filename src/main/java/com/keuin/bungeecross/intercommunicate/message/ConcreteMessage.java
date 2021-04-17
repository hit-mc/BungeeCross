package com.keuin.bungeecross.intercommunicate.message;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

class ConcreteMessage extends FixedTimeMessage {
    private final String sender;
    private final String message;

    ConcreteMessage(@NotNull String sender, @NotNull String message) {
        this.sender = Objects.requireNonNull(sender);
        this.message = Objects.requireNonNull(message);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        return new ComponentBuilder(message).create();
    }

    @Override
    public MessageUser getSender() {
        return MessageUser.createNameOnlyUser(sender);
    }

    @Override
    public boolean ifCanBeJoined() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", sender, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteMessage that = (ConcreteMessage) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, message);
    }
}
