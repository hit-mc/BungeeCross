package com.keuin.bungeecross.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;
import java.util.regex.Pattern;

public class ProxyConfig {
    private final String address;
    private transient Proxy proxy;

    public ProxyConfig(String address) {
        this.address = address;
    }

    public ProxyConfig() {
        this.address = "";
        this.proxy = Proxy.NO_PROXY;
    }

    private static int getSchemaDefaultPort(Proxy.Type proxyType) {
        if (proxyType == Proxy.Type.HTTP)
            return 8080;
        if (proxyType == Proxy.Type.SOCKS)
            return 1080;
        return -1;
    }

    public Proxy getProxy() {
        if (proxy == null)
            buildProxy();
        return proxy;
    }

    private void buildProxy() {
        var matcher = Pattern.compile("(.+)://(.+)(:?:([0-9]+))?").matcher(address);
        Proxy.Type schema;
        switch (matcher.group(1).toLowerCase()) {
            case "http":
                schema = Proxy.Type.HTTP;
                break;
            case "socks":
                schema = Proxy.Type.SOCKS;
                break;
            default:
                schema = Proxy.Type.DIRECT;
        }
        var addr = Optional.ofNullable(matcher.group(2)).orElse("");
        var port = (matcher.groupCount() == 3)
                ? Integer.parseInt(Optional.ofNullable(matcher.group(3)).orElse("-1"))
                : getSchemaDefaultPort(schema);
        if (addr.isEmpty() || port <= 0 || port > 65535)
            proxy = Proxy.NO_PROXY;
        else
            proxy = new Proxy(schema, new InetSocketAddress(addr, port));
    }
}
