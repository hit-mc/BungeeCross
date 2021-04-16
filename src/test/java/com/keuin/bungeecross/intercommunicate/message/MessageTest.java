package com.keuin.bungeecross.intercommunicate.message;

import org.junit.Test;

import static org.junit.Assert.*;

public class MessageTest {

    @org.junit.Test
    public void fromRedisRawString() {
        Message message = Message.fromRedisRawString("sender||message");
        assertNotNull(message);
        assertEquals(message.getMessage(), "message");
        assertEquals(message.getSender().getName(), "sender@QQ");
    }

    @Test
    public void testMessagePackBSON() throws Message.IllegalPackedMessageException {
        var endpoint = "test-endpoint";
        var sender = "message_sender";
        var message = Message.build("message", sender);
        var bson = message.pack2(endpoint);
        var unpacked = Message.unpack(bson);
        assertEquals(message.getMessage(), unpacked.getMessage());
        assertTrue("packed message contains incorrect sender string",
                unpacked.getSender().getName().contains(sender));
        assertTrue("packed message contains incorrect sender string",
                unpacked.getSender().getName().contains(endpoint));
    }
}