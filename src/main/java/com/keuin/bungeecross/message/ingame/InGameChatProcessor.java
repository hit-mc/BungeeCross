package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.InGameMessage;
import com.keuin.bungeecross.message.repeater.LoggableMessageSource;

public interface InGameChatProcessor extends LoggableMessageSource {
    void issue(InGameMessage message);
    void close();
}
