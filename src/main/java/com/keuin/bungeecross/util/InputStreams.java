package com.keuin.bungeecross.util;

import java.io.IOException;
import java.io.InputStream;

public class InputStreams {
    public static byte[] toByteArray(InputStream stream) throws IOException {
        byte[] targetArray = new byte[stream.available()];
        stream.read(targetArray);
        return targetArray;
    }
}
