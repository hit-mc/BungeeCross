package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.message.user.RedisUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.regex.*;

public interface Message {
    String getMessage();
    MessageUser getSender();

    /**
     * Pack message into Redis format.
     * @return packed string.
     */
    default String pack() {
        String SPLIT = "||";
        return String.format("%s%s%s", getSender().getName(), SPLIT, getMessage());
    }

    /**
     * Construct a Message object by raw string from Redis.
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
    default BaseComponent[] toRepeatedChatMessage() {
        String header = String.format("<%s> ", this.getSender().getName());
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(new ComponentBuilder(header).color(ChatColor.LIGHT_PURPLE).create());
        builder.append(new ComponentBuilder(this.getMessage()).color(ChatColor.GRAY).create());
        return builder.create();
    }
}
