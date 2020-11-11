package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
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
 * This class manages the Redis connection and its inbound/outbound queue.
 * It handles the message input and output.
 */
public class RedisManager implements MessageRepeater {

    private final Logger logger = Logger.getLogger(RedisManager.class.getName());

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final RedisConfig redisConfig;

    private final String pushQueueName;
    private final String popQueueName;

//    // The counter is used for message buffering and merging, which provides a line number of each line in the merged message.
//    private final Map<MessageUser, Integer> messageCounter = new HashMap<>();
    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private final SenderThread senderThread = new SenderThread();
    private final ReceiverThread receiverThread = new ReceiverThread();

    private final int waitIntervalMillis = 100;
    private final int waitRounds = 5;

    private InstructionDispatcher instructionDispatcher;

    private final InBoundMessageDispatcher inBoundMessageDispatcher;

    private final int POP_TIMEOUT = 1;
    private final int MAX_RETRY_TIMES = 10;
    private final String redisCommandPrefix = "!";

    private final int joinWaitMillis = 125;
    private final int sendCoolDownMillis = 500;
    private final int maxJoinedMessageCount = 10;

    private Message pendingOutboundMessage = null; // should only be used in method sendToRedis

    public RedisManager(RedisConfig redisConfig, InBoundMessageDispatcher inBoundMessageDispatcher) {
        logger.info(String.format("%s created with redis info: %s", this.getClass().getName(), redisConfig.toString()));

        this.pushQueueName = redisConfig.getPushQueueName();
        this.popQueueName = redisConfig.getPopQueueName();
        this.inBoundMessageDispatcher = inBoundMessageDispatcher;
        this.redisConfig = redisConfig;
    }

    public synchronized void start() {
        enabled.set(true);
        if (!senderThread.isAlive()) {
            senderThread.start();
        }
        if (!receiverThread.isAlive()) {
            receiverThread.start();
        }
    }

    public synchronized void stop() {
        enabled.set(false);
        if(senderThread.isAlive()) {
            senderThread.interrupt();
        }
        if(receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
    }

    public boolean isSenderAlive() {
        return senderThread.isAlive();
    }

    public boolean isReceiverAlive() {
        return receiverThread.isAlive();
    }

    @Override
    public void repeat(Message message) {
        sendQueue.add(message);
//        jedis.lpush(pushQueueName, message.pack());
    }

    public void setInstructionDispatcher(InstructionDispatcher instructionDispatcher) {
        this.instructionDispatcher = instructionDispatcher;
    }

    /**
     * Send to Redis server.
     * Receive from Minecraft.
     */
    private class SenderThread extends Thread {

        private final AtomicBoolean running = new AtomicBoolean(true);
        private int failureCooldownMillis = 0; // failure cool down

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
                            Thread.sleep(sendCoolDownMillis);
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
            int retryCounter = MAX_RETRY_TIMES;
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
         *
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

    /**
     * Receive from Redis server.
     * Send to Minecraft.
     */
    private class ReceiverThread extends Thread {

        private int cooldownMillis = 0;

        @Override
        public void run() {
            try {
                while (enabled.get()) {
                    if (cooldownMillis > 0) // cool down after encountered a failure
                        Thread.sleep(cooldownMillis);

                    try (Jedis jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false)) {
                        jedis.auth(redisConfig.getPassword());

                        while (enabled.get()) {
                            // receive from Redis
                            List<String> list = null;
                            int retryCounter = MAX_RETRY_TIMES;
                            while (retryCounter > 0) {
                                try {
                                    list = jedis.brpop(POP_TIMEOUT, popQueueName);
                                } catch (JedisException e) {
                                    logger.warning(String.format("Failed to pop message: %s. Retrying... (remaining times: %d)", e, retryCounter));
                                    --retryCounter;
                                    continue;
                                }
                                if (cooldownMillis > 0) {
                                    cooldownMillis = 0; // success. Reset cool down time interval.
                                    logger.info("Connection is recovered. Receiver cool down time is set to 0.");
                                }
                                break;
                            }
                            // max retry times reached. Increment the cool down time interval.
                            if (retryCounter == 0) {
                                cooldownMillis += 1000;
                                logger.severe(String.format("Max retry times reached. Cool down time interval increased to %dms.", cooldownMillis));
                                break; // break to reconnect to the Redis
                            }

                            if (list != null) {
                                for (String rawSting : list) {
                                    Message inboundMessage = Message.fromRedisRawString(rawSting);
                                    if (inboundMessage != null) {
                                        boolean isCommand = inboundMessage.getMessage().startsWith(redisCommandPrefix);
                                        // send to Minecraft
                                        if (!isCommand) {
                                            logger.info(String.format("Received inbound message: %s (rawString=%s).", inboundMessage, rawSting));
                                            inBoundMessageDispatcher.repeatInboundMessage(inboundMessage);
                                        }

                                        // Execute instruction
                                        if (isCommand && instructionDispatcher != null) {
                                            String cmd = inboundMessage.getMessage();
                                            if (redisCommandPrefix.length() == cmd.length())
                                                cmd = "";
                                            else
                                                cmd = cmd.substring(redisCommandPrefix.length());
                                            // dispatch the command
                                            instructionDispatcher.dispatchExecution(cmd, RedisManager.this);
                                        }
                                    } else {
                                        logger.warning(String.format("Malformed inbound message: %s. Ignored.", rawSting));
                                    }
                                }
                            } else {
                                Thread.sleep(1000);
                            }
                        }

                    } catch (JedisConnectionException e) {
                        cooldownMillis += 1000;
                        logger.severe(String.format("Failed to connect Redis server: %s. Cool down time interval is set to %dms.", e, cooldownMillis));
                    }
                    // while enabled
                }
            } catch (InterruptedException ignored) {
                logger.info("Receiver thread was interrupted. Quitting.");
            }

            logger.info("Receiver thread is stopped.");
        } // void run()
    }

}
