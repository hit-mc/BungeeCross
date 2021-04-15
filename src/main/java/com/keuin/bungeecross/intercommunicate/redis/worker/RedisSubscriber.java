package com.keuin.bungeecross.intercommunicate.redis.worker;

import com.keuin.bungeecross.intercommunicate.message.Message;
import redis.clients.jedis.BinaryJedisPubSub;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.logging.Logger;

class RedisSubscriber extends BinaryJedisPubSub {
    private final Logger logger = Logger.getLogger(RedisSubscriber.class.getName());
    private final Consumer<Message> messageConsumer;


    public RedisSubscriber(Consumer<Message> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    @Override
    public void onMessage(byte[] channel, byte[] bson) {
        try {
            logger.info(String.format("Receive message from topic `%s`.",
                    new String(channel, StandardCharsets.UTF_8)));

            var message = Message.unpack(bson);

            var messageCreateTime = message.getCreateTime();
            var timeDelta = Math.abs(System.currentTimeMillis() - messageCreateTime);
            if (timeDelta > 180 * 1000)
                logger.warning(String.format("Too far UTC timestamp %d. Potentially wrong time?", messageCreateTime));

            messageConsumer.accept(message);
        } catch (Message.IllegalPackedMessageException e) {
            logger.warning(e.getLocalizedMessage());
        }
    }


}
