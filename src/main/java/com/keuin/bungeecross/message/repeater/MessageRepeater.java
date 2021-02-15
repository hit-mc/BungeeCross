package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.Message;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;

public abstract class MessageRepeater implements MessageRepeatable {
    /**
     * Send a message to all players in a certain server.
     * @param message the message to be sent.
     * @param server the server where all players will receive the message.
     */
    protected final void broadcastInServer(Message message, ServerInfo server) {
        BaseComponent[] msg = message.toChatInGameRepeatFormat();
        server.getPlayers().forEach(p -> Optional.ofNullable(p).ifPresent(player -> player.sendMessage(msg)));
    }
}
