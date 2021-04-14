package com.keuin.bungeecross.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;

public class InputStreamsTest {
    @Test
    public void testInputStream() throws IOException {
        testArray(new byte[]{});
        testArray(new byte[]{1});
        testArray(new byte[]{1, 2});
        testArray(new byte[]{1, 2, 3, 4, 5});
    }

    private void testArray(byte[] array) throws IOException {
        var stream = new ByteArrayInputStream(array);
        assertArrayEquals(array, InputStreams.toByteArray(stream));
    }
}