package com.keuin.bungeecross.config;

public interface BungeeCrossConfig {
    MessageBrokerConfig getBrokerServer();

    int getMicroApiPort();

    ProxyConfig getProxy();

    int getHistoryMessageLifeSeconds();
}
