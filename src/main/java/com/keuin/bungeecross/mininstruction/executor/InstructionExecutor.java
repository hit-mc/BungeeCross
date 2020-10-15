package com.keuin.bungeecross.mininstruction.executor;

import net.md_5.bungee.api.chat.BaseComponent;

public interface InstructionExecutor {
    /**
     * Execute the command.
     * @return the echo.
     */
    BaseComponent[] execute();

    /**
     * Get the command string.
     * @return the command string.
     */
    String getCommand();

    /**
     * Get the usage, which should be put into the manual.
     * @return the usage string.
     */
    String getUsage();
}
