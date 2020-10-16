package com.keuin.bungeecross.mininstruction;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.message.redis.RedisQueueManager;
import com.keuin.bungeecross.mininstruction.executor.AbstractInstructionExecutor;
import com.keuin.bungeecross.mininstruction.executor.ListExecutor;
import com.keuin.bungeecross.mininstruction.executor.ReloadExecutor;
import com.keuin.bungeecross.mininstruction.executor.StatExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinInstructionInterpreter {

    private final RedisQueueManager redisQueueManager;
    private final Plugin plugin;
    private final Map<String, AbstractInstructionExecutor> instructions = new HashMap<>();

    public MinInstructionInterpreter(RedisQueueManager redisQueueManager, Plugin plugin) {
        this.redisQueueManager = redisQueueManager;
        this.plugin = plugin;
        registerInstructions();
    }

    /**
     * Bind all instructions.
     * Non-registered instructions cannot be executed.
     */
    private void registerInstructions() {
        List<AbstractInstructionExecutor> inst = Arrays.asList(
                ListExecutor.getInstance(),
                ReloadExecutor.getInstance(plugin),
                StatExecutor.getInstance(redisQueueManager)
        );
        for (AbstractInstructionExecutor executor : inst) {
            instructions.put(executor.getCommand(), executor);
        }
    }

    /**
     * Execute a command.
     * @param command the command string.
     * @param echoRepeater where to print the echo.
     */
    public synchronized void execute(String command, MessageRepeater echoRepeater) {
//        ComponentBuilder echoBuilder = new ComponentBuilder();

        // default line
//        echoBuilder.append(new ComponentBuilder(String.format("CommandExecute{cmd=%s}\n", command)).color(ChatColor.DARK_BLUE).create());

        // execute
        if (command.isEmpty()) {
            // blank command
            echoRepeater.repeat(new ComponentBuilder(String.format("MinInstruction Interpreter (BungeeCross %s)\n", BungeeCross.VERSION)).color(ChatColor.DARK_GREEN).create());
            echoBuilder.append(new ComponentBuilder("Use 'help' to show usages.").create());
        } else if (command.equals("help")) { // here goes the inline instructions
            // help command
            echoBuilder.append(new ComponentBuilder("All loaded instructions:\n").color(ChatColor.WHITE).create());
            for (AbstractInstructionExecutor inst : instructions.values()) {
                // "\n" cannot be replaced with "%n", for Minecraft prints CR as a visible symbol.
                echoBuilder.append(new TextComponent(String.format("+ %s\n", inst.getUsage())));
            }
        } else {
            AbstractInstructionExecutor executor = instructions.get(command);
            if(executor != null) {
                BaseComponent[] echo = executor.execute();
                echoBuilder.append(echo);
            } else {
                echoBuilder.append(new ComponentBuilder(String.format("MinInst: Invalid command %s.", command)).color(ChatColor.RED).create());
            }
        }

        // return
        return echoBuilder.create();
    }

}