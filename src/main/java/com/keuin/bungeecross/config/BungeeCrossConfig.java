package com.keuin.bungeecross.config;

import com.keuin.bungeecross.config.mutable.MutableRedisConfig;

public interface BungeeCrossConfig {
    RedisConfig getRedis();

    int getMicroApiPort();

    ProxyConfig getProxy();
}
