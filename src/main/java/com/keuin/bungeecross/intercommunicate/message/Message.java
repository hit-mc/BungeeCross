package com.keuin.bungeecross.intercommunicate.message;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.intercommunicate.user.RedisUser;
import com.keuin.bungeecross.util.SerializedMessages;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bson.BsonBinaryReader;
import org.bson.BsonType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Message {
    /**
     * Deserialize BSON packed message.
     *
     * @param bson the BSON data.
     * @return the message object.
     * @throws IllegalPackedMessageException if the message data is not valid.
     */
    static Message unpack(byte[] bson) throws IllegalPackedMessageException {
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
                reader.readStartArray();
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
                reader.readEndArray();
            }
            reader.readEndArray();

            if (isBsonKeyInvalid(reader, "time"))
                throw new IllegalPackedMessageException("time");
            var createTime = reader.readInt64();

            reader.readEndDocument();

            // TODO: refactor, create a new class `UnpackedRedisMessage`,
            //  which has a more powerful internal representation
            return new RedisMessage(new RedisUser(sender, sender, endpoint), msgBuilder.create(), createTime);
        } catch (Exception e) {
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
    static boolean isBsonKeyInvalid(BsonBinaryReader reader, String keyName) {
        var name = reader.readName();
        return !keyName.equals(name);
    }

    static Message build(@NotNull String message, @NotNull String sender) {
        return new ConcreteMessage(sender, message);
    }

    /**
     * Construct a Message object by raw string from Redis.
     *
     * @param rawString the raw string.
     * @return a Message object. If the raw string is invalid, return null.
     */
    @Deprecated
    static @Nullable Message fromRedisRawString(String rawString) {
        Pattern pattern = Pattern.compile("([^|]*)\\|\\|([\\s\\S]*)");
        Matcher matcher = pattern.matcher(rawString);
        if (matcher.matches()) {
            String sender = matcher.group(1);
            String body = matcher.group(2);
            return new RedisMessage(new RedisUser(sender), body);
        }
        return null;
    }

    long getCreateTime();

    /**
     * Get the message in pure text formatting.
     *
     * @return the pure text message.
     */
    String getMessage();

    /**
     * Get the message in minecraft rich text formatting.
     *
     * @return the rich text message.
     */
    BaseComponent[] getRichTextMessage();

    /**
     * Get the sender of this message.
     *
     * @return the sender.
     */
    MessageUser getSender();

    String pack();

    /**
     * If the message could be joined with neighbouring messages sent by the same user.
     *
     * @return true if join-able, false if not.
     */
    boolean ifCanBeJoined();

    byte[] pack2(String endpoint);

    BaseComponent[] toChatInGameRepeatFormat();

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
