package com.keuin.bungeecross.testutil;

import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.score.Scoreboard;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class TestableProxiedPlayer implements ProxiedPlayer {

    private final String name;
    private final UUID uuid;
    private final Server server;
    private int messageCount = 0;
    private String displayName;

    public TestableProxiedPlayer(String name, UUID uuid, Server server) {
        this.name = name;
        this.uuid = uuid;
        this.displayName = name;
        this.server = server;
    }

    public static TestableProxiedPlayer createSkeleton() {
        return new TestableProxiedPlayer("name", UUID.randomUUID(), TestableServer.createSkeleton());
    }

    public int getMessageCount() {
        return messageCount;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public void sendMessage(UUID uuid, BaseComponent baseComponent) {
        ++messageCount;
    }

    @Override
    public void sendMessage(UUID uuid, BaseComponent... baseComponents) {
        ++messageCount;
    }

    @Override
    public void setDisplayName(String s) {
        this.displayName = s;
    }

    @Override
    public void sendMessage(ChatMessageType chatMessageType, BaseComponent... baseComponents) {
        ++messageCount;
    }

    @Override
    public void sendMessage(ChatMessageType chatMessageType, BaseComponent baseComponent) {
        ++messageCount;
    }

    @Override
    public void connect(ServerInfo serverInfo) {

    }

    @Override
    public void connect(ServerInfo serverInfo, ServerConnectEvent.Reason reason) {

    }

    @Override
    public void connect(ServerInfo serverInfo, Callback<Boolean> callback) {

    }

    @Override
    public void connect(ServerInfo serverInfo, Callback<Boolean> callback, ServerConnectEvent.Reason reason) {

    }

    @Override
    public void connect(ServerConnectRequest serverConnectRequest) {

    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public int getPing() {
        return 0;
    }

    @Override
    public void sendData(String s, byte[] bytes) {

    }

    @Override
    public PendingConnection getPendingConnection() {
        return null;
    }

    @Override
    public void chat(String s) {

    }

    @Override
    public ServerInfo getReconnectServer() {
        return server.getInfo();
    }

    @Override
    public void setReconnectServer(ServerInfo serverInfo) {

    }

    @Override
    public String getUUID() {
        return uuid.toString();
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public byte getViewDistance() {
        return 0;
    }

    @Override
    public ChatMode getChatMode() {
        return null;
    }

    @Override
    public boolean hasChatColors() {
        return false;
    }

    @Override
    public SkinConfiguration getSkinParts() {
        return null;
    }

    @Override
    public MainHand getMainHand() {
        return null;
    }

    @Override
    public void setTabHeader(BaseComponent baseComponent, BaseComponent baseComponent1) {

    }

    @Override
    public void setTabHeader(BaseComponent[] baseComponents, BaseComponent[] baseComponents1) {

    }

    @Override
    public void resetTabHeader() {

    }

    @Override
    public void sendTitle(Title title) {

    }

    @Override
    public boolean isForgeUser() {
        return false;
    }

    @Override
    public Map<String, String> getModList() {
        return null;
    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendMessage(String s) {
        ++messageCount;
    }

    @Override
    public void sendMessages(String... strings) {
        ++messageCount;
    }

    @Override
    public void sendMessage(BaseComponent... baseComponents) {
        ++messageCount;
    }

    @Override
    public void sendMessage(BaseComponent baseComponent) {
        ++messageCount;
    }

    @Override
    public Collection<String> getGroups() {
        return null;
    }

    @Override
    public void addGroups(String... strings) {

    }

    @Override
    public void removeGroups(String... strings) {

    }

    @Override
    public boolean hasPermission(String s) {
        return false;
    }

    @Override
    public void setPermission(String s, boolean b) {

    }

    @Override
    public Collection<String> getPermissions() {
        return null;
    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    public SocketAddress getSocketAddress() {
        return null;
    }

    @Override
    public void disconnect(String s) {

    }

    @Override
    public void disconnect(BaseComponent... baseComponents) {

    }

    @Override
    public void disconnect(BaseComponent baseComponent) {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public Unsafe unsafe() {
        return null;
    }
}
