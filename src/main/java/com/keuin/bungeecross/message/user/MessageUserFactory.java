package com.keuin.bungeecross.message.user;

import java.util.HashMap;

/**
 * The factory class of all message user classes.
 * This is designed to reduce redundant copies of identical instance in memory.
 */
public class MessageUserFactory {

    private static final HashMap<String, ConsoleUser> consoleUserInstances = new HashMap<>();

    public static ConsoleUser getConsoleUser(String instruction) {
        ConsoleUser consoleUser = consoleUserInstances.get(instruction);
        if (consoleUser != null)
            return consoleUser;
        consoleUser = new ConsoleUser(instruction);
        consoleUserInstances.put(instruction, consoleUser);
        return consoleUser;
    }

}
