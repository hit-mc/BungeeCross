package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.Message;

/**
 * Repeat message to specific target.
 */
public interface MessageRepeater {

    /**
     * Repeat given message to the destination.
     * @param message the message. Concrete formatting is defined by the implementation.
     */
    void repeat(Message message);

    /**
     * If the repeater is buffered, flush the output buffer.
     * Otherwise this method should be of no effect.
     */
    void flush();
}
