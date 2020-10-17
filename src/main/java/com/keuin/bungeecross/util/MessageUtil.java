package com.keuin.bungeecross.util;

import com.keuin.bungeecross.message.JoinedMessage;
import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.user.MessageUser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.Collection;

public class MessageUtil {

    public static String getPlainTextOfBaseComponents(BaseComponent[] baseComponents) {
        if (baseComponents == null)
            throw new IllegalArgumentException("baseComponents must not be null.");
        StringBuilder builder = new StringBuilder();
        for (BaseComponent component : baseComponents) {
            builder.append(component.toPlainText());
        }
        return builder.toString();
    }

    public static Message joinMessage(Collection<Message> messages) {
        if (messages.isEmpty())
            return null;
        Message firstMessage = messages.toArray(new Message[0])[0];
        if (messages.size() == 1)
            return firstMessage;

        MessageUser sender = firstMessage.getSender();
        ComponentBuilder contentBuilder = new ComponentBuilder();

        for (Message msg : messages) {
            if (!msg.getSender().equals(sender)) {
                throw new IllegalArgumentException("Messages are not sent by the same user.");
            }
            BaseComponent[] baseComponents = msg.getRichTextMessage();
            if (baseComponents != null && baseComponents.length != 0)
                contentBuilder.append(baseComponents);
        }
        return new JoinedMessage(contentBuilder.create(), sender);
    }

}
