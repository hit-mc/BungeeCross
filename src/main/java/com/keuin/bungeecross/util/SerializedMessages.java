package com.keuin.bungeecross.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class SerializedMessages {
    private static final Logger logger = Logger.getLogger(SerializedMessages.class.getName());

    /**
     * Deserialize a message from BSON data.
     * @param type the type.
     * @param data the bytes.
     * @return the message.
     * @throws IOException when failed to decompress gzip data.
     */
    public static BaseComponent[] fromSerializedMessage(int type, byte[] data) throws IOException {
        if (type == MessageType.TEXT) {
            return new ComponentBuilder(new String(data, StandardCharsets.UTF_8)).create();
        } else if (type == MessageType.GZIP_TEXT) {
            // decompress gzip bytes, then decode to string
            var os = new GZIPInputStream(new ByteArrayInputStream(data));
            return new ComponentBuilder(new String(os.readAllBytes(), StandardCharsets.UTF_8)).create();
        } else if (type == MessageType.IMAGE) {
            return new ComponentBuilder("[图片]").color(ChatColor.GOLD).bold(true).create();
        } else {
            return new ComponentBuilder("[未知消息]").color(ChatColor.RED).bold(true).create();
        }
    }

    private static class MessageType {
        public static final int TEXT = 0;
        public static final int IMAGE = 1;
        public static final int GZIP_TEXT = 2;
    }
}
