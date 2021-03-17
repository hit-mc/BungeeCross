package com.keuin.bungeecross.mininstruction;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.intercommunicate.message.EchoMessage;
import com.keuin.bungeecross.intercommunicate.redis.RedisManager;
import com.keuin.bungeecross.intercommunicate.user.RepeatableUser;
import com.keuin.bungeecross.mininstruction.context.InterpreterContext;
import com.keuin.bungeecross.mininstruction.executor.*;
import com.keuin.bungeecross.mininstruction.executor.history.HistoryExecutor;
import com.keuin.bungeecross.mininstruction.history.ActivityProvider;
import com.keuin.bungeecross.util.CharacterFilter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MinInstructionInterpreter {

    private static final int MAX_COMMAND_LINE_LENGTH = 20;
    private static final String defaultName = "MinInst";


    private final RedisManager redisManager;
    private final Plugin plugin;
    private final ActivityProvider activityProvider;
    private final ProxyServer proxyServer;

    private final Map<String, AbstractInstructionExecutor> instructions = new HashMap<>();
    private final InterpreterContext context = new InterpreterContext(); // providing persistent context across executions

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
        Arrays.asList(
                new ListExecutor().withContext(context),
                new ReloadExecutor(plugin).withContext(context),
                new StatExecutor(redisManager).withContext(context),
                new HistoryExecutor(activityProvider, proxyServer).withContext(context),
                new WikiExecutor().withContext(context)
        ).forEach(executor -> instructions.put(executor.getCommand(), executor));
    }

    /**
     * Execute a command.
     *
     * @param command        the command string.
     * @param repeatableUser where to print the echo.
     */
    public synchronized void execute(String command, RepeatableUser repeatableUser) {
        try {
            Thread.sleep(100); // a simple mitigation: correct command echo place
        } catch (InterruptedException ignored) {
        }

        //        ComponentBuilder echoBuilder = new ComponentBuilder();

        // default line
//        echoBuilder.append(new ComponentBuilder(String.format("CommandExecute{cmd=%s}\n", command)).color(ChatColor.DARK_BLUE).create());

        // security check

        if (command.length() > MAX_COMMAND_LINE_LENGTH) {
            repeatableUser.repeat(
                    new EchoMessage(defaultName, new ComponentBuilder("Command is too long.")
                            .color(ChatColor.RED)
                            .create())
            );
            return;
        }

        String[] slices = command.split(" "); // slices[1, 2, ..., n-1] are parameters
        String[] params = new String[0];
        if (slices.length > 1) {
            params = new String[slices.length - 1];
            System.arraycopy(slices, 1, params, 0, slices.length - 1);
        }
        final String instruction = slices.length > 0 ? slices[0] : "";

        if (CharacterFilter.containsInvalidCharacter(instruction)) {
            repeatableUser.repeat(
                    new EchoMessage(defaultName,
                            new ComponentBuilder("Command contains invalid character.")
                                    .color(ChatColor.RED)
                                    .create()
                    ));
            return;
        }

        // execute
        if (instruction.isEmpty()) {
            // blank command
            repeatableUser.repeat(new EchoMessage(instruction, new ComponentBuilder(
                    String.format("MinInstruction Interpreter (BungeeCross %s)", BungeeCross.getVersion())
            ).color(ChatColor.DARK_GREEN).create()));
            repeatableUser.repeat(new EchoMessage(instruction, new ComponentBuilder(
                    String.format("Build time: %s", BungeeCross.getBuildTime())
            ).color(ChatColor.DARK_GREEN).create()));
            repeatableUser.repeat(new EchoMessage(instruction, new ComponentBuilder(
                    "Use 'help' to show usages."
            ).create()));

        } else if (instruction.equals("help")) { // here goes the inline instructions
            // help command
            repeatableUser.repeat(new EchoMessage(instruction, new ComponentBuilder(
                    "All loaded instructions:"
            ).color(ChatColor.WHITE).create()));
            for (AbstractInstructionExecutor inst : instructions.values()) {
                // "\n" cannot be replaced with "%n", for Minecraft prints CR as a visible symbol.
//                repeatableUser.repeat(new EchoMessage(command, String.format("+ %s", inst.getUsage())));
                repeatableUser.repeat(new EchoMessage(instruction,
                        (new ComponentBuilder())
//                                .append(new ComponentBuilder(inst.getCommand() + ": ").color(ChatColor.YELLOW).create())
//                                .append(new ComponentBuilder("").color(ChatColor.WHITE).create())
                                .append(inst.getUsage())
                                .create()
                ));
            }
        } else {
            AbstractInstructionExecutor executor = instructions.get(instruction);
            if (executor != null) {
                executor.execute(repeatableUser, params);
            } else {
                repeatableUser.repeat(new EchoMessage(defaultName, new ComponentBuilder(
                        "Invalid command."
                ).color(ChatColor.RED).create()));
            }
        }
    }

}