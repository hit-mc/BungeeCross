package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.EchoMessage;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public abstract class AbstractInstructionExecutor {

    private final String description;
    private final String[] params;
    private final String instruction;

    protected AbstractInstructionExecutor(String instruction, String description, String[] params) {
        this.instruction = instruction;
        this.description = description;
        this.params = params;
    }

    /**
     * Execute the command.
     * While executing, the output should be put in the echoRepeater.
     */
    public abstract void execute(MessageRepeater echoRepeater);

    /**
     * Get the command string.
     * @return the command string.
     */
    public final String getCommand() {
        return instruction;
    }

    /**
     * Get the usage description, which includes the instruction description and parameters.
     * This string should be put into the manual.
     * @return the usage string.
     */
    public final BaseComponent[] getUsage() {
        // TODO: Add highlight and click shortcut.
        StringBuilder paramBuilder = new StringBuilder();
        boolean isEmpty = true;
        for (String s : params) {
            paramBuilder.append(String.format("<%s> ", s));
            isEmpty = false;
        }
        if (!isEmpty)
            paramBuilder.deleteCharAt(paramBuilder.length() - 1); // remove the ending ' '
        return new BaseComponent[]{new TextComponent(String.format("%s %s: %s", getCommand(), paramBuilder.toString(), description))};
    }

    protected final void echo(MessageRepeater echoRepeater, String echo) {
        echoRepeater.repeat(new EchoMessage(getCommand(), echo));
    }

    protected final void echo(MessageRepeater echoRepeater, BaseComponent[] baseComponents) {
        echoRepeater.repeat(new EchoMessage(getCommand(), baseComponents));
    }

    protected final void echo(MessageRepeater echoRepeater, BaseComponent baseComponent) {
        echoRepeater.repeat(new EchoMessage(getCommand(), baseComponent));
    }
}
