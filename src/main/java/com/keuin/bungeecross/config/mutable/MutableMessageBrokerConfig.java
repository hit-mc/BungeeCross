package com.keuin.bungeecross.config.mutable;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.config.MessageBrokerConfig;

import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Redis configuration section
 */
public class MutableMessageBrokerConfig implements MessageBrokerConfig {

    private String host =  "";
    private int port = 6379;
    private int maxRetryTimes = 10;
    private String commandPrefix = "!";
    private int sendCoolDownMillis = 500;
    private String topicId = BungeeCross.generateTopicId();
    private String endpointName = "noname_endpoint";
    private String topicPrefix = "bc.";
    private String chatRelayPrefix = "";
    private int maxJoinedMessageCount = 10;
    private int keepAliveIntervalMillis = 0;
    private long subscriberId = 0;
    private long subscriberReconnectIntervalMillis = 10000;

    /**
     * Create a config using new protocol.
     */
    public MutableMessageBrokerConfig(String host, int port,
                                      String topicId, String endpointName, String topicPrefix) {
        this();
        this.host = host;
        this.port = port;
        this.topicId = topicId;
        this.endpointName = endpointName;
        this.topicPrefix = topicPrefix;
    }

    public MutableMessageBrokerConfig() {
    }

    public void copyFrom(MessageBrokerConfig from) {
        // TODO: rewrite using clone
        this.host = from.getHost();
        this.port = from.getPort();
        this.maxRetryTimes = from.getMaxRetryTimes();
        this.commandPrefix = from.getCommandPrefix();
        this.sendCoolDownMillis = from.getSendCoolDownMillis();
        this.topicId = from.getTopicId();
        this.endpointName = from.getEndpointName();
        this.topicPrefix = from.getTopicPrefix();
        this.chatRelayPrefix = from.getChatRelayPrefix();
        this.maxJoinedMessageCount = from.getMaxJoinedMessageCount();
        this.keepAliveIntervalMillis = from.getKeepAliveIntervalMillis();
        this.subscriberId = from.getSubscriberId();
        this.subscriberReconnectIntervalMillis = from.getSubscriberReconnectIntervalMillis();
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
    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    @Override
    public String getCommandPrefix() {
        return commandPrefix;
    }

    @Override
    public int getSendCoolDownMillis() {
        return sendCoolDownMillis;
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

    @Override
    public int getKeepAliveIntervalMillis() {
        return keepAliveIntervalMillis;
    }

    @Override
    public long getSubscriberId() {
        return subscriberId;
    }

    @Override
    public long getSubscriberReconnectIntervalMillis() {
        return subscriberReconnectIntervalMillis;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public void setSendCoolDownMillis(int sendCoolDownMillis) {
        this.sendCoolDownMillis = sendCoolDownMillis;
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

    public void setKeepAliveIntervalMillis(int keepAliveIntervalMillis) {
        this.keepAliveIntervalMillis = keepAliveIntervalMillis;
    }

    public void setSubscriberId(long subscriberId) {
        this.subscriberId = subscriberId;
    }

    public void setSubscriberReconnectIntervalMillis(long subscriberReconnectIntervalMillis) {
        this.subscriberReconnectIntervalMillis = subscriberReconnectIntervalMillis;
    }
}
