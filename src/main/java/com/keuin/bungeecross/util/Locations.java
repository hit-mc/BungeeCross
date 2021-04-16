package com.keuin.bungeecross.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Locations {
    /**
     * Add a nullable location prefix to a non-null string.
     *
     * @param location the nullable location.
     * @param obj      the not-null string to be prepended, e.g. a username or a sub-location.
     * @return the result string.
     */
    public static String locate(@Nullable String location, @NotNull String obj) {
        Objects.requireNonNull(obj);
        if (location != null)
            return String.format("%s@%s", obj, location);
        else
            return obj;
    }
}
