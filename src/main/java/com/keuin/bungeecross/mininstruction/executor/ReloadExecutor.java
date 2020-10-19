package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.repeater.MessageRepeater;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.Logger;

public final class ReloadExecutor extends AbstractInstructionExecutor {

    private static final ReloadExecutor INSTANCE = new ReloadExecutor(
            "disable, reload all configurations, and finally enable BungeeCross.",
            new String[0]
    );

    private static Plugin plugin;
    private final Logger logger = Logger.getLogger(ReloadExecutor.class.getName());

    private ReloadExecutor(String description, String[] params) {
        super("reload", description, params);
    }

    public static ReloadExecutor getInstance(Plugin plugin) {
        ReloadExecutor.plugin = plugin;
        return INSTANCE;
    }

    @Override
    public void execute(MessageRepeater echoRepeater) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            logger.info("Reloading BungeeCross....");

            logger.info("Disabling...");
            plugin.onDisable();

            logger.info("Enabling...");
            plugin.onEnable();

            logger.info("Soft reload finished.");
        });
    }
}
