package com.keuin.bungeecross.wiki;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.wiki.entry.WikiEntryView;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Search, fetch pages from Minecraft wiki.
 */
public class WikiFetcher {

    private final Logger logger = Logger.getLogger(WikiFetcher.class.getName());
    private final OkHttpClient client;

    private final ConcurrentMap<String, LegacyWikiEntry> cache = new ConcurrentHashMap<>();
    private final Set<String> cachedInvalidKeywords = Collections.synchronizedSet(new HashSet<>());

    public WikiFetcher() {
        client = new OkHttpClient();
    }

    public WikiFetcher(Proxy proxy) {
        Objects.requireNonNull(proxy);
        client = new OkHttpClient.Builder().proxy(proxy).build();
    }

    public void fetchEntry(String keyword, Consumer<LegacyWikiEntry> callback, Consumer<Exception> onFailure, MessageUser messageUser) {
        var url = String.format("https://minecraft.fandom.com/zh/wiki/%s", keyword);
        var request = new Request.Builder().url(url).build();
        var cachedEntry = cache.get(keyword);

        // cache success entries
        if (cachedEntry != null) {
            // use cached result
            // TODO: this is not async, optimize it in the future
            callback.accept(cachedEntry);
            return;
        }

        // cache invalid queries
        if (cachedInvalidKeywords.contains(keyword)) {
            onFailure.accept(new NoSuchElementException());
            return;
        }

        // do new query
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
                        var entry = new LegacyWikiEntry(response, messageUser);
                        cache.put(keyword, entry);
                        callback.accept(entry);
                    } else if (response.code() == 404) {
                        cachedInvalidKeywords.add(keyword);
                        throw new WikiEntryView.NoSuchEntryException();
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
            return String.format("Minecraft wiki made an invalid response: status code is %d", responseCode);
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

}
