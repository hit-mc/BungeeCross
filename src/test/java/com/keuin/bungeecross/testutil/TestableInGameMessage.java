package com.keuin.bungeecross.testutil;

import com.keuin.bungeecross.intercommunicate.message.InGameMessage;
import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TestableInGameMessage extends InGameMessage {
    public TestableInGameMessage(String message, MessageUser sender, ProxiedPlayer proxiedPlayer) {
        super(message, proxiedPlayer);
    }

    public static TestableInGameMessage create(String message, MessageUser sender, ProxiedPlayer proxiedPlayer) {
        return new TestableInGameMessage(message, sender, proxiedPlayer);
    }
}
