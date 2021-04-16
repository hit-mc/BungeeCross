package com.keuin.bungeecross.intercommunicate.msghandler;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.intercommunicate.user.SimpleRepeatableUser;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Handles inbound chat and command, dispatch them to corresponding handlers.
 */
public class InboundMessageHandler implements Consumer<Message> {

    private final InstructionDispatcher instructionDispatcher;
    private final MessageRepeatable chatMessageRepeater;
    private final MessageRepeatable commandEchoConsumer;
    private final String commandPrefix;
    private final String chatRelayPrefix;

    public InboundMessageHandler(InstructionDispatcher instructionDispatcher,
                                 MessageRepeatable chatMessageRepeater,
                                 MessageRepeatable commandEchoConsumer,
                                 String commandPrefix,
                                 String chatRelayPrefix) {
        this.instructionDispatcher = Objects.requireNonNull(instructionDispatcher);
        this.chatMessageRepeater = Objects.requireNonNull(chatMessageRepeater);
        this.commandEchoConsumer = Objects.requireNonNull(commandEchoConsumer);
        this.commandPrefix = Objects.requireNonNull(commandPrefix);
        this.chatRelayPrefix = Objects.requireNonNull(chatRelayPrefix);
    }

    @Override
    public void accept(Message message) {
        var msg = message.getMessage();
        if (msg.startsWith(commandPrefix)) {
            instructionDispatcher.dispatchExecution(
                    msg.substring(commandPrefix.length()),
                    new SimpleRepeatableUser(message.getSender(), commandEchoConsumer)
            );
        } else if (msg.startsWith(chatRelayPrefix)) {
            chatMessageRepeater.repeat(message);
        }
    }
}
