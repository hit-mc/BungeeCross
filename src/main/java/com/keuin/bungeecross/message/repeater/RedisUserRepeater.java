package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.RedisManager;
import com.keuin.bungeecross.message.user.MessageUser;

import java.util.Objects;

/**
 * Representing a user behind the Redis server.
 * Note: Please do not use RedisManager as a repeater directly, since it does not contain the command executor's information.
 */
public class RedisUserRepeater implements MessageRepeater {

    private final RedisManager redisManager;
    private final MessageUser messageSender;

    public RedisUserRepeater(RedisManager redisManager, MessageUser messageSender) {
        this.redisManager = redisManager;
        this.messageSender = messageSender;
        if (this.redisManager == null)
            throw new IllegalArgumentException("redisManager should not be null");
        if (this.messageSender == null)
            throw new IllegalArgumentException("messageSender should not be null");
    }

    @Override
    public void repeat(Message message) {
        redisManager.repeat(message); // simply delegate to the Redis manager
    }

    @Override
    public String toString() {
        return messageSender.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedisUserRepeater that = (RedisUserRepeater) o;
        return redisManager.equals(that.redisManager) &&
                messageSender.equals(that.messageSender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(redisManager, messageSender);
    }
}
