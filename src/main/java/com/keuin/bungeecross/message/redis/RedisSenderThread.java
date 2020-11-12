package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.util.MessageUtil;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Send to Redis server.
 * Receive from Minecraft.
 */
class RedisSenderThread extends Thread {

    private final Logger logger = Logger.getLogger(RedisSenderThread.class.getName());

    private final AtomicBoolean enabled;
    private int failureCooldownMillis = 0; // failure cool down

    private final RedisConfig redisConfig;

    public RedisSenderThread(AtomicBoolean enabled, RedisConfig redisConfig) {
        super(RedisSenderThread.class.getName());
        this.enabled = enabled;
        this.redisConfig = redisConfig;
    }

    @Override
    public void run() {
        try {
            while (enabled.get()) { // while running
                if (failureCooldownMillis > 0)
                    Thread.sleep(failureCooldownMillis);

                try (Jedis jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false)) {
                    jedis.auth(redisConfig.getPassword());
                    while (enabled.get()) {
                        // process the queue
                        handleSendQueue(jedis); // may be interrupted
                        if (failureCooldownMillis > 0) {
                            failureCooldownMillis = 0; // success. Reset cool down time interval.
                            logger.info("Connection is recovered. Sender cool down time is set to 0.");
                        }

                        // send cool down, prevent spamming
                        Thread.sleep(redisConfig.getSendCoolDownMillis());
                    }
                    logger.info("Sender thread is stopped.");
                } catch (JedisConnectionException e) {
                    failureCooldownMillis += 1000;
                    logger.severe(String.format("Failed to connect Redis server: %s. Sender retry cool down time is set to %dms.", e, failureCooldownMillis));
                }
            }
        } catch (InterruptedException exception) {
            logger.info("Sender thread was interrupted. Quitting.");
        }
    }

    /**
     * Send a message to the Redis server. This message is not guaranteed to be sent to the remote,
     * as network exceptions may happen.
     *
     * @param jedis   the Redis server.
     * @param message the Message object.
     */
    private void sendToRedis(Jedis jedis, Message message) throws JedisConnectionException {
        int retryCounter = redisConfig.getMaxRetryTimes();
        // send outbound message
        while (retryCounter > 0) {
            try {
                pendingOutboundMessage = message;
                jedis.lpush(pushQueueName, message.pack());
                pendingOutboundMessage = null;
            } catch (JedisException e) {
                logger.warning(String.format("Failed to push message: %s. Retrying... (remaining times: %d)", e, retryCounter));
                --retryCounter;
                if (retryCounter == 0) {
                    logger.severe("Max retry times reached. Failed to send to the Redis server.");
                    throw e;
                }
                continue;
            }
            return;
        }
    }

    /**
     * Send the pending outbound message to the Redis server.
     * @param jedis the Redis server.
     */
    private void sendToRedis(Jedis jedis) throws JedisConnectionException {
        if (pendingOutboundMessage != null) {
            logger.info("Sending pending message " + pendingOutboundMessage.toString());
            sendToRedis(jedis, pendingOutboundMessage);
        }
        // otherwise, never mind.
    }

    private void handleSendQueue(Jedis jedis) throws InterruptedException, JedisConnectionException {
        sendToRedis(jedis); // process the pending message firstly.

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

                    // the next message is not joinable.
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
            sendToRedis(jedis, MessageUtil.joinMessage(joinList));

            // send the (2nd) separated message nextMessage.
            if (tailMessage != null)
                sendToRedis(jedis, tailMessage);

        } else {
            // The first message is not joinable and should be sent separately.
            sendToRedis(jedis, firstMessage);
        }
    }
}