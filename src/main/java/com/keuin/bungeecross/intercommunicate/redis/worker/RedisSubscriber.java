package com.keuin.bungeecross.intercommunicate.redis.worker;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.message.RedisMessage;
import com.keuin.bungeecross.intercommunicate.user.RedisUser;
import com.keuin.bungeecross.util.InputStreams;
import com.keuin.bungeecross.util.MessageUtil;
import com.keuin.bungeecross.util.SerializedMessages;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bson.BsonBinaryReader;
import org.bson.BsonDocument;
import org.bson.BsonType;
import redis.clients.jedis.BinaryJedisPubSub;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

class RedisSubscriber extends BinaryJedisPubSub {
    private final Logger logger = Logger.getLogger(RedisSubscriber.class.getName());
    private final Consumer<Message> messageConsumer;


    public RedisSubscriber(Consumer<Message> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void onMessage(byte[] channel, byte[] message) {
        logger.info(String.format("Receive message from topic `%s`.",
                new String(channel, StandardCharsets.UTF_8)));
        var reader = new BsonBinaryReader(ByteBuffer.wrap(message));

        if (!checkKey(reader, "endpoint"))
            return;
        var endpoint = reader.readString();

        if (!checkKey(reader, "sender"))
            return;
        var sender = reader.readString();

        // read message array
        var msgBuilder = new ComponentBuilder();

        if (!checkKey(reader, "msg"))
            return;
        reader.readStartArray();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            // read sub array
            reader.readStartArray();
            // we only deal with text messages
            var messageType = reader.readInt32();
            var data = reader.readBinaryData().getData();
            try {
                msgBuilder.append(SerializedMessages.fromSerializedMessage(messageType, data));
            } catch (IOException e) {
                logger.warning("Unsupported message block type: " + messageType);
                return;
            }
            reader.readEndArray();
        }
        reader.readEndArray();


        if (!checkKey(reader, "time"))
            return;
        var time = reader.readDateTime();
        var delta = System.currentTimeMillis() - time;
        if (delta < -1000*600 || delta > 1000*60)
            logger.warning(String.format("Too far UTC timestamp %d. Potentially wrong time?", time));

        messageConsumer.accept(new RedisMessage(
                new RedisUser(sender, sender, endpoint), msgBuilder.create()
        ));
    }

    private boolean checkKey(BsonBinaryReader reader, String keyName) {
        var name = reader.readName();
        if (!keyName.equals(name)) {
            logger.warning(String.format("Illegal BSON key: %s expected, but got %s instead.",
                    keyName, name));
            return false;
        }
        return true;
    }


}
