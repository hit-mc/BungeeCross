package com.keuin.bungeecross.testutil;

import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.logging.Logger;

public class TestableProxyServer extends ProxyServer {

    private final Logger logger = Logger.getLogger(TestableProxyServer.class.getName());
    private final Map<String, ServerInfo> servers = new HashMap<>();

    public static TestableProxyServer createSkeleton() {
        return new TestableProxyServer();
    }

    public void addServerInfo(ServerInfo serverInfo) {
        if (serverInfo == null)
            throw new IllegalArgumentException("server info must not be null");
        servers.put(serverInfo.getName(), serverInfo);
    }

    @Override
    public String getName() {
        return "test_server";
    }

    @Override
    public String getVersion() {
        return "version";
    }

    @Override
    public String getTranslation(String s, Object... objects) {
        return "translation";
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Collection<ProxiedPlayer> getPlayers() {
        return null;
    }

    @Override
    public ProxiedPlayer getPlayer(String s) {
        return null;
    }

    @Override
    public ProxiedPlayer getPlayer(UUID uuid) {
        return null;
    }

    @Override
    public Map<String, ServerInfo> getServers() {
        return Collections.unmodifiableMap(servers);
    }

    @Override
    public ServerInfo getServerInfo(String s) {
        return servers.get(s);
    }

    @Override
    public PluginManager getPluginManager() {
        return null;
    }

    @Override
    public ConfigurationAdapter getConfigurationAdapter() {
        return null;
    }

    @Override
    public void setConfigurationAdapter(ConfigurationAdapter configurationAdapter) {

    }

    @Override
    public ReconnectHandler getReconnectHandler() {
        return null;
    }

    @Override
    public void setReconnectHandler(ReconnectHandler reconnectHandler) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void stop(String s) {

    }

    @Override
    public void registerChannel(String s) {

    }

    @Override
    public void unregisterChannel(String s) {

    }

    @Override
    public Collection<String> getChannels() {
        return null;
    }

    @Override
    public String getGameVersion() {
        return null;
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public ServerInfo constructServerInfo(String s, InetSocketAddress inetSocketAddress, String s1, boolean b) {
        return null;
    }

    @Override
    public ServerInfo constructServerInfo(String s, SocketAddress socketAddress, String s1, boolean b) {
        return null;
    }

    @Override
    public CommandSender getConsole() {
        return null;
    }

    @Override
    public File getPluginsFolder() {
        return null;
    }

    @Override
    public TaskScheduler getScheduler() {
        return null;
    }

    @Override
    public int getOnlineCount() {
        return 0;
    }

    @Override
    public void broadcast(String s) {

    }

    @Override
    public void broadcast(BaseComponent... baseComponents) {

    }

    @Override
    public void broadcast(BaseComponent baseComponent) {

    }

    @Override
    public Collection<String> getDisabledCommands() {
        return null;
    }

    @Override
    public ProxyConfig getConfig() {
        return null;
    }

    @Override
    public Collection<ProxiedPlayer> matchPlayer(String s) {
        return null;
    }

    @Override
    public Title createTitle() {
        return null;
    }
}
