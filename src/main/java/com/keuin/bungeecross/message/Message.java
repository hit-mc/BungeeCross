package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.message.user.RedisUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Message {

    /**
     * Get the message in pure text formatting.
     * @return the pure text message.
     */
    String getMessage();

    /**
     * Get the message in minecraft rich text formatting.
     * @return the rich text message.
     */
    BaseComponent[] getRichTextMessage();

    /**
     * Get the sender of this message.
     * @return the sender.
     */
    MessageUser getSender();

    /**
     * If the message could be joined with neighbouring messages sent by the same user.
     * @return true if joinable, false if not.
     */
    boolean isJoinable();

    /**
     * Pack message into Redis format.
     *
     * @return packed string.
     */
    default String pack() {
        String SPLIT = "||";
        return String.format("%s%s%s", getSender().getName(), SPLIT, getMessage());
    }

    static Message build(String message, String sender) {
        return new ConcreteMessage(sender, message);
    }

    /**
     * Construct a Message object by raw string from Redis.
     *
     * @param rawString the raw string.
     * @return a Message object. If the raw string is invalid, return null.
     */
    static Message fromRedisRawString(String rawString) {
        Pattern pattern = Pattern.compile("([^|]*)(?:\\|\\|)([\\s\\S]*)");
        Matcher matcher = pattern.matcher(rawString);
        if (matcher.matches()) {
            String sender = matcher.group(1);
            String body = matcher.group(2);
            return new RedisMessage(new RedisUser(sender), body);
        }
        return null;
    }

    /**
     * Convert the message into BaseComponent[], which can be sent to the game by player.sendMessage directly.
     * @return a BaseComponent[] instance.
     */
    default BaseComponent[] toChatInGameRepeatFormat() {
        String header = String.format("<%s> ", this.getSender().getName());
        return new ComponentBuilder()
                .append(new ComponentBuilder(header).color(ChatColor.LIGHT_PURPLE).create())
                .append(new ComponentBuilder(this.getMessage()).color(ChatColor.GRAY).create())
                .create();
    }
}
