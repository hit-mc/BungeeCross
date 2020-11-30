package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.InGameMessage;
import com.keuin.bungeecross.message.user.MessageUser;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TestableInGameMessage extends InGameMessage {
    public TestableInGameMessage(String message, MessageUser sender, ProxiedPlayer proxiedPlayer) {
        super(message, sender, proxiedPlayer);
    }

    public static TestableInGameMessage create(String message, MessageUser sender, ProxiedPlayer proxiedPlayer) {
        return new TestableInGameMessage(message, sender, proxiedPlayer);
    }
}
