package com.keuin.bungeecross.intercommunicate.msghandler;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.intercommunicate.user.SimpleRepeatableUser;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;

import java.util.function.Consumer;

/**
 * Handles inbound chat and command, dispatch them to corresponding handlers.
 */
public class InboundMessageHandler implements Consumer<Message> {

    private final InstructionDispatcher instructionDispatcher;
    private final MessageRepeatable chatMessageRepeater;
    private final MessageRepeatable commandEchoConsumer;
    private final String commandPrefix;

    public InboundMessageHandler(InstructionDispatcher instructionDispatcher,
                                 MessageRepeatable chatMessageRepeater,
                                 MessageRepeatable commandEchoConsumer,
                                 String commandPrefix) {
        this.instructionDispatcher = instructionDispatcher;
        this.chatMessageRepeater = chatMessageRepeater;
        this.commandEchoConsumer = commandEchoConsumer;
        this.commandPrefix = commandPrefix;
    }

    @Override
    public void accept(Message message) {
        var msg = message.getMessage();
        if (msg.startsWith(commandPrefix)) {
            instructionDispatcher.dispatchExecution(
                    msg.substring(commandPrefix.length()),
                    new SimpleRepeatableUser(message.getSender(), commandEchoConsumer)
            );
        } else {
            chatMessageRepeater.repeat(message);
        }
    }
}
