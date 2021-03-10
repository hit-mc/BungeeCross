package com.keuin.bungeecross.testutil;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * For unit test only.
 */
public class TestableRepeatable implements MessageRepeatable {

    private final List<Message> messageList = new ArrayList<>();

    @Override
    public void repeat(Message message) {
        messageList.add(message);
    }

    public List<Message> getMessageList() {
        return Collections.unmodifiableList(messageList);
    }
}
