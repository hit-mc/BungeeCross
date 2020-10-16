package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.InGameMessage;

public interface InGameChatProcessor {
    void issue(InGameMessage message);
    void close();
}
