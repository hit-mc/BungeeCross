package com.keuin.bungeecross.intercommunicate.repeater;

import com.keuin.bungeecross.intercommunicate.message.Message;

/**
 * Repeat message to specific target.
 */
public interface MessageRepeatable {

    /**
     * Repeat given message to the destination.
     * @param message the message. Concrete formatting is defined by the implementation.
     */
    void repeat(Message message);

//    /**
//     * Enable or disable the buffer of this repeater.
//     * Buffer could improve the performance of expensive repeating, such as the repeat over the Internet.
//     * If the repeater does not support buffering, this method will be of no effect.
//     * @param enabled if the buffer will be enabled. If set to False, the buffer will be flushed immediately.
//     * @return the old buffer setting.
//     */
//    boolean setBuffer(boolean enabled);
//
//    boolean isBufferEnabled();

//    /**
//     * Repeat given message to the destination.
//     * @param message the message. Concrete formatting is defined by the implementation.
//     * @param buffered if the message will be saved into the buffer, until
//     */
//    void repeat(Message message, boolean buffered);

//    /**
//     * If the repeater is buffered, flush the output buffer.
//     * Otherwise this method should be of no effect.
//     */
//    void flush();
}
