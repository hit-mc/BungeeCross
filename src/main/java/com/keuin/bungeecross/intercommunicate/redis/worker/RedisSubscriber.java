package com.keuin.bungeecross.intercommunicate.redis.worker;

import com.keuin.bungeecross.intercommunicate.message.FixedTimeMessage;
import com.keuin.bungeecross.intercommunicate.message.Message;
import redis.clients.jedis.BinaryJedisPubSub;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

class RedisSubscriber extends BinaryJedisPubSub {

    private final Logger logger = Logger.getLogger(RedisSubscriber.class.getName());
    private final Consumer<Message> messageConsumer;
    private final Function<byte[], Boolean> topicFilter;

    /**
     * Create a redis subscriber.
     *
     * @param messageConsumer where to send message to.
     * @param topicFilter     if the topic is acceptable, return true. If you want to drop this topic, return false.
     */
    public RedisSubscriber(Consumer<Message> messageConsumer, Function<byte[], Boolean> topicFilter) {
        this.messageConsumer = messageConsumer;
        this.topicFilter = topicFilter;
    }

    @Override
    public void onMessage(byte[] channel, byte[] bson) {
        var channelString = new String(channel, StandardCharsets.UTF_8);
        try {
            logger.info(String.format("Received message from topic `%s`.", channelString));

            var message = Message.unpack(bson);
            logger.info("Unpacked message: " + message);

            var messageCreateTime = message.getCreateTime();
            var timeDelta = Math.abs(Instant.now().getEpochSecond() - messageCreateTime);
            if (timeDelta > 180)
                logger.warning(String.format("Too far UTC timestamp %d. Potentially wrong time?", messageCreateTime));

            // TODO: this should not be synchronized, or it will block the jedis pool loop
            messageConsumer.accept(message);
        } catch (FixedTimeMessage.IllegalPackedMessageException e) {
            logger.warning(String.format("Cannot decode message from channel %s: %s.",
                    channelString, e.getMessage()));
            Optional.ofNullable(e.getCause())
                    .ifPresent(cause -> {
                        logger.warning("Reason: " + cause.getMessage());
                        cause.printStackTrace();
                    });
        }
    }

    @Override
    public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {
        if (topicFilter.apply(channel)) {
            onMessage(channel, message);
        }
    }
}
