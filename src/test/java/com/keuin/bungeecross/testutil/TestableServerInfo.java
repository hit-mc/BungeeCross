package com.keuin.bungeecross.testutil;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TestableServerInfo implements ServerInfo {

    private final Set<ProxiedPlayer> playerSet = new HashSet<>();
    private String name = "skeleton_server";

    public TestableServerInfo(String name) {
        this.name = name;
    }

    public TestableServerInfo() {
    }

    public final void addPlayer(ProxiedPlayer player) {
        playerSet.add(player);
    }

    @Override
    public String getName() {
        return name;
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
    public Collection<ProxiedPlayer> getPlayers() {
        return Collections.unmodifiableCollection(playerSet);
    }

    @Override
    public String getMotd() {
        return null;
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean canAccess(CommandSender commandSender) {
        return false;
    }

    @Override
    public void sendData(String s, byte[] bytes) {

    }

    @Override
    public boolean sendData(String s, byte[] bytes, boolean b) {
        return false;
    }

    @Override
    public void ping(Callback<ServerPing> callback) {

    }
}
