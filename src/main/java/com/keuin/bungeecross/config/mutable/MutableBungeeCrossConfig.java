package com.keuin.bungeecross.config.mutable;

import com.keuin.bungeecross.config.BungeeCrossConfig;
import com.keuin.bungeecross.config.ProxyConfig;
import com.keuin.bungeecross.config.RedisConfig;

/**
 * Root config.
 */
public class MutableBungeeCrossConfig implements BungeeCrossConfig {

    private final MutableRedisConfig redis = new MutableRedisConfig();
    private int microApiPort = 7000;
    private final MutableProxyConfig proxy = new MutableProxyConfig();

    public synchronized void copyFrom(BungeeCrossConfig from) {
        this.redis.copyFrom(from.getRedis());
        this.microApiPort = from.getMicroApiPort();
        this.proxy.copyFrom(from.getProxy());
    }

    @Override public synchronized RedisConfig getRedis() {
        return redis;
    }

    @Override public synchronized int getMicroApiPort() {
        return microApiPort;
    }

    @Override public synchronized ProxyConfig getProxy() {
        return proxy;
    }
}
