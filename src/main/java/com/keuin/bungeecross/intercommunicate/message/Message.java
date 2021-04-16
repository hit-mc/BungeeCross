package com.keuin.bungeecross.intercommunicate.message;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.intercommunicate.user.RedisUser;
import com.keuin.bungeecross.util.SerializedMessages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bson.*;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Message {

    private final long createTime;
//    private static final Logger logger = Logger.getLogger("AbstractMessageStaticContext");

    public Message() {
        this.createTime = Instant.now().getEpochSecond();
    }

    public Message(long createTime) {
        this.createTime = createTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    /**
     * Unserialize BSON packed message.
     *
     * @param bson the BSON data.
     * @return the message object.
     * @throws IllegalPackedMessageException if the message data is not valid.
     */
    public static Message unpack(byte[] bson) throws IllegalPackedMessageException {
        try {
            var reader = new BsonBinaryReader(ByteBuffer.wrap(bson));

            reader.readStartDocument();

            if (isBsonKeyInvalid(reader, "endpoint"))
                throw new IllegalPackedMessageException("endpoint");
            var endpoint = reader.readString();

            if (isBsonKeyInvalid(reader, "sender"))
                throw new IllegalPackedMessageException("sender");
            var sender = reader.readString();

            // read message array
            var msgBuilder = new ComponentBuilder();

            if (isBsonKeyInvalid(reader, "msg"))
                throw new IllegalPackedMessageException("msg");
            reader.readStartArray();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                // read sub array
//                reader.readStartArray();
//                var i = reader.readInt32();
//                System.out.println("Index: " + i);
                // we only deal with text messages
                var messageType = reader.readInt32();
                var data = reader.readBinaryData().getData();
                try {
                    msgBuilder.append(SerializedMessages.fromSerializedMessage(messageType, data));
                } catch (IOException e) {
                    throw new IllegalPackedMessageException("Unsupported message block type: " + messageType, e);
                }
//                reader.readEndArray();
            }
            reader.readEndArray();

            if (isBsonKeyInvalid(reader, "time"))
                throw new IllegalPackedMessageException("time");
            var createTime = reader.readDateTime();

            reader.readEndDocument();

            // TODO: refactor, create a new class `UnpackedRedisMessage`,
            //  which has a more powerful internal representation
            return new RedisMessage(new RedisUser(sender, sender, endpoint), msgBuilder.create(), createTime);
        } catch (BsonInvalidOperationException e) {
            throw new IllegalPackedMessageException("invalid packed message data", e);
        }
    }

    /**
     * Read in one BSON key and check if it is invalid.
     *
     * @param reader  the BSON reader.
     * @param keyName expected key.
     * @return if the key name equals to what is expected.
     */
    private static boolean isBsonKeyInvalid(BsonBinaryReader reader, String keyName) {
        var name = reader.readName();
        return !keyName.equals(name);
    }

    /**
     * Get the message in pure text formatting.
     *
     * @return the pure text message.
     */
    public abstract String getMessage();

    /**
     * Get the message in minecraft rich text formatting.
     *
     * @return the rich text message.
     */
    public abstract BaseComponent[] getRichTextMessage();

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
     * Get the sender of this message.
     *
     * @return the sender.
     */
    public abstract MessageUser getSender();

    /**
     * If the message could be joined with neighbouring messages sent by the same user.
     *
     * @return true if joinable, false if not.
     */
    public abstract boolean isJoinable();

    /**
     * Get new protocol serialized BSON data.
     *
     * @return the BSON bytes array. Do not modify it.
     */
    public byte[] pack2(String endpoint) {
        Objects.requireNonNull(endpoint);
        var doc = new BsonDocument()
                .append("endpoint", new BsonString(endpoint))
                .append("sender", new BsonString(getSender().getName()))
                .append("msg", new BsonArray(new BsonArray(Arrays.asList(
                        new BsonInt32(0), new BsonBinary(getMessage().getBytes(StandardCharsets.UTF_8))
                ))))
                .append("time", new BsonDateTime(getCreateTime()));
        var codec = new BsonDocumentCodec();
        var writeBuffer = new BasicOutputBuffer();
        var writer = new BsonBinaryWriter(writeBuffer);
        codec.encode(writer, doc, EncoderContext.builder().build());
        return writeBuffer.getInternalBuffer();
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
     *
     * @return a BaseComponent[] instance.
     */
    public BaseComponent[] toChatInGameRepeatFormat() {
        String header = String.format("<%s> ", this.getSender().getName());
        return new ComponentBuilder()
                .append(new ComponentBuilder(header).color(ChatColor.LIGHT_PURPLE).create())
                .append(new ComponentBuilder(this.getMessage()).color(ChatColor.GRAY).create())
                .create();
    }

    public static class IllegalPackedMessageException extends Exception {
        public IllegalPackedMessageException() {
            super();
        }

        public IllegalPackedMessageException(String missingPropertyName) {
            super(String.format("missing BSON property `%s`.", missingPropertyName));
        }

        public IllegalPackedMessageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
