package com.keuin.bungeecross.util.wiki;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Search, fetch pages from Minecraft wiki.
 */
public class WikiFetcher {

    private static final Logger logger = Logger.getLogger(WikiFetcher.class.getName());
    private static final OkHttpClient client = new OkHttpClient();

    public static void fetchEntry(String name, Consumer<WikiEntry> callback, Consumer<Exception> onFailure, MessageUser messageUser) {
        var url = String.format("https://minecraft.fandom.com/zh/wiki/%s", name);
        var request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                logger.warning("Failed to make request: " + e.getMessage());
                onFailure.accept(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                logger.fine("Response returned.");
                try {
                    if (response.isSuccessful()) {
                        logger.fine("Response is successful.");
                        callback.accept(new WikiEntry(response, messageUser));
                    } else if (response.code() == 404) {
                        throw new NoSuchEntryException();
                    } else {
                        logger.fine("Response is not successful.");
                        onFailure.accept(new BadResponseException(response.code()));
                    }
                } catch (Exception e) {
                    onFailure.accept(e);
                }
            }
        });
    }

    private static class BadResponseException extends Exception {
        private final int responseCode;

        public BadResponseException(int responseCode) {
            this.responseCode = responseCode;
        }

        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public String toString() {
            return String.format("BadResponseException{responseCode=%d}", responseCode);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BadResponseException that = (BadResponseException) o;
            return responseCode == that.responseCode;
        }

        @Override
        public int hashCode() {
            return Objects.hash(responseCode);
        }
    }

    private static class NoSuchEntryException extends Exception {
        @Override
        public String toString() {
            return "No such entry in Minecraft wiki. Your keyword is incorrect.";
        }
    }
}
