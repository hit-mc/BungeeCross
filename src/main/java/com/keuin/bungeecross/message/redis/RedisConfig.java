package com.keuin.bungeecross.message.redis;

/**
 * Redis configuration section
 * Immutable.
 */
public class RedisConfig {
    private final String host;
    private final int port;
    private final String password;
    private final String pushQueueName;
    private final String popQueueName;

    public RedisConfig(String host, int port, String password, String pushQueueName, String popQueueName) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.pushQueueName = pushQueueName;
        this.popQueueName = popQueueName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getPushQueueName() {
        return pushQueueName;
    }

    public String getPopQueueName() {
        return popQueueName;
    }

    @Override
    public String toString() {
        return "RedisConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", password='" + password + '\'' +
                ", pushQueueName='" + pushQueueName + '\'' +
                ", popQueueName='" + popQueueName + '\'' +
                '}';
    }
}
