package com.keuin.bungeecross.config;

public interface BungeeCrossConfig {
    MessageBrokerConfig getBroker();

    int getMicroApiPort();

    ProxyConfig getProxy();

    int getHistoryMessageLifeSeconds();
}
