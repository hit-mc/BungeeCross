package com.keuin.bungeecross.intercommunicate.message;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.intercommunicate.user.RedisUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bson.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Message {

    private final long createTime;

    public Message() {
        this.createTime = Instant.now().getEpochSecond();
    }

    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the message in pure text formatting.
     * @return the pure text message.
     */
    public abstract String getMessage();

    /**
     * Get the message in minecraft rich text formatting.
     * @return the rich text message.
     */
    public abstract BaseComponent[] getRichTextMessage();

    /**
     * Get the sender of this message.
     * @return the sender.
     */
    public abstract MessageUser getSender();

    /**
     * If the message could be joined with neighbouring messages sent by the same user.
     * @return true if joinable, false if not.
     */
    public abstract boolean isJoinable();

    /**
     * Pack message into Redis format.
     *
     * @return packed string.
     */
    public String pack() {
        String SPLIT = "||";
        return String.format("%s%s%s", getSender().getName(), SPLIT, getMessage());
    }

    /**
     * Get new protocol serialized BSON data.
     * @return the BSON bytes array.
     */
    public byte[] pack2() {
        return new BsonDocument()
                .append("endpoint", new BsonString(BungeeCross.getEndpointName()))
                .append("sender", new BsonString(getSender().getName()))
                .append("msg", new BsonArray(new BsonArray(Arrays.asList(
                        new BsonInt32(0), new BsonBinary(getMessage().getBytes(StandardCharsets.UTF_8))
                ))))
                .append("time", new BsonTimestamp(getCreateTime()))
                .asBsonReader().readBinaryData().getData();
    }

    public static Message build(String message, String sender) {
        return new ConcreteMessage(sender, message);
    }

    /**
     * Construct a Message object by raw string from Redis.
     *
     * @param rawString the raw string.
     * @return a Message object. If the raw string is invalid, return null.
     */
    public static Message fromRedisRawString(String rawString) {
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
    public BaseComponent[] toChatInGameRepeatFormat() {
        String header = String.format("<%s> ", this.getSender().getName());
        return new ComponentBuilder()
                .append(new ComponentBuilder(header).color(ChatColor.LIGHT_PURPLE).create())
                .append(new ComponentBuilder(this.getMessage()).color(ChatColor.GRAY).create())
                .create();
    }
}
