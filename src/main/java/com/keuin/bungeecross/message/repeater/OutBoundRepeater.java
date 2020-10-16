package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.RedisQueueManager;

public class OutBoundRepeater implements MessageRepeater {

    private final RedisQueueManager redisQueueManager;

    public OutBoundRepeater(RedisQueueManager redisQueueManager) {
        this.redisQueueManager = redisQueueManager;
    }

    @Override
    public void repeat(Message message) {
        redisQueueManager.sendMessage(message);
    }

    @Override
    public void flush() {

    }
}
