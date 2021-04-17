package com.keuin.bungeecross.wiki;

import com.keuin.bungeecross.intercommunicate.message.FixedTimeMessage;
import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.util.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Deprecated
public class LegacyWikiEntry extends FixedTimeMessage {

    private static final LegacyWikiEntry EMPTY_ENTRY = new LegacyWikiEntry(null);
    private static final Logger logger = Logger.getLogger(LegacyWikiEntry.class.getName());
    private static final Pattern invalidWikiContentPattern = Pattern.compile("^见.*(?:特性|方块)");
    private List<String> catalog = Collections.emptyList();

    private final List<String> texts = new ArrayList<>(20);
    private final MessageUser user;
    private String title = "";

    private boolean isMessageGenerated;
    private BaseComponent[] message;

    private LegacyWikiEntry(MessageUser messageUser) {
        message = new BaseComponent[0];
        isMessageGenerated = true;
        this.user = messageUser;
    }

    public LegacyWikiEntry(Response response, MessageUser messageUser)
            throws IOException, EntryNotFoundException {
        this(messageUser);
        isMessageGenerated = false;
        var body = Objects.requireNonNull(response.body());
        var content = body.string();
        if (content.contains("<p><i>No results found.</i></p>")) {
            logger.info("Construct empty WikiEntry");
            throw new EntryNotFoundException();
        }
        var doc = Jsoup.parse(content);

        // extract page title
        title = doc.select("h1#firstHeading").text();
        catalog = doc.select(".mw-parser-output>h2").stream()
                .map(Element::text)
                .filter(s -> !Objects.equals(s, "目录"))
                .collect(Collectors.toUnmodifiableList());
        //doc.select("h2")
//        Arrays.asList("导航菜单", "特性探索", "关注我们", "纵览", "社区", "广告")
        // add text into entry
        doc.select(".mw-parser-output")
                .forEach(container -> container.getAllElements().select("p").stream()
                        .map(Element::text)
                        .filter(str -> str.length() > 6 && !invalidWikiContentPattern.matcher(str).find())
                        .forEach(texts::add));
        logger.fine("Read " + texts.size() + " lines.");
    }

    public List<String> getTexts() {
        return Collections.unmodifiableList(texts);
    }

    public List<String> getCatalog() {
        return catalog;
    }

    private void buildMessage() {
        if (texts.isEmpty()) {
            message = new BaseComponent[0];
            isMessageGenerated = true;
        }

        var builder = new ComponentBuilder();
        int builtTextSize = 0;
        final int MAX_LENGTH = 300;
        builder.append(WikiPrettyUtil.h1(title));
        for (String s : texts) {
            if ((builtTextSize > 0) && (builtTextSize + s.length()) >= MAX_LENGTH) {
                builder.append(new ComponentBuilder("(more...)")
                        .color(ChatColor.DARK_GRAY).italic(true).bold(true).create());
                break;
            }
            builtTextSize += s.length();
            builder.append(WikiPrettyUtil.p(s));
        }
        message = builder.create();
        isMessageGenerated = true;
    }

    @Deprecated
    @Override
    public String getMessage() {
        if (!isMessageGenerated)
            buildMessage();
        assert message != null;
        return MessageUtil.getPlainTextOfBaseComponents(message);
    }

    @Deprecated
    @Override
    public BaseComponent[] getRichTextMessage() {
        if (!isMessageGenerated)
            buildMessage();
        assert message != null;
        return Arrays.copyOf(message, message.length);
    }

    @Deprecated
    @Override
    public MessageUser getSender() {
        return user;
    }

    @Deprecated
    @Override
    public boolean ifCanBeJoined() {
        return false;
    }

    public static class EntryNotFoundException extends Exception {
    }
}
