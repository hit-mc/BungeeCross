package com.keuin.bungeecross;

import com.keuin.bungeecross.message.redis.RedisConfig;

/**
 * Configuration in a class.
 * Immutable.
 */
public class BungeeCrossConfig {
    private final RedisConfig redis;

    public BungeeCrossConfig(RedisConfig redis) {
        this.redis = redis;
    }

    public RedisConfig getRedis() {
        return redis;
    }
}
