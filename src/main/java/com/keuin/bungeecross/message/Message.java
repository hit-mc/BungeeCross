package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.redis.RedisMessage;
import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.message.user.RedisUser;

import java.util.regex.*;

public interface Message {
    String getMessage();
    MessageUser getSender();

    /**
     * Pack message into Redis format.
     * @return packed string.
     */
    default String pack() {
        String SPLIT = "||";
        return String.format("%s%s%s", getSender().getName(), SPLIT, getMessage());
    }

    /**
     * Construct a Message object by raw string from Redis.
     * @param rawString the raw string.
     * @return a Message object. If the raw string is invalid, return null.
     */
    static Message fromRedisRawString(String rawString) {
        Pattern pattern = Pattern.compile("([^|]*)(?:\\|\\|)([\\s\\S]*)");
        Matcher matcher = pattern.matcher(rawString);
        if (matcher.matches()) {
            String sender = matcher.group(0);
            String body = matcher.group(1);
            return new RedisMessage(new RedisUser(sender), body);
        }
        return null;
    }
}
