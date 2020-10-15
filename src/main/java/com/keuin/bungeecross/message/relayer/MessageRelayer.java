package com.keuin.bungeecross.message.relayer;

import com.keuin.bungeecross.message.Message;

/**
 * Relay message to specific target.
 */
public interface MessageRelayer {
    void relay(Message message);
}
