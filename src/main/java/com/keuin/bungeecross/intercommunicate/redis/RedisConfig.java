package com.keuin.bungeecross.intercommunicate.redis;

import com.keuin.bungeecross.BungeeCross;

/**
 * Redis configuration section
 * Immutable.
 */
public class RedisConfig {
    private String host;
    private int port;
    private String password;
    private String pushQueueName;
    private String popQueueName;
    private int maxRetryTimes;
    private int popTimeoutSeconds;
    private String redisCommandPrefix;
    private int sendCoolDownMillis;
    private boolean sslEnabled;
    private boolean legacyProtocol;
    private String topicId;
    private String endpointName;
    private String topicPrefix;

    /**
     * Create a config using new protocol.
     */
    public RedisConfig(String host, int port, String password,
                       String topicId, String endpointName, String topicPrefix) {
        this();
        this.host = host;
        this.port = port;
        this.password = password;
        this.topicId = topicId;
        this.endpointName = endpointName;
        this.legacyProtocol = false;
        this.topicPrefix = topicPrefix;
    }

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
        topicPrefix = "bc.";
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

    public String getTopicPrefix() {
        return topicPrefix;
    }
}
