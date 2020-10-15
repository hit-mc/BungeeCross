package com.keuin.bungeecross.mininstruction.executor;

import net.md_5.bungee.api.chat.BaseComponent;

public class ReloadExecutor implements InstructionExecutor {

    private static final ReloadExecutor INSTANCE = new ReloadExecutor();
    private static final String commandString = "reload";

    public static ReloadExecutor getInstance() {
        return INSTANCE;
    }

    @Override
    public BaseComponent[] execute() {
        return new BaseComponent[0];
    }

    @Override
    public String getCommand() {
        return commandString;
    }

    @Override
    public String getUsage() {
        return ": disable, reload all configurations, and finally enable BungeeCross.";
    }
}
