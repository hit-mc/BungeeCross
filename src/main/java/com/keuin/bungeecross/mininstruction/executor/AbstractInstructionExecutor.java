package com.keuin.bungeecross.mininstruction.executor;

import net.md_5.bungee.api.chat.BaseComponent;

public abstract class AbstractInstructionExecutor {

    private final String description;
    private final String[] params;

    protected AbstractInstructionExecutor(String description, String[] params) {
        this.description = description;
        this.params = params;
    }

    /**
     * Execute the command.
     * @return the echo.
     */
    public abstract BaseComponent[] execute();

    /**
     * Get the command string.
     * @return the command string.
     */
    public abstract String getCommand();

    /**
     * Get the usage description, which includes the instruction description and parameters.
     * This string should be put into the manual.
     * @return the usage string.
     */
    public final String getUsage() {
        StringBuilder paramBuilder = new StringBuilder();
        for (String s : params) {
            paramBuilder.append(String.format("<%s> ", s));
        }
        paramBuilder.deleteCharAt(paramBuilder.length() - 1); // remove the ending ' '
        return String.format("%s %s: %s", getCommand(), paramBuilder.toString(), description);
    }
}
