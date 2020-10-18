package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.EchoMessage;
import com.keuin.bungeecross.message.redis.RedisManager;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public final class StatExecutor extends AbstractInstructionExecutor {

    private static final StatExecutor INSTANCE = new StatExecutor(
            "show the status of BungeeCross.",
            new String[0]
    );

    private static final String commandString = "stat";
    private static RedisManager redisManager = null;

    private StatExecutor(String description, String[] params) {
        super(description, params);
    }

    public static StatExecutor getInstance(RedisManager redisManager) {
        StatExecutor.redisManager = redisManager;
        return INSTANCE;
    }

    @Override
    public void execute(MessageRepeater echoRepeater) {
        if (redisManager != null) {
            ComponentBuilder builder = new ComponentBuilder();
//            builder.append("Stat:\n");
            builder.append(new ComponentBuilder("Sender thread: ").color(ChatColor.WHITE).create());
            builder.append(redisManager.isSenderAlive() ?
                    new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                    new ComponentBuilder("Stopped").color(ChatColor.RED).create()
            );
            echoRepeater.repeat(new EchoMessage(commandString, builder.create()));

            builder = new ComponentBuilder();

            builder.append(new ComponentBuilder("Receiver thread: ").color(ChatColor.WHITE).create());
            builder.append(redisManager.isReceiverAlive() ?
                    new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                    new ComponentBuilder("Stopped").color(ChatColor.RED).create());
            echoRepeater.repeat(new EchoMessage(commandString, builder.create()));
        } else {
            echoRepeater.repeat(new EchoMessage(commandString, new ComponentBuilder("RedisManager is not available. Cannot get stat.").color(ChatColor.RED).create()));
        }
    }

    @Override
    public String getCommand() {
        return commandString;
    }

}
