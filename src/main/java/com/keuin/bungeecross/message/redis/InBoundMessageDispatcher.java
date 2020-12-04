package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;

/**
 * This interface is a visitor of other in-game message manager, which provides a view of repeating messages to the game.
 */
public interface InBoundMessageDispatcher {
    void repeatInboundMessage(Message message);
}