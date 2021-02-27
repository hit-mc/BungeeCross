package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.util.MessageUtil;
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
        if (originalMessage instanceof HistoryMessage)
            throw new IllegalArgumentException("Nested history message is not allowed.");
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

    /**
     * Get the original message that this history message includes.
     * @return the original message.
     */
    public Message getOriginalMessage() {
        return originalMessage;
    }

    @Override
    public String getMessage() {
        return MessageUtil.getPlainTextOfBaseComponents(this.getRichTextMessage());
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        BaseComponent[] originalComponents = originalMessage.toChatInGameRepeatFormat();
        BaseComponent[] newComponents = new ComponentBuilder("(" + DateUtil.getOffsetString(sentTime) + ") ")
                .color(ChatColor.DARK_GREEN).italic(true).create();
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
