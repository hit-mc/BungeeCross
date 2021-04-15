package com.keuin.bungeecross.intercommunicate.redis.worker;

import com.keuin.bungeecross.intercommunicate.repeater.LoggableMessageSource;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;

public abstract class AbstractRedisReceiver extends Thread implements LoggableMessageSource {
    public abstract void setInstructionDispatcher(InstructionDispatcher instructionDispatcher);
}
