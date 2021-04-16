package com.keuin.bungeecross.intercommunicate.redis;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.msghandler.InboundMessageHandler;
import com.keuin.bungeecross.intercommunicate.redis.worker.AbstractRedisReceiver;
import com.keuin.bungeecross.intercommunicate.redis.worker.LegacyRedisReceiverWorker;
import com.keuin.bungeecross.intercommunicate.redis.worker.RedisSenderWorker;
import com.keuin.bungeecross.intercommunicate.redis.worker.SubscribingRedisReceiverWorker;
import com.keuin.bungeecross.intercommunicate.repeater.LoggableMessageSource;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import com.keuin.bungeecross.recentmsg.HistoryMessageLogger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * This class manages the Redis connection and its inbound/outbound queue.
 * It handles the message input and output.
 */
public class RedisManager implements com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable, LoggableMessageSource {

    private final Logger logger = Logger.getLogger(RedisManager.class.getName());

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final InboundMessageHandler inboundMessageHandler;
    private final InstructionDispatcher instructionDispatcher;

    private final RedisSenderWorker senderWorker;
    private final AbstractRedisReceiver receiverWorker;

    public RedisManager(RedisConfig redisConfig, MessageRepeatable inBoundMessageDispatcher, InstructionDispatcher instructionDispatcher) {
        logger.info(String.format("%s created with redis info: %s", this.getClass().getName(), redisConfig.toString()));

        this.instructionDispatcher = Objects.requireNonNull(instructionDispatcher);
        this.senderWorker = new RedisSenderWorker(redisConfig, enabled);
        this.inboundMessageHandler = new InboundMessageHandler(
                instructionDispatcher, inBoundMessageDispatcher,
                this.senderWorker, redisConfig.getRedisCommandPrefix(),
                redisConfig.getChatRelayPrefix());

        if (redisConfig.isLegacyProtocol()) {
            var legacyReceiver = new LegacyRedisReceiverWorker(
                    enabled, redisConfig, inBoundMessageDispatcher, this);
            legacyReceiver.setInstructionDispatcher(instructionDispatcher);
            this.receiverWorker = legacyReceiver;
        } else {
            this.receiverWorker = new SubscribingRedisReceiverWorker(redisConfig, inboundMessageHandler);
        }
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

    @Override
    public void registerHistoryLogger(HistoryMessageLogger historyMessageLogger) {
        Objects.requireNonNull(historyMessageLogger);
        logger.info("Registering history msg logger " + historyMessageLogger + ".");
        receiverWorker.registerHistoryLogger(historyMessageLogger);
    }
}
