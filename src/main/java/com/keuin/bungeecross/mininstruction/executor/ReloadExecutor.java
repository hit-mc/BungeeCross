package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
    public ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            logger.info("Reloading BungeeCross....");

            logger.info("Disabling...");
            plugin.onDisable();

            logger.info("Enabling...");
            plugin.onEnable();

            logger.info("Soft reload finished.");

            try {
                echo(echoRepeater, new ComponentBuilder(
                        String.format("Soft reload finished. (triggered by %s)", echoRepeater.toString())
                ).color(ChatColor.GREEN).create());
            } catch (Exception e) { // Prevent possible weird problems
                logger.warning("Unimportant exception was ignored: " + e);
                e.printStackTrace();
            }
        });
        return ExecutionResult.SUCCESS;
    }
}
