package com.keuin.bungeecross.message.redis.worker;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.RedisManager;
import com.keuin.bungeecross.util.MessageUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Send to Redis server.
 * Receive from Minecraft.
 */
public class RedisSenderWorker extends Thread {

    private final Logger logger = Logger.getLogger(RedisSenderWorker.class.getName());

    private final RedisManager redisManager;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private int failureCooldownMillis = 0; // failure cool down

    public RedisSenderWorker(RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    @Override
    public void run() {
        try {
            while (redisManager.enabled.get()) { // while running
                if (failureCooldownMillis > 0)
                    Thread.sleep(failureCooldownMillis);
                if (failureCooldownMillis < 0)
                    failureCooldownMillis = 1;

                try (Jedis jedis = new Jedis(redisManager.redisConfig.getHost(), redisManager.redisConfig.getPort(), false)) {
                    jedis.auth(redisManager.redisConfig.getPassword());
                    while (redisManager.enabled.get()) {
                        // process the queue
                        handleSendQueue(jedis); // may be interrupted
                        if (failureCooldownMillis > 0) {
                            failureCooldownMillis = 0; // success. Reset cool down time interval.
                            redisManager.logger.info("Connection recovered. Sender cool down time is set to 0.");
                        }

                        // send cool down, prevent spamming
                        Thread.sleep(redisManager.sendCoolDownMillis);
                    }
                    redisManager.logger.info("Sender thread is stopped.");
                } catch (JedisConnectionException e) {
                    failureCooldownMillis += 1000;
                    redisManager.logger.severe(String.format("Failed to connect Redis server: %s. Sender retry cool down time is set to %dms.", e, failureCooldownMillis));
                }
            }
        } catch (InterruptedException exception) {
            redisManager.logger.info("Sender thread was interrupted. Quitting.");
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
        int retryCounter = redisManager.MAX_RETRY_TIMES;
        // send outbound message
        while (retryCounter > 0) {
            try {
                redisManager.pendingOutboundMessage = message;
                jedis.lpush(redisManager.pushQueueName, message.pack());
                redisManager.pendingOutboundMessage = null;
            } catch (JedisException e) {
                redisManager.logger.warning(String.format("Failed to push message: %s. Retrying... (remaining times: %d)", e, retryCounter));
                --retryCounter;
                if (retryCounter == 0) {
                    redisManager.logger.severe("Max retry times reached. Failed to send to the Redis server.");
                    throw e;
                }
                continue;
            }
            return;
        }
    }

    /**
     * Send the pending outbound message to the Redis server.
     *
     * @param jedis the Redis server.
     */
    private void sendToRedis(Jedis jedis) throws JedisConnectionException {
        if (redisManager.pendingOutboundMessage != null) {
            redisManager.logger.info("Sending pending message " + redisManager.pendingOutboundMessage.toString());
            sendToRedis(jedis, redisManager.pendingOutboundMessage);
        }
        // otherwise, never mind.
    }

    private void handleSendQueue(Jedis jedis) throws InterruptedException, JedisConnectionException {
        sendToRedis(jedis); // process the pending message firstly.

        Message firstMessage = redisManager.sendQueue.take();
        if (redisManager.maxJoinedMessageCount > 1 && firstMessage.isJoinable()) {

            List<Message> joinList = new ArrayList<>(); // messages should be joined before sent (always contains the first message).
            joinList.add(firstMessage);

            Message tailMessage = null; // the last message that should be sent separately (if has).

            // get next messages with max count maxJoinedMessageCount.
            for (int i = 0; i < redisManager.maxJoinedMessageCount - 1; ++i) {
                Message nextMessage = redisManager.sendQueue.poll(redisManager.joinWaitMillis, TimeUnit.MILLISECONDS);
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
            sendToRedis(jedis, MessageUtil.joinMessages(joinList));

            // send the (2nd) separated message nextMessage.
            if (tailMessage != null)
                sendToRedis(jedis, tailMessage);

        } else {
            // The first message is not joinable and should be sent separately.
            sendToRedis(jedis, firstMessage);
        }
    }
}
