package com.keuin.bungeecross.config;

public interface RedisConfig {
    String getHost();

    int getPort();

    String getPassword();

    String getPushQueueName();

    String getPopQueueName();

    int getMaxRetryTimes();

    int getPopTimeoutSeconds();

    String getRedisCommandPrefix();

    int getSendCoolDownMillis();

    boolean isSslEnabled();

    boolean isLegacyProtocol();

    String getTopicId();

    String getEndpointName();

    String getTopicPrefix();

    String getChatRelayPrefix();

    int getMaxJoinedMessageCount();
}
