package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.redis.RedisManager;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public final class StatExecutor extends AbstractInstructionExecutor {

    private static final StatExecutor INSTANCE = new StatExecutor(
            "show the status of BungeeCross.",
            new String[0]
    );

    private static RedisManager redisManager = null;

    private StatExecutor(String description, String[] params) {
        super("stat", description, params);
    }

    public static StatExecutor getInstance(RedisManager redisManager) {
        StatExecutor.redisManager = redisManager;
        return INSTANCE;
    }

    @Override
    public ExecutionResult doExecute(UserContext context, MessageRepeater echoRepeater, String[] params) {
        if (redisManager != null) {
            ComponentBuilder builder = new ComponentBuilder();
//            builder.append("Stat:\n");
            builder.append(new ComponentBuilder("Sender thread: ").color(ChatColor.WHITE).create());
            builder.append(redisManager.isSenderAlive() ?
                    new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                    new ComponentBuilder("Stopped").color(ChatColor.RED).create()
            );
            echo(echoRepeater, builder.create());

            builder = new ComponentBuilder();

            builder.append(new ComponentBuilder("Receiver thread: ").color(ChatColor.WHITE).create());
            builder.append(redisManager.isReceiverAlive() ?
                    new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                    new ComponentBuilder("Stopped").color(ChatColor.RED).create());
            echo(echoRepeater, builder.create());
        } else {
            echo(echoRepeater, new ComponentBuilder("RedisManager is not available. Cannot get stat.").color(ChatColor.RED).create());
        }
        return ExecutionResult.SUCCESS;
    }

}
