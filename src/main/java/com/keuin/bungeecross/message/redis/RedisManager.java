package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.worker.RedisReceiverWorker;
import com.keuin.bungeecross.message.redis.worker.RedisSenderWorker;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * This class manages the Redis connection and its inbound/outbound queue.
 * It handles the message input and output.
 */
public class RedisManager implements MessageRepeater {

    private final Logger logger = Logger.getLogger(RedisManager.class.getName());

    private final AtomicBoolean enabled = new AtomicBoolean(true);

    private final RedisSenderWorker senderWorker;
    private final RedisReceiverWorker receiverWorker;

    public RedisManager(RedisConfig redisConfig, InBoundMessageDispatcher inBoundMessageDispatcher) {
        logger.info(String.format("%s created with redis info: %s", this.getClass().getName(), redisConfig.toString()));

        this.senderWorker = new RedisSenderWorker(redisConfig, enabled);
        this.receiverWorker = new RedisReceiverWorker(
                enabled,
                redisConfig,
                inBoundMessageDispatcher,
                this
        );
    }

    public synchronized void start() {
        logger.info("RedisManager is starting...");
        enabled.set(true);
        if (!senderWorker.isAlive()) {
            logger.info("Start send worker");
            senderWorker.start();
        }
        if (!receiverWorker.isAlive()) {
            logger.info("Start receive worker");
            receiverWorker.start();
        }
    }

    public synchronized void stop() {
        logger.info("RedisManager is stopping...");
        enabled.set(false);
        if (senderWorker.isAlive()) {
            logger.info("Interrupt send worker");
            senderWorker.interrupt();
        }
        if (receiverWorker.isAlive()) {
            logger.info("Interrupt receive worker");
            receiverWorker.interrupt();
        }
    }

    public boolean isSenderAlive() {
        return senderWorker.isAlive();
    }

    public boolean isReceiverAlive() {
        return receiverWorker.isAlive();
    }

    @Override
    public void repeat(Message message) {
        senderWorker.repeat(message);
    }

    public void setInstructionDispatcher(InstructionDispatcher instructionDispatcher) {
        this.receiverWorker.setInstructionDispatcher(instructionDispatcher);
    }

}
