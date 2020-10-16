package com.keuin.bungeecross;

import com.keuin.bungeecross.message.ingame.InGameChatProcessor;
import com.keuin.bungeecross.message.InGameMessage;
import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.message.user.PlayerUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Events implements Listener {

    private final InGameChatProcessor inGameChatProcessor;
    private final Plugin plugin;
    private final Logger logger = Logger.getLogger(Events.class.getName());

    private final Map<UUID, ServerInfo> joiningServers = new HashMap<>();

    public Events(Plugin plugin, InGameChatProcessor inGameChatProcessor) {
        this.plugin = plugin;
        this.inGameChatProcessor = inGameChatProcessor;
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
//        logger.info("PluginMessage: " + new String(event.getData(), StandardCharsets.UTF_8));
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        joiningServers.put(event.getPlayer().getUniqueId(), event.getTarget());
    }

    @EventHandler
    public void onPlayerJoined(ServerConnectedEvent event) {

        // after a player joined
        ProxiedPlayer player = event.getPlayer();
        ProxyServer proxy = plugin.getProxy();
        ServerInfo server = joiningServers.get(player.getUniqueId());

        // Set custom tab header
//        player.sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("actionbar")); // invalid?
//        player.sendMessage(ChatMessageType.CHAT, new TextComponent("chat"));
//        player.sendMessage(ChatMessageType.SYSTEM, new TextComponent("system"));
//        player.setTabHeader(new ComponentBuilder("Test Header 1").color(ChatColor.GOLD).create(), new ComponentBuilder("Test Header 2").color(ChatColor.GOLD).create());

        if (!joiningServers.containsKey(event.getPlayer().getUniqueId())) {
            logger.warning(String.format("Unexpected player %s. Login broadcast will not be sent.", event.getPlayer().getName()));
            return;
        }

        // build message
//        TranslatableComponent joinedMessage = new TranslatableComponent("multiplayer.player.joined");
//        joinedMessage.addWith(player.getName());
        logger.info(String.format("Server: %s, player: %s, proxy: %s.", server, player, proxy));
        BaseComponent[] joinedMessage = (new ComponentBuilder(String.format("%s joined server [%s].", player.getName(), server.getName()))).italic(true).color(ChatColor.YELLOW).create();

        for (ServerInfo serverInfo : proxy.getServers().values()) {
            // for all other servers
            if (!serverInfo.getName().equals(server.getName()))
                for (ProxiedPlayer dest : serverInfo.getPlayers())
                    if (!dest.getUniqueId().equals(player.getUniqueId()))
                        dest.sendMessage(joinedMessage); // repeat the join message
        }

        joiningServers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if(!(event.getSender() instanceof ProxiedPlayer)) {
            logger.severe(String.format("Sender is not a ProxiedPlayer instance: %s", event.getSender().toString()));
            return;
        }

        String message = event.getMessage();
        ProxiedPlayer sender = (ProxiedPlayer) event.getSender();
        MessageUser messageUser = new PlayerUser(sender.getName(), sender.getUniqueId(), sender.getServer().getInfo().getName());

        if (message.startsWith("/"))
            return; // Do not repeat commands

        logger.info(String.format("Chat message: %s, sender: %s", message, messageUser));
        inGameChatProcessor.issue(new InGameMessage(message, messageUser, sender));

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