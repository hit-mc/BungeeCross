package com.keuin.bungeecross;

import com.keuin.bungeecross.message.ingame.InGameChatProcessor;
import com.keuin.bungeecross.message.ingame.InGameMessage;
import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.message.user.PlayerUser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;
import java.util.logging.Logger;

public class Events implements Listener {

    private final InGameChatProcessor inGameChatProcessor;
    private final Logger logger;

    public Events(InGameChatProcessor inGameChatProcessor) {
        this.inGameChatProcessor = inGameChatProcessor;
        logger = BungeeCross.logger;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if(!(event.getSender() instanceof ProxiedPlayer)) {
            logger.severe(String.format("Sender is not a ProxiedPlayer instance: %s", event.getSender().toString()));
            return;
        }

        String message = event.getMessage();
        ProxiedPlayer sender = (ProxiedPlayer) event.getSender();
        MessageUser user = new PlayerUser(sender.getName(), sender.getUniqueId());
        logger.info(String.format("Chat message: %s, sender: %s", message, user));

        inGameChatProcessor.issue(new InGameMessage(message, user));

//        String rel = String.format("Broadcast! user=%s, msg=%s.", sender, message);
//        BungeeCross.logger.info(rel);
//        ProxyServer.getInstance().broadcast(new ComponentBuilder(rel).color(ChatColor.RED).create());
//        ProxyServer proxy = ProxyServer.getInstance();
//        ProxiedPlayer
//        assert event.getSender() instanceof ProxiedPlayer;
//        ProxiedPlayer sender = (ProxiedPlayer)event.getSender();
//        //        String sender = event.getSender().toString();
//        ProxyServer proxy = ProxyServer.getInstance();
//        Map<String, ServerInfo> servers = proxy.getServers();
//        for (Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
//            if(!sender.getServer().getInfo().getName().equals(entry.getValue().getName())) {
////              entry.getValue()
//            }
//        }
//        String message = event.getMessage();
    }

}