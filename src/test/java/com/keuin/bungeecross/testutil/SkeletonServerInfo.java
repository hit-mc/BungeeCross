package com.keuin.bungeecross.testutil;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;

public class SkeletonServerInfo implements ServerInfo {
    @Override
    public String getName() {
        return "skeleton_server";
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
        return null;
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
