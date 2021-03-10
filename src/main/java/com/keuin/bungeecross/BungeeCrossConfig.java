package com.keuin.bungeecross;

import com.keuin.bungeecross.intercommunicate.redis.RedisConfig;

import java.util.Objects;

/**
 * Configuration in a class.
 * Immutable.
 */
public class BungeeCrossConfig {

    private final RedisConfig redis;
    private final int microApiPort;

    public BungeeCrossConfig(RedisConfig redis, int microApiPort) {
        this.redis = Objects.requireNonNull(redis);
        this.microApiPort = microApiPort;
    }

    public RedisConfig getRedis() {
        return redis;
    }

    public int getMicroApiPort() {
        return microApiPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BungeeCrossConfig that = (BungeeCrossConfig) o;
        return microApiPort == that.microApiPort &&
                redis.equals(that.redis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redis, microApiPort);
    }
}
