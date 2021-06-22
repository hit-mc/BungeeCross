package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.config.ProxyConfig;
import com.keuin.bungeecross.config.mutable.MutableProxyConfig;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import com.keuin.bungeecross.testutil.TestableMessageUser;
import com.keuin.bungeecross.testutil.TestableRepeatable;
import org.junit.Test;

import java.net.Proxy;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.*;

public class WikiExecutorTest {

    @Test
    public void testNormal() throws InterruptedException {
        var ctx = new UserContext(new TestableMessageUser("name",
                UUID.randomUUID(), "id", "location"));
        var wiki = new WikiExecutor(Proxy.NO_PROXY);
        var que = new LinkedBlockingDeque<>();
        var repeatable = new TestableRepeatable(msg -> que.add(0));
        wiki.doExecute(ctx, repeatable, new String[]{"圆石"});
        que.takeFirst();
        assertEquals("No message sent.", 1, repeatable.getMessageList().size());
        var msg = repeatable.getMessageList().get(0);
        assertTrue(String.format("Message (len=%d) is too short.", msg.getMessage().length()),
                msg.getMessage().length() > 10);
    }

    @Test
    public void testInvalid() throws InterruptedException {
        var ctx = new UserContext(new TestableMessageUser("name",
                UUID.randomUUID(), "id", "location"));
        var wiki = new WikiExecutor(Proxy.NO_PROXY);
        var repeatable = new TestableRepeatable();
        wiki.doExecute(ctx, repeatable, new String[]{"123ibu43yu23"});
        Thread.sleep(5000);
        assertEquals(1, repeatable.getMessageList().size());
        var msg = repeatable.getMessageList().get(0);
        assertNotNull(msg.getMessage());
        assertTrue(msg.getMessage().toLowerCase(Locale.ROOT).contains("no such entry"));
    }

    @Test
    public void testProxy() throws InterruptedException {
        var ctx = new UserContext(new TestableMessageUser("name",
                UUID.randomUUID(), "id", "location"));
        var wiki = new WikiExecutor(new MutableProxyConfig("http://127.0.0.1:10809").getProxy());
        var que = new LinkedBlockingDeque<>();
        var repeatable = new TestableRepeatable(msg -> que.add(0));
        wiki.doExecute(ctx, repeatable, new String[]{"圆石"});
        que.takeFirst();
        assertEquals("No message sent.", 1, repeatable.getMessageList().size());
        var msg = repeatable.getMessageList().get(0);
        assertTrue(String.format("Message (len=%d) is too short.", msg.getMessage().length()),
                msg.getMessage().length() > 10);
    }
}