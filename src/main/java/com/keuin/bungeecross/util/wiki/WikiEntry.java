package com.keuin.bungeecross.util.wiki;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class WikiEntry implements Message {

    private static final WikiEntry EMPTY_ENTRY = new WikiEntry(null);
    private final List<String> texts = new ArrayList<>(20);
    private final MessageUser user;
    private String title = "";
    private BaseComponent[] message;

    private WikiEntry(MessageUser messageUser) {
        message = new BaseComponent[0];
        this.user = messageUser;
    }

    public WikiEntry(Response response, MessageUser messageUser) throws IOException {
        this(messageUser);
        var body = Objects.requireNonNull(response.body());
        var content = body.string();
        if (content.contains("<p><i>No results found.</i></p>")) {
            return;
        }
        var doc = Jsoup.parse(content);

        // extract page title
        for (Element ele : doc.select("hi#firstHeading")) {
            title = ele.text();
            break;
        }

        // add text into entry
        for (var container : doc.select(".mw-parser-output")) {
//                container.getElementsByTag("p").stream()
//                        .map(Element::text).forEach(entry.texts::add);
            container.getAllElements().select("p").stream()
                    .map(Element::text)
                    .filter(str -> str.length() > 6)
                    .filter(str -> !str.startsWith("见"))
                    .forEach(texts::add);
        }
    }

    public List<String> getTexts() {
        return Collections.unmodifiableList(texts);
    }

    private void buildMessage() {
        var builder = new ComponentBuilder();
        builder.append(WikiPrettyUtil.h1(title));
        for (String s : texts) {
            builder.append(WikiPrettyUtil.p(s));
        }
        message = builder.create();
    }

    @Override
    public String getMessage() {
        if (message == null)
            buildMessage();
        assert message != null;
        return MessageUtil.getPlainTextOfBaseComponents(message);
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        if (message == null)
            buildMessage();
        assert message != null;
        return Arrays.copyOf(message, message.length);
    }

    @Override
    public MessageUser getSender() {
        return user;
    }

    @Override
    public boolean isJoinable() {
        return false;
    }

    public static class EntryNotFoundException extends Exception {
        private final String entryName;

        public EntryNotFoundException(String entryName) {
            this.entryName = entryName;
        }

        @Override
        public String toString() {
            return "EntryNotFoundException{" +
                    "entryName='" + entryName + '\'' +
                    '}';
        }
    }
}
