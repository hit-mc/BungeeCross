package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;

/**
 * This class is a visitor of other in-game message manager, which provides a inbound message apply interface.
 */
public interface InBoundMessageDispatcher {
    void repeatInboundMessage(Message message);
}
