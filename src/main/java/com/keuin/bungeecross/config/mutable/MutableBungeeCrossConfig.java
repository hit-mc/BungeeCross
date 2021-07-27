package com.keuin.bungeecross.config.mutable;

import com.keuin.bungeecross.config.BungeeCrossConfig;
import com.keuin.bungeecross.config.MessageBrokerConfig;
import com.keuin.bungeecross.config.ProxyConfig;

/**
 * Root config.
 */
public class MutableBungeeCrossConfig implements BungeeCrossConfig {

    private final MutableMessageBrokerConfig brokerServer = new MutableMessageBrokerConfig();
    private int microApiPort = 7000;
    private int historyMessageLifeSeconds = 600;
    private final MutableProxyConfig proxy = new MutableProxyConfig();

    public synchronized void copyFrom(BungeeCrossConfig from) {
        brokerServer.copyFrom(from.getBrokerServer());
        microApiPort = from.getMicroApiPort();
        historyMessageLifeSeconds = from.getHistoryMessageLifeSeconds();
        proxy.copyFrom(from.getProxy());
    }

    @Override public synchronized MessageBrokerConfig getBrokerServer() {
        return brokerServer;
    }

    @Override public synchronized int getMicroApiPort() {
        return microApiPort;
    }

    @Override public synchronized ProxyConfig getProxy() {
        return proxy;
    }

    @Override
    public int getHistoryMessageLifeSeconds() {
        return historyMessageLifeSeconds;
    }
}
