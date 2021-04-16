package com.keuin.bungeecross.intercommunicate.user;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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
    public @NotNull String getName() {
        return Objects.requireNonNull(user.getName());
    }

    @Override
    public UUID getUUID() {
        return user.getUUID();
    }

    @Override
    public @NotNull String getId() {
        return Objects.requireNonNull(user.getId());
    }

    @Override
    public String getLocation() {
        return user.getLocation();
    }
}
