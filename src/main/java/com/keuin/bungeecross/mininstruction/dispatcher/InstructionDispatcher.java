package com.keuin.bungeecross.mininstruction.dispatcher;

import com.keuin.bungeecross.message.user.RepeatableUser;

public interface InstructionDispatcher {
    void dispatchExecution(String command, RepeatableUser echoRepeater);

    void close();
}
