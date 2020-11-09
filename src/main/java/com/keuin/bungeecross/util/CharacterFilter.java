package com.keuin.bungeecross.util;

import java.util.regex.Pattern;

/**
 * Filter invalid characters
 */
public class CharacterFilter {

    private static final Pattern pattern = Pattern.compile("[^0-9a-zA-Z ()\\[\\]{}_+\\-*/^,.\\u2E80-\\u9FFF]");

    /**
     * Check if the given command contains at least one invalid character.
     * @param string the string to be checked.
     * @return true if invalid, false if valid.
     */
    public static boolean containsInvalidCharacter(String string) {
        return pattern.matcher(string).find();
    }
}
