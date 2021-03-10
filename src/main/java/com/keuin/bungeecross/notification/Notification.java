package com.keuin.bungeecross.notification;

import com.keuin.bungeecross.intercommunicate.message.Message;

import java.util.function.Consumer;

public interface Notification {
    Notification notifyIfNeeded(Consumer<Message> receiver);
}
