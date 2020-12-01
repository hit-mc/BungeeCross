package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.context.UserContext;

public class WikiExecutor extends AbstractInstructionExecutor {
    protected WikiExecutor(String instruction, String description, String[] params) {
        super(instruction, description, params);
    }

    @Override
    public void doExecute(UserContext context, MessageRepeater echoRepeater) {

    }
}
