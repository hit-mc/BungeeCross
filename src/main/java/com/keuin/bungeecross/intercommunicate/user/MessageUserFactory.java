package com.keuin.bungeecross.intercommunicate.user;

import java.util.HashMap;
import java.util.Objects;

/**
 * The factory class of all message user classes.
 * This is designed to reduce redundant copies of identical instances in memory.
 */
public class MessageUserFactory {

    private static final HashMap<String, ConsoleUser> consoleUserInstances = new HashMap<>();

    public static ConsoleUser getConsoleUser(String instruction) {
        Objects.requireNonNull(instruction);
        ConsoleUser consoleUser = consoleUserInstances.get(instruction);
        if (consoleUser != null)
            return consoleUser;
        consoleUser = new ConsoleUser(instruction);
        consoleUserInstances.put(instruction, consoleUser);
        return consoleUser;
    }

}
