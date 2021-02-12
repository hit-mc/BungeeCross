package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.util.date.DateUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.time.LocalDateTime;

/**
 * A past sent message.
 * Showing a message sent in the past, i.e. when the player is offline,
 * or the display is not the original display.
 */
public class HistoryMessage implements Message {

    private final Message originalMessage;
    private final LocalDateTime sentTime;

    public HistoryMessage(Message originalMessage, LocalDateTime sentTime) {
        this.originalMessage = originalMessage;
        this.sentTime = sentTime;
    }

    /**
     * Get when the original message was sent.
     * @return the time.
     */
    public LocalDateTime getSentTime() {
        return sentTime;
    }

    @Override
    public String getMessage() {
        return "(" + DateUtil.getOffsetString(sentTime) + ") " + originalMessage.getMessage();
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        BaseComponent[] originalComponents = originalMessage.getRichTextMessage();
        BaseComponent[] newComponents = new ComponentBuilder(getMessage()).color(ChatColor.DARK_GREEN).create();
        BaseComponent[] arr = new BaseComponent[originalComponents.length + newComponents.length];
        System.arraycopy(newComponents, 0, arr, 0, newComponents.length);
        int off = newComponents.length;
        System.arraycopy(originalComponents, 0, arr, off, originalComponents.length);
        return arr;
    }

    @Override
    public MessageUser getSender() {
        return originalMessage.getSender();
    }

    @Override
    public boolean isJoinable() {
        // May be buggy?
        return originalMessage.isJoinable();
    }
}
