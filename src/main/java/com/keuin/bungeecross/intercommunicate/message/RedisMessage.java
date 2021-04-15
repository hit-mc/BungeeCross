package com.keuin.bungeecross.intercommunicate.message;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;

public class RedisMessage extends Message {

    private final MessageUser sender;
    private final String message;
    private final BaseComponent[] components;

    public RedisMessage(MessageUser sender, String message) {
        this.sender = sender;
        this.message = message;
        this.components = null;
    }

    public RedisMessage(MessageUser sender, BaseComponent[] components) {
        this.sender = sender;
        this.components = Arrays.copyOf(components, components.length);
        this.message = MessageUtil.getPlainTextOfBaseComponents(components);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        if (components == null)
            return new BaseComponent[]{new TextComponent(message)};
        return Arrays.copyOf(components, components.length);
    }

    @Override
    public MessageUser getSender() {
        return sender;
    }

    @Override
    public boolean isJoinable() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("<%s> %s", sender.getName(), message);
    }
}
