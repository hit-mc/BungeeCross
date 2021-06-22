package com.keuin.bungeecross.config;

import java.net.Proxy;

public interface ProxyConfig {
    static int getSchemaDefaultPort(Proxy.Type proxyType) {
        if (proxyType == Proxy.Type.HTTP)
            return 8080;
        if (proxyType == Proxy.Type.SOCKS)
            return 1080;
        return -1;
    }

    Proxy getProxy();
}
