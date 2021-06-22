package com.keuin.bungeecross.config.mutable;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.config.RedisConfig;

/**
 * Redis configuration section
 */
public class MutableRedisConfig implements RedisConfig {
    private String host =  "";
    private int port = 6379;
    private String password = "";
    private String pushQueueName = "";
    private String popQueueName = "";
    private int maxRetryTimes = 10;
    private int popTimeoutSeconds = 1;
    private String redisCommandPrefix = "!";
    private int sendCoolDownMillis = 500;
    private boolean sslEnabled = false;
    private boolean legacyProtocol = false;
    private String topicId = BungeeCross.generateTopicId();
    private String endpointName = "noname_endpoint";
    private String topicPrefix = "bc.";
    private String chatRelayPrefix = "";
    private int maxJoinedMessageCount = 10;

    /**
     * Create a config using new protocol.
     */
    public MutableRedisConfig(String host, int port, String password,
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

    public MutableRedisConfig() {
    }

    public void copyFrom(RedisConfig from) {
        this.host = from.getHost();
        this.port = from.getPort();
        this.password = from.getPassword();
        this.popQueueName = from.getPopQueueName();
        this.pushQueueName = from.getPushQueueName();
        this.maxRetryTimes = from.getMaxRetryTimes();
        this.popTimeoutSeconds = from.getPopTimeoutSeconds();
        this.redisCommandPrefix = from.getRedisCommandPrefix();
        this.sendCoolDownMillis = from.getSendCoolDownMillis();
        this.sslEnabled = from.isSslEnabled();
        this.legacyProtocol = from.isLegacyProtocol();
        this.topicId = from.getTopicId();
        this.endpointName = from.getEndpointName();
        this.topicPrefix = from.getTopicPrefix();
        this.chatRelayPrefix = from.getChatRelayPrefix();
        this.maxJoinedMessageCount = from.getMaxJoinedMessageCount();
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getPushQueueName() {
        return pushQueueName;
    }

    @Override
    public String getPopQueueName() {
        return popQueueName;
    }

    @Override
    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    @Override
    public int getPopTimeoutSeconds() {
        return popTimeoutSeconds;
    }

    @Override
    public String getRedisCommandPrefix() {
        return redisCommandPrefix;
    }

    @Override
    public int getSendCoolDownMillis() {
        return sendCoolDownMillis;
    }

    @Override
    public boolean isSslEnabled() {
        return sslEnabled;
    }

    @Override
    public boolean isLegacyProtocol() {
        return legacyProtocol;
    }

    @Override
    public String getTopicId() {
        return topicId;
    }

    @Override
    public String getEndpointName() {
        return endpointName;
    }

    @Override
    public String getTopicPrefix() {
        return topicPrefix;
    }

    @Override
    public String getChatRelayPrefix() {
        return chatRelayPrefix;
    }

    @Override
    public int getMaxJoinedMessageCount() {
        return maxJoinedMessageCount;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPushQueueName(String pushQueueName) {
        this.pushQueueName = pushQueueName;
    }

    public void setPopQueueName(String popQueueName) {
        this.popQueueName = popQueueName;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public void setPopTimeoutSeconds(int popTimeoutSeconds) {
        this.popTimeoutSeconds = popTimeoutSeconds;
    }

    public void setRedisCommandPrefix(String redisCommandPrefix) {
        this.redisCommandPrefix = redisCommandPrefix;
    }

    public void setSendCoolDownMillis(int sendCoolDownMillis) {
        this.sendCoolDownMillis = sendCoolDownMillis;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public void setLegacyProtocol(boolean legacyProtocol) {
        this.legacyProtocol = legacyProtocol;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public void setChatRelayPrefix(String chatRelayPrefix) {
        this.chatRelayPrefix = chatRelayPrefix;
    }

    public void setMaxJoinedMessageCount(int maxJoinedMessageCount) {
        this.maxJoinedMessageCount = maxJoinedMessageCount;
    }
}
