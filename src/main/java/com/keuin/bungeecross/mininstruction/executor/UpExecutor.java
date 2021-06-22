package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import com.keuin.bungeecross.mininstruction.executor.up.ServerStatus;
import com.keuin.bungeecross.mininstruction.executor.up.ServerStatusChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class UpExecutor extends AbstractInstructionExecutor {

    private final Plugin plugin;
    private final Logger logger = Logger.getLogger(UpExecutor.class.getName());

    public UpExecutor(@NotNull Plugin plugin) {
        super("up", "check status of all servers.", new String[0]);
        this.plugin = plugin;
    }

    @Override
    protected ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
       if (params.length != 0) {
           echo(echoRepeater, "Illegal arguments. Expecting no arguments.");
           return ExecutionResult.FAILED;
       }

        int timeoutMillis = 3000;
        ServerStatusChecker.newChecker(plugin, timeoutMillis).ping(result -> {
            var builder = new ComponentBuilder()
                    .append(new ComponentBuilder("Servers:").color(ChatColor.WHITE).create());
            for (Map.Entry<ServerInfo, ServerStatus> entry : result.entrySet()) {
                var name = entry.getKey().getName();
                var status = switch (entry.getValue()) {
                    case ONLINE -> new ComponentBuilder("[UP]").color(ChatColor.GREEN).bold(true).create();
                    case OFFLINE -> new ComponentBuilder("[DOWN]").color(ChatColor.RED).bold(true).create();
                    case TIMED_OUT -> new ComponentBuilder("[TIMED OUT]").color(ChatColor.GOLD).bold(true).create();
                };
                builder.append(new ComponentBuilder("\n" + name).color(ChatColor.WHITE).create())
                        .append(new TextComponent(": ")).append(status);
            }
            echo(echoRepeater, builder.create());
        });

        return ExecutionResult.SUCCESS;
    }
}
