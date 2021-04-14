package com.keuin.bungeecross.util;

import java.io.IOException;
import java.io.InputStream;

public class InputStreams {
    public static byte[] toByteArray(InputStream stream) throws IOException {
        var remaining = stream.available();
        byte[] targetArray = new byte[remaining];
        while (remaining > 0)
            remaining -= stream.read(targetArray);
        return targetArray;
    }
}
