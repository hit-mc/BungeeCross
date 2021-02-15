package com.keuin.bungeecross.message.user;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.repeater.MessageRepeatable;

import java.util.UUID;

public class SimpleRepeatableUser implements RepeatableUser {

    private final MessageUser user;
    private final MessageRepeatable repeater;

    public SimpleRepeatableUser(MessageUser user, MessageRepeatable repeater) {
        this.user = user;
        this.repeater = repeater;
    }

    @Override
    public void repeat(Message message) {
        repeater.repeat(message);
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public UUID getUUID() {
        return user.getUUID();
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public String getLocation() {
        return user.getLocation();
    }
}
