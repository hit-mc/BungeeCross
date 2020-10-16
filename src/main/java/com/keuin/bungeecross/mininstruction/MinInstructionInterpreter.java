package com.keuin.bungeecross.mininstruction;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.message.repeater.RedisManager;
import com.keuin.bungeecross.mininstruction.executor.InstructionExecutor;
import com.keuin.bungeecross.mininstruction.executor.ListExecutor;
import com.keuin.bungeecross.mininstruction.executor.ReloadExecutor;
import com.keuin.bungeecross.mininstruction.executor.StatExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinInstructionInterpreter {

    private final RedisManager redisManager;
    private final Plugin plugin;
    private final Map<String, InstructionExecutor> instructions = new HashMap<>();

    public MinInstructionInterpreter(RedisManager redisManager, Plugin plugin) {
        this.redisManager = redisManager;
        this.plugin = plugin;
        registerInstructions();
    }

    /**
     * Bind all instructions.
     * Non-registered instructions cannot be executed.
     */
    private void registerInstructions() {
        List<InstructionExecutor> inst = Arrays.asList(
                ListExecutor.getInstance(),
                ReloadExecutor.getInstance(plugin),
                StatExecutor.getInstance(redisManager)
        );
        for (InstructionExecutor executor : inst) {
            instructions.put(executor.getCommand(), executor);
        }
    }

    public synchronized BaseComponent[] execute(String command) {
        ComponentBuilder echoBuilder = new ComponentBuilder();

        // default line
//        echoBuilder.append(new ComponentBuilder(String.format("CommandExecute{cmd=%s}\n", command)).color(ChatColor.DARK_BLUE).create());

        // execute
        if (command.isEmpty()) {
            // blank command
            echoBuilder.append(new ComponentBuilder(String.format("MinInstruction Interpreter (BungeeCross %s)\n", BungeeCross.VERSION)).color(ChatColor.DARK_GREEN).create());
            echoBuilder.append(new ComponentBuilder("Use 'help' to show usages.").create());
        } else if (command.equals("help")) {
            // help command
            echoBuilder.append(new ComponentBuilder("All loaded instructions:\n").color(ChatColor.WHITE).create());
            for (Map.Entry<String, InstructionExecutor> entry : instructions.entrySet()) {
                // "\n" cannot be replaced with "%n", for Minecraft prints CR as a visible symbol.
                echoBuilder.append(new ComponentBuilder(String.format("+ %s%s\n", entry.getKey(), entry.getValue().getUsage())).create());
            }
        } else {
            InstructionExecutor executor = instructions.get(command);
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