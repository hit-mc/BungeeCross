package com.keuin.bungeecross.testutil;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class TestableServer implements Server {

    private final TestableServerInfo serverInfo;

    public TestableServer() {
        serverInfo = new TestableServerInfo();
    }

    public TestableServer(String name) {
        serverInfo = new TestableServerInfo(name);
    }

    public static TestableServer createSkeleton() {
        return new TestableServer();
    }

    public TestableServerInfo getTestableServerInfo() {
        return serverInfo;
    }

    @Override
    public ServerInfo getInfo() {
        return serverInfo;
    }

    @Override
    public void sendData(String s, byte[] bytes) {

    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public SocketAddress getSocketAddress() {
        return null;
    }

    @Override
    public void disconnect(String s) {

    }

    @Override
    public void disconnect(BaseComponent... baseComponents) {

    }

    @Override
    public void disconnect(BaseComponent baseComponent) {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Unsafe unsafe() {
        return null;
    }
}
