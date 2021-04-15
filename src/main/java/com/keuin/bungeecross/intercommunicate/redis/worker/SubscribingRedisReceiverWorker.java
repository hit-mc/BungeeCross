package com.keuin.bungeecross.intercommunicate.redis.worker;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.intercommunicate.redis.RedisConfig;
import com.keuin.bungeecross.intercommunicate.redis.RedisManager;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import com.keuin.bungeecross.recentmsg.HistoryMessageLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SubscribingRedisReceiverWorker extends AbstractRedisReceiver {

    private final Logger logger = Logger.getLogger(SubscribingRedisReceiverWorker.class.getName());
    private final Set<HistoryMessageLogger> loggers = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Jedis jedis;
    private final RedisSubscriber subscriber;
    private InstructionDispatcher instructionDispatcher;

    public SubscribingRedisReceiverWorker(AtomicBoolean enableFlag, RedisConfig redisConfig,
                                          MessageRepeatable inBoundMessageDispatcher, RedisManager redisManager) {
        this.subscriber = new RedisSubscriber(inBoundMessageDispatcher::repeat);
        this.jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false);
    }

    @Override
    public void run() {
        try {
            jedis.psubscribe(
                    subscriber,
                    String.format("%s[^%s]", BungeeCross.topicPrefix, BungeeCross.getTopicId())
                            .getBytes(StandardCharsets.UTF_8)
            );
        } catch (JedisException e) {
            e.printStackTrace();
        } finally {
            logger.severe("Receiver thread is quitting...");
        }
    }

    @Override
    public void registerHistoryLogger(HistoryMessageLogger historyMessageLogger) {
        this.loggers.add(historyMessageLogger);
    }

    @Override
    public void setInstructionDispatcher(InstructionDispatcher instructionDispatcher) {
        this.instructionDispatcher = instructionDispatcher;
    }
}
