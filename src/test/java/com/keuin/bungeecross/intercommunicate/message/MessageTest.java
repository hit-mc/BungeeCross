package com.keuin.bungeecross.intercommunicate.message;

import static org.junit.Assert.*;

public class MessageTest {

    @org.junit.Test
    public void fromRedisRawString() {
        Message message = Message.fromRedisRawString("sender||message");
        assertNotNull(message);
        assertEquals(message.getMessage(), "message");
        assertEquals(message.getSender().getName(), "sender@QQ");
    }
}