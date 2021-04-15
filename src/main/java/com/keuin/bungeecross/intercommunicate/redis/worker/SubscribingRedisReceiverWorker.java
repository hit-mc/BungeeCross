package com.keuin.bungeecross.intercommunicate.redis.worker;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.intercommunicate.redis.RedisConfig;
import com.keuin.bungeecross.intercommunicate.redis.RedisManager;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class SubscribingRedisReceiverWorker {
    public SubscribingRedisReceiverWorker(AtomicBoolean enableFlag, RedisConfig redisConfig,
                                          MessageRepeatable inBoundMessageDispatcher, RedisManager redisManager) {
        var subscriber = new RedisSubscriber(inBoundMessageDispatcher::repeat);
        Jedis jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false);
        jedis.psubscribe(
                subscriber,
                String.format("%s[^%s]", BungeeCross.topicPrefix, BungeeCross.getTopicId())
                        .getBytes(StandardCharsets.UTF_8)
        ); // TODO: will this block?
    }
}
