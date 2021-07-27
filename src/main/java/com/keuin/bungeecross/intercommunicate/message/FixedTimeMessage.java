package com.keuin.bungeecross.intercommunicate.message;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bson.*;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.io.BasicOutputBuffer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public abstract class FixedTimeMessage implements Message {

    private final long createTime;
//    private static final Logger logger = Logger.getLogger("AbstractMessageStaticContext");

    public FixedTimeMessage() {
        this.createTime = System.currentTimeMillis();
    }

    public FixedTimeMessage(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get new protocol serialized BSON data.
     *
     * @return the BSON bytes array. Do not modify it.
     */
    @Override
    public byte[] pack(String endpoint) {
        Objects.requireNonNull(endpoint);
        var doc = new BsonDocument()
                .append("endpoint", new BsonString(endpoint))
                .append("sender", new BsonString(getSender().toString()))
                .append("msg", new BsonArray(Collections.singletonList(
                        new BsonArray(Arrays.asList(
                                new BsonInt32(0), new BsonBinary(getMessage().getBytes(StandardCharsets.UTF_8))
                        ))
                )))
                .append("time", new BsonInt64(getCreateTime()));
        var codec = new BsonDocumentCodec();
        try (var writeBuffer = new BasicOutputBuffer();
             var writer = new BsonBinaryWriter(writeBuffer)) {
            codec.encode(writer, doc, EncoderContext.builder().build());
            return Arrays.copyOf(writeBuffer.getInternalBuffer(), writeBuffer.getSize());
        }

    }

    /**
     * Convert the message into BaseComponent[], which can be sent to the game by player.sendMessage directly.
     *
     * @return a BaseComponent[] instance.
     */
    @Override
    public BaseComponent[] toChatInGameRepeatFormat() {
        String header = String.format("<%s> ", this.getSender());
        return new ComponentBuilder()
                .append(new ComponentBuilder(header).color(ChatColor.LIGHT_PURPLE).create())
                .append(new ComponentBuilder(this.getMessage()).color(ChatColor.GRAY).create())
                .create();
    }

}
