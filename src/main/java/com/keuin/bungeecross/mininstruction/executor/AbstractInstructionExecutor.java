package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.EchoMessage;
import com.keuin.bungeecross.message.repeater.MessageRepeatable;
import com.keuin.bungeecross.message.user.RepeatableUser;
import com.keuin.bungeecross.mininstruction.context.InterpreterContext;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Optional;

public abstract class AbstractInstructionExecutor {

    private final String description;
    private final String[] params;
    private final String instruction;
    private InterpreterContext context = new InterpreterContext();

    protected AbstractInstructionExecutor(String instruction, String description, String[] params) {
        this.instruction = instruction;
        this.description = description;
        this.params = params;
    }

    /**
     * Execute the command.
     * While executing, the output would be repeat to the command sender.
     */
    public final void execute(RepeatableUser commandSender, String[] params) {
        Optional.ofNullable(doExecute(context.getUserContext(commandSender), commandSender, params))
                .filter(r -> !r.isSuccess()).ifPresent(r -> echo(commandSender, r.getBaseComponents()));
    }

    /**
     * Execute the command.
     * While executing, the output should be put in the echoRepeater.
     */
    protected abstract ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params);

    /**
     * Get the command string.
     *
     * @return the command string.
     */
    public final String getCommand() {
        return instruction;
    }

    /**
     * Set executor's context, return this executor.
     *
     * @param interpreterContext the new context.
     * @return the executor instance.
     */
    public AbstractInstructionExecutor withContext(InterpreterContext interpreterContext) {
        context = interpreterContext;
        return this;
    }

    /**
     * Get the usage description, which includes the instruction description and parameters.
     * This string should be put into the manual.
     *
     * @return the usage string.
     */
    public final BaseComponent[] getUsage() {
        // TODO: Add highlight and click shortcut.

        // create params
        ComponentBuilder paramBuilder = new ComponentBuilder();
        boolean isEmpty = true;
        for (String s : params) {
            TextComponent component = new TextComponent(String.format("<%s>", s));
            component.setColor(ChatColor.GREEN);
            paramBuilder.append(component);
            paramBuilder.append(new TextComponent(" "));
            isEmpty = false;
        }

        ComponentBuilder builder = new ComponentBuilder();
        // command name
        builder.append(new ComponentBuilder(getCommand()).color(ChatColor.YELLOW).create());
        builder.append(new TextComponent(" "));

        // params
        if (!isEmpty) {
            paramBuilder.removeComponent(paramBuilder.getCursor());
            builder.append(paramBuilder.create());
        }

        // description
        builder.append(new ComponentBuilder(": " + description).color(ChatColor.WHITE).create());
        return builder.create();
    }

    protected final void echo(MessageRepeatable echoRepeater, String echo) {
        echoRepeater.repeat(new EchoMessage(getCommand(), echo));
    }

    protected final void echo(MessageRepeatable echoRepeater, BaseComponent[] baseComponents) {
        echoRepeater.repeat(new EchoMessage(getCommand(), baseComponents));
    }

    protected final void echo(MessageRepeatable echoRepeater, BaseComponent baseComponent) {
        echoRepeater.repeat(new EchoMessage(getCommand(), baseComponent));
    }

    protected enum ExecutionResult {
        SUCCESS("Command has been executed successfully.", ChatColor.GREEN, true),
        ILLEGAL_PARAM("Illegal parameter.",  ChatColor.RED, false),
        FAILED("Failed to execute.", ChatColor.RED, false);

        private final BaseComponent[] baseComponents;
        private final boolean isSuccess;
        ExecutionResult(String message, ChatColor color, boolean isSuccess) {
            baseComponents = (new ComponentBuilder(message)).color(color).create();
            this.isSuccess = isSuccess;
        }

        public BaseComponent[] getBaseComponents() {
            return baseComponents;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }
}
