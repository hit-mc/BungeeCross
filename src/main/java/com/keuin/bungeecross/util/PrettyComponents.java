package com.keuin.bungeecross.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

public class PrettyComponents {

    private static final ChatColor SERVER_TEXT_COLOR = ChatColor.GREEN;

    /**
     * Create a green navigable button of a server.
     * @param serverName the server name.
     * @param displayStringPattern the formatting pattern used for the button's title.
     * @return a component.
     */
    public static BaseComponent createNavigableServerButton(String serverName, String displayStringPattern) {
        // build hover text
        ComponentBuilder hoverTextBuilder = new ComponentBuilder();
        hoverTextBuilder.append("Go to server ");
        hoverTextBuilder.append((new ComponentBuilder("[" + serverName + "]")).color(SERVER_TEXT_COLOR).create());

        // build server text
        TextComponent componentServer = new TextComponent(String.format(displayStringPattern, serverName));
        componentServer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverTextBuilder.create())));
        componentServer.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        String.format("/server %s", serverName)
                )
        );

        componentServer.setColor(SERVER_TEXT_COLOR);
        componentServer.setUnderlined(true);

        return componentServer;
    }

    public static BaseComponent createNavigableServerButton(String serverName) {
        return createNavigableServerButton(serverName, "[%s]");
    }

}
