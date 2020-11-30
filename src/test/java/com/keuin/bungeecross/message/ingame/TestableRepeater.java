package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.repeater.MessageRepeater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * For unit test only.
 */
public class TestableRepeater implements MessageRepeater {

    private final List<Message> messageList = new ArrayList<>();

    @Override
    public void repeat(Message message) {
        messageList.add(message);
    }

    public List<Message> getMessageList() {
        return Collections.unmodifiableList(messageList);
    }
}