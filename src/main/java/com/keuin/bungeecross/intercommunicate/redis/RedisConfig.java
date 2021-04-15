package com.keuin.bungeecross.intercommunicate.redis;

import com.keuin.bungeecross.BungeeCross;

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
    private final boolean legacyProtocol;
    private final String topicId;
    private final String endpointName;

    public RedisConfig() {
        host = "";
        port = 6379;
        password = "";
        pushQueueName = "";
        popQueueName = "";
        redisCommandPrefix = "!";
        maxRetryTimes = 10;
        popTimeoutSeconds = 1;
        sendCoolDownMillis = 500;
        sslEnabled = false;
        legacyProtocol = true;
        topicId = BungeeCross.generateTopicId();
        endpointName = "noname_endpoint";
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

    public boolean isLegacyProtocol() {
        return legacyProtocol;
    }

    public String getTopicId() {
        return topicId;
    }

    public String getEndpointName() {
        return endpointName;
    }
}
