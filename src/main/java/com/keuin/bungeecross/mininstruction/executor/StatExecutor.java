package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.intercommunicate.redis.BrokerManager;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.Objects;

public final class StatExecutor extends AbstractInstructionExecutor {
    private final BrokerManager brokerManager;

    public StatExecutor(BrokerManager brokerManager) {
        super("stat", "show the status of BungeeCross.", new String[0]);
        this.brokerManager = Objects.requireNonNull(brokerManager);
    }

    @Override
    public ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
        if (brokerManager == null) {
            echo(echoRepeater, new ComponentBuilder("RedisManager is not available. Cannot get stat.")
                    .color(ChatColor.RED).create());
            return ExecutionResult.FAILED;
        }

        var builder = new ComponentBuilder();

        // sender
        builder.append(new ComponentBuilder("Sender thread: ").color(ChatColor.WHITE).create());
        builder.append(brokerManager.isSenderAlive() ?
                new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                new ComponentBuilder("Stopped").color(ChatColor.RED).create()
        );
        echo(echoRepeater, builder.create());

        // receiver
        builder = new ComponentBuilder();
        builder.append(new ComponentBuilder("Receiver thread: ").color(ChatColor.WHITE).create());
        builder.append(brokerManager.isReceiverAlive() ?
                new ComponentBuilder("Alive").color(ChatColor.GREEN).create() :
                new ComponentBuilder("Stopped").color(ChatColor.RED).create());
        echo(echoRepeater, builder.create());
        return ExecutionResult.SUCCESS;
    }

}
