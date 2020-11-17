package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.InBoundMessageDispatcher;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class InGameBroadcastRepeater implements MessageRepeater, InBoundMessageDispatcher {

    private final Collection<ServerInfo> servers;

    public InGameBroadcastRepeater(ProxyServer proxyServer) {
        servers = proxyServer.getServers().values();
    }

    /**
     * Repeat message to all other servers.
     * @param message the message to be repeated.
     */
    @Override
    public void repeat(Message message) {
        UUID senderUUID = message.getSender().getUUID();
        for (ServerInfo targetServer : servers) {
            boolean repeat = true; // whether we should repeat message to this server
            for (ProxiedPlayer player : targetServer.getPlayers()) {
                if (player != null && senderUUID.equals(player.getUniqueId())) {
                    repeat = false;
                    break;
                }
            }
            if(repeat) {
                broadcastInServer(message, targetServer);
            }
        }
    }

//    @Override
//    public boolean setBuffer(boolean enabled) {
//        return false;
//    }
//
//    @Override
//    public boolean isBufferEnabled() {
//        return false;
//    }
//
//    @Override
//    public void flush() {
//    }

    @Override
    public void repeatInboundMessage(Message message) {
        servers.forEach((server) -> broadcastInServer(message, server));
    }

    private void broadcastInServer(Message message, ServerInfo server) {
        BaseComponent[] sentMessage = message.toChatInGameRepeatFormat();

        for (ProxiedPlayer player : server.getPlayers()) {
            if(player != null) {
                player.sendMessage(sentMessage);
            }
        }
    }

}
