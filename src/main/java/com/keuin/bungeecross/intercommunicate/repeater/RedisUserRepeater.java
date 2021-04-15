package com.keuin.bungeecross.intercommunicate.repeater;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.redis.RedisManager;
import com.keuin.bungeecross.intercommunicate.user.MessageUser;

import java.util.Objects;

/**
 * Representing a user behind the Redis server.
 * Note: Please do not use RedisManager as a repeater directly, since it does not contain the command executor's information.
 */
public class RedisUserRepeater implements MessageRepeatable {

    private final MessageRepeatable messageRepeatable;
    private final MessageUser messageSender;

    public RedisUserRepeater(MessageRepeatable messageRepeatable, MessageUser messageSender) {
        this.messageRepeatable = messageRepeatable;
        this.messageSender = messageSender;
        if (this.messageRepeatable == null)
            throw new IllegalArgumentException("messageRepeatable should not be null");
        if (this.messageSender == null)
            throw new IllegalArgumentException("messageSender should not be null");
    }

    @Override
    public void repeat(Message message) {
        messageRepeatable.repeat(message); // simply delegate to the Redis manager
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
        return messageRepeatable.equals(that.messageRepeatable) &&
                messageSender.equals(that.messageSender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageRepeatable, messageSender);
    }
}
