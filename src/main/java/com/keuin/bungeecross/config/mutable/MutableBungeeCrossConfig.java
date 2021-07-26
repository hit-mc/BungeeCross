package com.keuin.bungeecross.config.mutable;

import com.keuin.bungeecross.config.BungeeCrossConfig;
import com.keuin.bungeecross.config.ProxyConfig;
import com.keuin.bungeecross.config.MessageBrokerConfig;

/**
 * Root config.
 */
public class MutableBungeeCrossConfig implements BungeeCrossConfig {

    private final MutableMessageBrokerConfig brokerServer = new MutableMessageBrokerConfig();
    private int microApiPort = 7000;
    private final MutableProxyConfig proxy = new MutableProxyConfig();

    public synchronized void copyFrom(BungeeCrossConfig from) {
        this.brokerServer.copyFrom(from.getBroker());
        this.microApiPort = from.getMicroApiPort();
        this.proxy.copyFrom(from.getProxy());
    }

    @Override public synchronized MessageBrokerConfig getBroker() {
        return brokerServer;
    }

    @Override public synchronized int getMicroApiPort() {
        return microApiPort;
    }

    @Override public synchronized ProxyConfig getProxy() {
        return proxy;
    }
}
