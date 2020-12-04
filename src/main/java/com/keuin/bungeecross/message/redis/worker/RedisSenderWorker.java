package com.keuin.bungeecross.message.redis.worker;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.RedisConfig;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.util.MessageUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Send to Redis server.
 * Receive from Minecraft.
 */
public class RedisSenderWorker extends Thread implements MessageRepeater {

    private final Logger logger = Logger.getLogger(RedisSenderWorker.class.getName());

    private final RedisConfig redisConfig;
    private final AtomicBoolean enabled;
    private final String pushQueueName;
    private final int joinWaitMillis = 125;
    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private final int sendCoolDownMillis = 500;
    private final int maxJoinedMessageCount = 10;
    private Jedis jedis = null;

    public RedisSenderWorker(RedisConfig redisConfig, AtomicBoolean enabled) {
        this.redisConfig = redisConfig;
        this.enabled = enabled;
        this.pushQueueName = redisConfig.getPushQueueName();
    }


    /**
     * Try to release old jedis instance and reconnect.
     * May failed silently.
     */
    private void resetJedis() {
        try {
            Optional.ofNullable(jedis).ifPresent(Jedis::close);
            jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false);
            jedis.auth(redisConfig.getPassword());
        } catch (JedisException e) {
            logger.severe(String.format("Failed to connect/disconnect: %s", e));
        }
    }

    @Override
    public void run() {
        try {
            while (enabled.get()) { // while running

                resetJedis();
                while (enabled.get()) {
                    // process the queue
                    handleSendQueue(); // may be interrupted

                    // send cool down, prevent spamming
                    Thread.sleep(sendCoolDownMillis);

//                        logger.info("Sender thread is stopped.");
                }

            }
        } catch (InterruptedException exception) {
            logger.info("Sender thread was interrupted. Quitting.");
        }
    }

    /**
     * Send a message to the Redis server. The message is guaranteed to be sent to the remote.
     * (otherwise `enabled` is set to false or interrupted)
     *
     * @param message the message to be sent.
     */
    private void sendToRedis(Message message) throws InterruptedException {
        int failureCoolDownMillis = 0; // failure cool down
        // send outbound message
        while (enabled.get()) {
            try {
//                        pendingOutboundMessage = message;
                jedis.lpush(pushQueueName, message.pack());
//                        pendingOutboundMessage = null;
                logger.info("Message was sent to Redis server successfully.");
                return;
            } catch (JedisException e) {
                logger.warning(String.format("Failed to push message: %s.", e));
            }
            failureCoolDownMillis += 1000;
            logger.info(String.format("Sender is reconnecting to Redis server... (wait for %dms)", failureCoolDownMillis));
            Thread.sleep(failureCoolDownMillis);
            // failed. reset Jedis
            resetJedis();
        }
    }

//        /**
//         * Send the pending outbound message to the Redis server.
//         */
//        private void processPendingMessage() throws InterruptedException {
//            if (pendingOutboundMessage != null) {
//                logger.info("Sending pending message " + pendingOutboundMessage.toString());
//                sendToRedis(pendingOutboundMessage);
//            }
//            // otherwise, never mind.
//        }

    private void handleSendQueue() throws InterruptedException {
//            processPendingMessage(); // process the pending message firstly.

        Message firstMessage = sendQueue.take();
        if (maxJoinedMessageCount > 1 && firstMessage.isJoinable()) {

            List<Message> joinList = new ArrayList<>(); // messages should be joined before sent (always contains the first message).
            joinList.add(firstMessage);

            Message tailMessage = null; // the last message that should be sent separately (if has).

            // get next messages with max count maxJoinedMessageCount.
            for (int i = 0; i < maxJoinedMessageCount - 1; ++i) {
                Message nextMessage = sendQueue.poll(joinWaitMillis, TimeUnit.MILLISECONDS);
                if (nextMessage == null) {
                    // no more messages
                    // just send the joinList as a single message.
                    break;
                } else if (!nextMessage.isJoinable() || !nextMessage.getSender().equals(firstMessage.getSender())) {

                    // the next message is not join-able.
                    // they have to be sent separately.
                    // 1. join the joinList and send them as a single message.
                    // 2. send the nextMessage as a single standalone message.
                    tailMessage = nextMessage;
                    break;
                } else {
                    joinList.add(nextMessage); // this message should be joined
                }
            }

            // send the (1st) joined message.
            sendToRedis(MessageUtil.joinMessages(joinList));

            // send the (2nd) separated message nextMessage.
            if (tailMessage != null)
                sendToRedis(tailMessage);

        } else {
            // The first message is not joinable and should be sent separately.
            sendToRedis(firstMessage);
        }
    }

    @Override
    public void repeat(Message message) {
        sendQueue.add(message);
    }
}