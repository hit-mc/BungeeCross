package com.keuin.bungeecross.config;

/**
 * Configuration in a class.
 * Immutable.
 */
public class BungeeCrossConfig {

    private final RedisConfig redis;
    private final int microApiPort;
    private final ProxyConfig proxy;

    public BungeeCrossConfig() {
        this.redis = new RedisConfig();
        this.microApiPort = 7000;
        this.proxy = new ProxyConfig();
    }

    public RedisConfig getRedis() {
        return redis;
    }

    public int getMicroApiPort() {
        return microApiPort;
    }

    public ProxyConfig getProxy() {
        return proxy;
    }
}
