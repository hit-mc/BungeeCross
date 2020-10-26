package com.keuin.bungeecross.mininstruction;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.message.EchoMessage;
import com.keuin.bungeecross.message.redis.RedisManager;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.executor.AbstractInstructionExecutor;
import com.keuin.bungeecross.mininstruction.executor.ListExecutor;
import com.keuin.bungeecross.mininstruction.executor.ReloadExecutor;
import com.keuin.bungeecross.mininstruction.executor.StatExecutor;
import com.keuin.bungeecross.mininstruction.executor.history.HistoryExecutor;
import com.keuin.bungeecross.mininstruction.history.ActivityProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinInstructionInterpreter {

    private final RedisManager redisManager;
    private final Plugin plugin;
    private final ActivityProvider activityProvider;
    private final ProxyServer proxyServer;

    private final Map<String, AbstractInstructionExecutor> instructions = new HashMap<>();

    public MinInstructionInterpreter(RedisManager redisManager, Plugin plugin, ActivityProvider activityProvider, ProxyServer proxyServer) {
        this.redisManager = redisManager;
        this.plugin = plugin;
        this.activityProvider = activityProvider;
        this.proxyServer = proxyServer;
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
                StatExecutor.getInstance(redisManager),
                HistoryExecutor.getInstance(activityProvider, proxyServer)
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
            echoRepeater.repeat(new EchoMessage(command, new ComponentBuilder(
                    String.format("MinInstruction Interpreter (BungeeCross %s)", BungeeCross.VERSION)
            ).color(ChatColor.DARK_GREEN).create()));
            echoRepeater.repeat(new EchoMessage(command, new ComponentBuilder(
                    String.format("Build time: %s", BungeeCross.BUILD_TIME)
            ).color(ChatColor.DARK_GREEN).create()));
            echoRepeater.repeat(new EchoMessage(command, new ComponentBuilder("Use 'help' to show usages.").create()));

        } else if (command.equals("help")) { // here goes the inline instructions
            // help command
            echoRepeater.repeat(new EchoMessage(command, new ComponentBuilder("All loaded instructions:").color(ChatColor.WHITE).create()));
            for (AbstractInstructionExecutor inst : instructions.values()) {
                // "\n" cannot be replaced with "%n", for Minecraft prints CR as a visible symbol.
                echoRepeater.repeat(new EchoMessage(inst.getCommand(), String.format("+ %s", inst.getUsage())));
            }
        } else {
            AbstractInstructionExecutor executor = instructions.get(command);
            if(executor != null) {
                executor.execute(echoRepeater);
            } else {
                echoRepeater.repeat(new EchoMessage(command, new ComponentBuilder(String.format("MinInst: Invalid command %s.", command)).color(ChatColor.RED).create()));
            }
        }
    }

}