package com.keuin.bungeecross.wiki.entry;

import com.keuin.bungeecross.intercommunicate.message.Message;

import java.util.function.Consumer;

/**
 * A view of a Minecraft wiki webpage Response, from OkHttp.
 */
public interface WikiEntryView {
    /**
     * Print the next page to message consumer.
     * May send more than one message(s).
     *
     * @param messageConsumer the message consumer.
     */
    void print(Consumer<Message> messageConsumer);

    /**
     * Whether the view has reached the page's end.
     *
     * @return true if end, false if there are readable contents.
     */
    boolean isEnd();

    /**
     * Throws when the webpage is not a valid Minecraft wiki entry page.
     */
    class InvalidWikiPageException extends Exception {
        public InvalidWikiPageException(String message) {
            super(message);
        }

        public InvalidWikiPageException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidWikiPageException(Throwable cause) {
            super(cause);
        }
    }

    class NoSuchEntryException extends InvalidWikiPageException {
        public NoSuchEntryException() {
            super("No such entry in Minecraft wiki. Your keyword is incorrect.");
        }
    }
}
