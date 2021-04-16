package com.keuin.bungeecross.testutil;

public class TestConfig {
    public final String host;
    public final int port;
    public final String password;

    public TestConfig(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public TestConfig() {
        host = null;
        port = -1;
        password = "";
    }
}
