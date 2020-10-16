package com.keuin.bungeecross.util;

import net.md_5.bungee.api.chat.BaseComponent;

public class MessageUtil {

    public static String getPlainTextOfBaseComponents(BaseComponent[] baseComponents) {
        if (baseComponents == null)
            throw new IllegalArgumentException("baseComponents must not be null.");
        StringBuilder builder = new StringBuilder();
        for (BaseComponent component : baseComponents) {
            builder.append(component.toPlainText());
        }
        return builder.toString();
    }

}
