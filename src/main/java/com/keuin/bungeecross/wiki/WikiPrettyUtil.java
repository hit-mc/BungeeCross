package com.keuin.bungeecross.wiki;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

class WikiPrettyUtil {
    public static BaseComponent[] h1(String string) {
        return new ComponentBuilder(string).color(ChatColor.GOLD).bold(true).append("\n").create();
    }

    public static BaseComponent[] h2(String string) {
        return new ComponentBuilder(string).color(ChatColor.GOLD).bold(false).append("\n").create();
    }

    public static BaseComponent[] bold(String string) {
        return new ComponentBuilder(string).bold(true).append("\n").create();
    }

    public static BaseComponent[] p(String string) {
        return new ComponentBuilder(string).append("\n").create();
    }
}
