package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;

/**
 * This class is a visitor of other in-game message manager, which provides an interface repeating message to the game.
 */
public interface InBoundMessageDispatcher {
    void repeatInboundMessage(Message message);
}