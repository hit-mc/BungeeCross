package com.keuin.bungeecross.testutil;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * For unit test only.
 */
public class TestableRepeatable implements MessageRepeatable {

    private final List<Message> messageList = new ArrayList<>();
    private final Consumer<Message> callback;

    public TestableRepeatable() {
        this.callback = null;
    }

    public TestableRepeatable(Consumer<Message> callback) {
        this.callback = callback;
    }

    @Override
    public void repeat(Message message) {
        messageList.add(message);
        Optional.ofNullable(this.callback).ifPresent(cb -> cb.accept(message));
    }

    public List<Message> getMessageList() {
        return Collections.unmodifiableList(messageList);
    }
}
