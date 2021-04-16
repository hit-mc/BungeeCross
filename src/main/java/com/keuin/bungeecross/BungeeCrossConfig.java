package com.keuin.bungeecross;

import com.keuin.bungeecross.intercommunicate.redis.RedisConfig;

/**
 * Configuration in a class.
 * Immutable.
 */
public class BungeeCrossConfig {

    private final RedisConfig redis;
    private final int microApiPort;

    public BungeeCrossConfig() {
        this.redis = new RedisConfig();
        this.microApiPort = 7000;
    }

    public RedisConfig getRedis() {
        return redis;
    }

    public int getMicroApiPort() {
        return microApiPort;
    }

}
