package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;

public class JoinedMessage implements Message {

    private final BaseComponent[] baseComponents;
    private final String message;
    private final MessageUser sender;

    public JoinedMessage(BaseComponent[] baseComponents, MessageUser sender) {
        this.sender = sender;
        this.message = MessageUtil.getPlainTextOfBaseComponents(baseComponents);
        BaseComponent[] copy = new BaseComponent[baseComponents.length];
        System.arraycopy(baseComponents, 0, copy, 0, copy.length);
        this.baseComponents = copy;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        BaseComponent[] copy = new BaseComponent[baseComponents.length];
        System.arraycopy(baseComponents, 0, copy, 0, copy.length);
        return copy;
    }

    @Override
    public MessageUser getSender() {
        return sender;
    }

    @Override
    public boolean isJoinable() {
        return true;
    }
}
