package com.keuin.bungeecross.config;

public interface MessageBrokerConfig {
    String getHost();

    int getPort();

    int getMaxRetryTimes();

    String getCommandPrefix();

    int getSendCoolDownMillis();

    String getTopicId();

    String getEndpointName();

    String getTopicPrefix();

    String getChatRelayPrefix();

    int getMaxJoinedMessageCount();

    int getKeepAliveIntervalMillis();

    long getSubscriberId();

    long getSubscriberReconnectIntervalMillis();
}
