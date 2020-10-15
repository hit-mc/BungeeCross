package com.keuin.bungeecross.message.ingame;

public interface InGameChatProcessor {
    void issue(InGameMessage message);
    void close();
}
