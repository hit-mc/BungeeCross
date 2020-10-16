package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.redis.RedisQueueManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class StatExecutor extends AbstractInstructionExecutor {

    private static final StatExecutor INSTANCE = new StatExecutor(
            "show the status of BungeeCross.",
            new String[0]
    );

    private static final String commandString = "stat";
    private static RedisQueueManager redisQueueManager = null;

    protected StatExecutor(String description, String[] params) {
        super(description, params);
    }

    public static StatExecutor getInstance(RedisQueueManager redisQueueManager) {
        StatExecutor.redisQueueManager = redisQueueManager;
        return INSTANCE;
    }

    @Override
    public BaseComponent[] execute() {
        if (redisQueueManager != null) {
            ComponentBuilder builder = new ComponentBuilder();
//            builder.append("Stat:\n");
            builder.append(new ComponentBuilder("Sender thread: ").color(ChatColor.WHITE).create());
            builder.append(redisQueueManager.isSenderAlive() ?
                    new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                    new ComponentBuilder("Stopped").color(ChatColor.GREEN).create()
            );
            builder.append("\n");
            builder.append(new ComponentBuilder("Receiver thread: ").color(ChatColor.WHITE).create());
            builder.append(redisQueueManager.isReceiverAlive() ?
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

}
