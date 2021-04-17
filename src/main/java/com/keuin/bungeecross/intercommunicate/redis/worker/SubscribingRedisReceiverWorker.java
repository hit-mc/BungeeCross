package com.keuin.bungeecross.intercommunicate.redis.worker;

import com.keuin.bungeecross.config.RedisConfig;
import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.recentmsg.HistoryMessageLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SubscribingRedisReceiverWorker extends AbstractRedisReceiver {

    private final Logger logger = Logger.getLogger(SubscribingRedisReceiverWorker.class.getName());
    private final Set<HistoryMessageLogger> loggers = Collections.newSetFromMap(new IdentityHashMap<>());
    private final RedisSubscriber subscriber;
    private final RedisConfig redisConfig;

    public SubscribingRedisReceiverWorker(RedisConfig redisConfig,
                                          Consumer<Message> inboundMessageHandler) {
        this.subscriber = new RedisSubscriber(inboundMessageHandler,
                topic -> !Objects.equals(new String(topic, StandardCharsets.UTF_8),
                        redisConfig.getTopicPrefix() + redisConfig.getTopicId()));
        this.redisConfig = redisConfig;
    }

    @Override
    public void run() {
        logger.info("connecting...");
        try (var jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false)) {
            jedis.auth(redisConfig.getPassword());
//            var pattern = String.format("%s[^(%s)]*", redisConfig.getTopicPrefix(), redisConfig.getTopicId());
            var pattern = String.format("%s*", redisConfig.getTopicPrefix());
            var patternBytes = pattern
                    .getBytes(StandardCharsets.UTF_8);
            logger.info(String.format("start subscribing to %s...", pattern));
            jedis.psubscribe(subscriber, patternBytes);
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
}
