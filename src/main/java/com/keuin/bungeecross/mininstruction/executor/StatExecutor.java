package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.intercommunicate.redis.RedisManager;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.Objects;

public final class StatExecutor extends AbstractInstructionExecutor {
    private final RedisManager redisManager;

    public StatExecutor(RedisManager redisManager) {
        super("stat", "show the status of BungeeCross.", new String[0]);
        this.redisManager = Objects.requireNonNull(redisManager);
    }

    @Override
    public ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
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
