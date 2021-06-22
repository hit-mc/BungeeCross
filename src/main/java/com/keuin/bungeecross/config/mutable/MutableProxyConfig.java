package com.keuin.bungeecross.config.mutable;

import com.keuin.bungeecross.config.ProxyConfig;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;
import java.util.regex.Pattern;

public class MutableProxyConfig implements ProxyConfig {
    private String address = "";
    private transient Proxy proxy = Proxy.NO_PROXY;

    public MutableProxyConfig(String address) {
        this.address = address;
    }

    public MutableProxyConfig() {}

    public synchronized void copyFrom(ProxyConfig from) {
        this.proxy = from.getProxy();
    }

    @Override public synchronized Proxy getProxy() {
        if (proxy == null)
            buildProxy();
        return proxy;
    }

    private void buildProxy() {
        var matcher = Pattern.compile("(\\S+)://([^:]+)(?::([0-9]+))?").matcher(address);
        if (!matcher.matches())
            throw new IllegalArgumentException("Illegal proxy address");
        Proxy.Type schema = switch (matcher.group(1).toLowerCase()) {
            case "http" -> Proxy.Type.HTTP;
            case "socks" -> Proxy.Type.SOCKS;
            default -> Proxy.Type.DIRECT;
        };
        var addr = Optional.ofNullable(matcher.group(2)).orElse("");
        var port = (matcher.groupCount() == 3)
                ? Integer.parseInt(Optional.ofNullable(matcher.group(3)).orElse("-1"))
                : ProxyConfig.getSchemaDefaultPort(schema);
        if (addr.isEmpty() || port <= 0 || port > 65535)
            proxy = Proxy.NO_PROXY;
        else
            proxy = new Proxy(schema, new InetSocketAddress(addr, port));
    }
}
