package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.user.MessageUser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

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
    public BaseComponent[] getRichTextMessage() {
        return new BaseComponent[]{new TextComponent(message)};
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
