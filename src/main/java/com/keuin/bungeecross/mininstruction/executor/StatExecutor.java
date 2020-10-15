package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.relayer.RedisManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class StatExecutor implements InstructionExecutor {

    private static final StatExecutor INSTANCE = new StatExecutor();
    private static final String commandString = "stat";
    private static RedisManager redisManager = null;

    public static StatExecutor getInstance(RedisManager redisManager) {
        StatExecutor.redisManager = redisManager;
        return INSTANCE;
    }

    private StatExecutor() {
    }

    @Override
    public BaseComponent[] execute() {
        if (redisManager != null) {
            ComponentBuilder builder = new ComponentBuilder();
//            builder.append("Stat:\n");
            builder.append(new ComponentBuilder("Sender thread: ").color(ChatColor.WHITE).create());
            builder.append(redisManager.isSenderAlive() ?
                    new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                    new ComponentBuilder("Stopped").color(ChatColor.GREEN).create()
            );
            builder.append("\n");
            builder.append(new ComponentBuilder("Receiver thread: ").color(ChatColor.WHITE).create());
            builder.append(redisManager.isReceiverAlive() ?
                    new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                    new ComponentBuilder("Stopped").color(ChatColor.GREEN).create()
            );
            return builder.create();
        } else {
            return new ComponentBuilder("RedisManager is not available. Cannot get stat.").color(ChatColor.RED).create();
        }
    }

    @Override
    public String getCommand() {
        return commandString;
    }

    @Override
    public String getUsage() {
        return ": show the status of BungeeCross.";
    }
}
