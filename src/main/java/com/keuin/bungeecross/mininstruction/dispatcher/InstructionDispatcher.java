package com.keuin.bungeecross.mininstruction.dispatcher;

import com.keuin.bungeecross.message.repeater.MessageRepeater;

public interface InstructionDispatcher {
    void dispatchExecution(String command, MessageRepeater echoRepeater);

    void close();
}
