package com.keuin.bungeecross.mininstruction;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.message.EchoMessage;
import com.keuin.bungeecross.message.redis.RedisManager;
import com.keuin.bungeecross.message.user.RepeatableUser;
import com.keuin.bungeecross.mininstruction.context.InterpreterContext;
import com.keuin.bungeecross.mininstruction.executor.AbstractInstructionExecutor;
import com.keuin.bungeecross.mininstruction.executor.ListExecutor;
import com.keuin.bungeecross.mininstruction.executor.ReloadExecutor;
import com.keuin.bungeecross.mininstruction.executor.StatExecutor;
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
                ListExecutor.getInstance().withContext(context),
                ReloadExecutor.getInstance(plugin).withContext(context),
                StatExecutor.getInstance(redisManager).withContext(context),
                HistoryExecutor.getInstance(activityProvider, proxyServer).withContext(context),

        ).forEach(executor -> instructions.put(executor.getCommand(), executor));
    }

    /**
     * Execute a command.
     *
     * @param command        the command string.
     * @param repeatableUser where to print the echo.
     */
    public synchronized void execute(String command, RepeatableUser repeatableUser) {
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

        if (CharacterFilter.containsInvalidCharacter(command)) {
            repeatableUser.repeat(
                    new EchoMessage(defaultName,
                            new ComponentBuilder("Command contains invalid character.")
                                    .color(ChatColor.RED)
                                    .create()
                    ));
            return;
        }

        // execute
        if (command.isEmpty()) {
            // blank command
            repeatableUser.repeat(new EchoMessage(command, new ComponentBuilder(
                    String.format("MinInstruction Interpreter (BungeeCross %s)", BungeeCross.getVersion())
            ).color(ChatColor.DARK_GREEN).create()));
            repeatableUser.repeat(new EchoMessage(command, new ComponentBuilder(
                    String.format("Build time: %s", BungeeCross.getBuildTime())
            ).color(ChatColor.DARK_GREEN).create()));
            repeatableUser.repeat(new EchoMessage(command, new ComponentBuilder("Use 'help' to show usages.").create()));

        } else if (command.equals("help")) { // here goes the inline instructions
            // help command
            repeatableUser.repeat(new EchoMessage(command, new ComponentBuilder("All loaded instructions:").color(ChatColor.WHITE).create()));
            for (AbstractInstructionExecutor inst : instructions.values()) {
                // "\n" cannot be replaced with "%n", for Minecraft prints CR as a visible symbol.
//                repeatableUser.repeat(new EchoMessage(command, String.format("+ %s", inst.getUsage())));
                repeatableUser.repeat(new EchoMessage(command,
                        (new ComponentBuilder())
//                                .append(new ComponentBuilder(inst.getCommand() + ": ").color(ChatColor.YELLOW).create())
//                                .append(new ComponentBuilder("").color(ChatColor.WHITE).create())
                                .append(inst.getUsage())
                                .create()
                ));
            }
        } else {
            AbstractInstructionExecutor executor = instructions.get(command);
            if (executor != null) {
                executor.execute(repeatableUser);
            } else {
                repeatableUser.repeat(new EchoMessage(defaultName, new ComponentBuilder(String.format("Invalid command %s.", command)).color(ChatColor.RED).create()));
            }
        }
    }

}