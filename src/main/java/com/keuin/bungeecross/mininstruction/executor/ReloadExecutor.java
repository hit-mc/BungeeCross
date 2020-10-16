package com.keuin.bungeecross.mininstruction.executor;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Logger;

public class ReloadExecutor extends AbstractInstructionExecutor {

    private static final ReloadExecutor INSTANCE = new ReloadExecutor(
            "disable, reload all configurations, and finally enable BungeeCross.",
            new String[0]
    );

    private static final String commandString = "reload";
    private static Plugin plugin;
    private final Logger logger = Logger.getLogger(ReloadExecutor.class.getName());

    protected ReloadExecutor(String description, String[] params) {
        super(description, params);
    }

    public static ReloadExecutor getInstance(Plugin plugin) {
        ReloadExecutor.plugin = plugin;
        return INSTANCE;
    }

    @Override
    public BaseComponent[] execute() {
        plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
            @Override
            public void run() {
                logger.info("Reloading BungeeCross....");

                logger.info("Disabling...");
                plugin.onDisable();

                logger.info("Enabling...");
                plugin.onEnable();

                logger.info("Soft reload finished.");
            }
        });
        return new BaseComponent[0];
    }

    @Override
    public String getCommand() {
        return commandString;
    }

}
