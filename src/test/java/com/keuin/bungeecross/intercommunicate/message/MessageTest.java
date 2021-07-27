package com.keuin.bungeecross.intercommunicate.message;

import org.bson.*;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageTest {

    @Test
    public void testMessagePackBSON() throws FixedTimeMessage.IllegalPackedMessageException, IOException {
        var endpoint = "test-endpoint";
        var sender = "message_sender";
        var message = Message.build("message", sender);
        var bson = message.pack(endpoint);
        var unpacked = Message.unpack(bson);
        assertEquals(message.getMessage(), unpacked.getMessage());
        assertEquals(sender, unpacked.getSender().getName());
        assertEquals(endpoint, unpacked.getSender().getLocation());
    }

    @Test
    public void testMultipleMessageBlocksUnpacking() throws FixedTimeMessage.IllegalPackedMessageException {
        var endpoint = "test-endpoint";
        var sender = "test-sender";
        var messages = new String[]{"message1", "message2", "msg3"};
        var createTime = Instant.now().toEpochMilli();
        var doc = new BsonDocument()
                .append("endpoint", new BsonString(endpoint))
                .append("sender", new BsonString(sender))
                .append("msg", new BsonArray(Arrays.stream(messages).map(str ->
                        new BsonArray(Arrays.asList(
                                new BsonInt32(0), new BsonBinary(str.getBytes(StandardCharsets.UTF_8))
                        ))
                ).collect(Collectors.toList())))
                .append("time", new BsonInt64(createTime));
        var codec = new BsonDocumentCodec();
        var writeBuffer = new BasicOutputBuffer();
        var writer = new BsonBinaryWriter(writeBuffer);
        codec.encode(writer, doc, EncoderContext.builder().build());

        // pack and unpack
        var bson = writeBuffer.getInternalBuffer();
        var unpacked = Message.unpack(bson);
        for (String s : messages) {
            assertTrue(unpacked.getMessage().contains(s));
        }
        assertEquals(String.join("", Arrays.asList(messages)), unpacked.getMessage());
    }
}