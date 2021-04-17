package com.keuin.bungeecross.util;

import com.keuin.bungeecross.wiki.WikiFetcher;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

public class WikiFetcherTest {
    private final Logger logger = Logger.getLogger(WikiFetcherTest.class.getName());

    @Test
    public void testWiki() throws InterruptedException {
        final var queue = new LinkedBlockingDeque<>();
        new WikiFetcher().fetchEntry("远古守卫者", wikiEntry -> {
            for (String s : wikiEntry.getTexts()) {
                logger.info(s);
            }
            queue.push(0);
        }, exception -> {
            fail(exception.getMessage());
            queue.push(0);
        }, null);
        queue.takeFirst();
    }
}