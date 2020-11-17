package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
    private final RedisSenderThread senderThread;
    private final RedisReceiverThread receiverThread;

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
        this.receiverThread = new RedisReceiverThread(
                enabled,
                redisConfig,
                inBoundMessageDispatcher,
                instructionDispatcher,
                this
        );
        // TODO: Finish refactoring
        this.senderThread = new RedisSenderThread(
                enabled,
                redisConfig.getHost(),
                redisConfig.getPort(),
                redisConfig.getPassword()
        );
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

}
