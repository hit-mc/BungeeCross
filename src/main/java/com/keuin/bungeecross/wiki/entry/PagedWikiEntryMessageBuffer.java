package com.keuin.bungeecross.wiki.entry;

import com.keuin.bungeecross.intercommunicate.message.FixedTimeMessage;
import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

public class PagedWikiEntryMessageBuffer extends FixedTimeMessage implements Consumer<BaseComponent[]> {

    private static final MessageUser sender = MessageUser.createNameOnlyUser("MinecraftWiki");
    private final ComponentBuilder builder = new ComponentBuilder();
    private String cachedMessageString = null;
    private BaseComponent[] cachedBuiltMessage;

    @Override
    public String getMessage() {
        if (cachedMessageString == null) {
            cachedBuiltMessage = builder.create();
            cachedMessageString = MessageUtil.getPlainTextOfBaseComponents(cachedBuiltMessage);
        }
        return cachedMessageString;
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        if (cachedBuiltMessage == null)
            cachedBuiltMessage = builder.create();
        return Arrays.copyOf(cachedBuiltMessage, cachedBuiltMessage.length);
    }

    @Override
    public MessageUser getSender() {
        return sender;
    }

    @Override
    public boolean ifCanBeJoined() {
        return true;
    }

    @Override
    public void accept(BaseComponent[] baseComponents) {
        cachedBuiltMessage = null;
        cachedMessageString = null;
        builder.append(baseComponents);
    }
}
