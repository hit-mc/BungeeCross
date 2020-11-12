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
    private final int maxRetryTimes;
    private final int popTimeoutSeconds;
    private final String redisCommandPrefix;
    private final int sendCoolDownMillis;
    private final boolean sslEnabled;

    public RedisConfig(String host, int port, String password, String pushQueueName, String popQueueName, String redisCommandPrefix, int maxRetryTimes, int popTimeoutSeconds, int sendCoolDownMillis, boolean sslEnabled) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.pushQueueName = pushQueueName;
        this.popQueueName = popQueueName;
        this.maxRetryTimes = maxRetryTimes;
        this.popTimeoutSeconds = popTimeoutSeconds;
        this.redisCommandPrefix = redisCommandPrefix;
        this.sendCoolDownMillis = sendCoolDownMillis;
        this.sslEnabled = sslEnabled;
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

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public int getPopTimeoutSeconds() {
        return popTimeoutSeconds;
    }

    public String getRedisCommandPrefix() {
        return redisCommandPrefix;
    }

    public int getSendCoolDownMillis() {
        return sendCoolDownMillis;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    @Override
    public String toString() {
        return "RedisConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", password='" + password + '\'' +
                ", pushQueueName='" + pushQueueName + '\'' +
                ", popQueueName='" + popQueueName + '\'' +
                ", maxRetryTimes=" + maxRetryTimes +
                ", popTimeout=" + popTimeoutSeconds +
                ", redisCommandPrefix='" + redisCommandPrefix + '\'' +
                '}';
    }
}
