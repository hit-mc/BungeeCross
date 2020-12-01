package com.keuin.bungeecross.message.user;

import com.keuin.bungeecross.message.repeater.MessageRepeater;

/**
 * Representing a user who can receive messages.
 */
public interface RepeatableUser extends MessageUser, MessageRepeater {
}
