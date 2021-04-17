package com.keuin.bungeecross.wiki.entry;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.wiki.styler.PrintStyler;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ConcreteWikiEntryView implements WikiEntryView {

    private final Document document;
    private final String title;
    private final List<Element> catalog;
    private final List<String> textCatalog;
    private final PrintStyler styler;

    protected ConcreteWikiEntryView(@NotNull Document document, @NotNull PrintStyler styler) {
        this.document = Objects.requireNonNull(document);
        this.styler = Objects.requireNonNull(styler);
        title = document.select("h1#firstHeading").text();
        catalog = document.select(".mw-parser-output>h2").stream().collect(Collectors.toUnmodifiableList());
        textCatalog = catalog.stream().map(Element::text).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Create a concrete wiki view from a success Minecraft wiki page GET response.
     *
     * @param response the response.
     * @return the view.
     */
    public static ConcreteWikiEntryView createView(@NotNull Response response, @NotNull PrintStyler styler) throws InvalidWikiPageException, IOException {
        Objects.requireNonNull(response);
        Objects.requireNonNull(styler);
        if (!response.isSuccessful())
            throw new InvalidWikiPageException("response with code " + response.code() + " is unsuccessful");

        var body = Objects.requireNonNull(response.body());
        var content = body.string();
        if (content.contains("<p><i>No results found.</i></p>")) {
            throw new NoSuchEntryException();
        }
        var doc = Jsoup.parse(content);

        // validate Document
        if (doc.select("h1#firstHeading").size() <= 0)
            throw new InvalidWikiPageException("page heading is missing");
        if (doc.select(".mw-parser-output>h2").size() <= 0)
            throw new InvalidWikiPageException("page catalog is missing");

        return new ConcreteWikiEntryView(doc, styler);
    }

    public static ConcreteWikiEntryView createView(@NotNull Response response) throws InvalidWikiPageException, IOException {
        return createView(response, PrintStyler.DEFAULT_STYLER);
    }

    /**
     * Render page into Message.
     */
    private void renderPage() {

    }

    @Override
    public void print(Consumer<Message> messageConsumer) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
