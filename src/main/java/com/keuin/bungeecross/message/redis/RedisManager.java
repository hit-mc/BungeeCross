package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.message.repeater.RedisUserRepeater;
import com.keuin.bungeecross.message.user.SimpleRepeatableUser;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import com.keuin.bungeecross.util.MessageUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
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

    private InstructionDispatcher instructionDispatcher;

    private final InBoundMessageDispatcher inBoundMessageDispatcher;

    private final int POP_TIMEOUT = 1;
    private final String redisCommandPrefix = "!";

    private final int joinWaitMillis = 125;
    private final int sendCoolDownMillis = 500;
    private final int maxJoinedMessageCount = 10;

//    private Message pendingOutboundMessage = null; // should only be used in method sendToRedis

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
        if (senderThread.isAlive()) {
            senderThread.interrupt();
        }
        if (receiverThread.isAlive()) {
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

        private Jedis jedis = null;

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

                        // receive from Redis
                        List<String> list;
                        try {
                            while (enabled.get()) {
                                list = jedis.brpop(POP_TIMEOUT, popQueueName);
                                if (list == null) {
                                    Thread.sleep(1000);
                                    continue;
                                }

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
                                            instructionDispatcher.dispatchExecution(
                                                    cmd,
                                                    new SimpleRepeatableUser(
                                                            inboundMessage.getSender(),
                                                            new RedisUserRepeater(
                                                                    RedisManager.this,
                                                                    inboundMessage.getSender()
                                                            )
                                                    )
                                            );
                                        }
                                    } else {
                                        logger.warning(String.format("Malformed inbound message: %s. Ignored.", rawSting));
                                    }
                                }

                                if (cooldownMillis > 0) {
                                    cooldownMillis = 0; // success. Reset cool down time interval.
                                    logger.info("Connection recovered. Set receiver cool down to 0.");
                                }
                            }
                        } catch (JedisException e) {
                            cooldownMillis += 1000;
                            logger.warning(String.format("Failed to pop message: %s. Retrying... (wait for %dms)", e, cooldownMillis));
                        }

                    } catch (JedisConnectionException e) {
                        cooldownMillis += 1000;
                        logger.severe(String.format("Failed to connect Redis server: %s. Set cool down time interval to %dms.", e, cooldownMillis));
                    }
                } // while enabled
            } catch (InterruptedException ignored) {
                logger.info("Receiver thread was interrupted. Quitting.");
            }
            logger.info("Receiver thread stopped.");
        } // void run()
    }

}
