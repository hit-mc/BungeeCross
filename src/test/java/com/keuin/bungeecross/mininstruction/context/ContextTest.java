package com.keuin.bungeecross.mininstruction.context;

import com.keuin.bungeecross.testutil.TestableMessageUser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ContextTest {
    @Test
    public void testInterpreterContext() {
        InterpreterContext context = new InterpreterContext();
        TestableMessageUser user1 = TestableMessageUser.create("123", null, "456", "");
        TestableMessageUser user2 = TestableMessageUser.create("abc", null, "def", "");
        UserContext context1 = context.getUserContext(user1);
        UserContext context2 = context.getUserContext(user2);
        String key1 = "testkey1";
        String key2 = "Testkey1";
        String value1 = "value1";
        String value2 = "value2";
        assertNull(context1.get(key1));
        assertNull(context2.get(key1));
        assertNull(context1.get(key2));
        assertNull(context2.get(key2));
        assertNull(context1.set(key1, value1));
        assertEquals(value1, context1.get(key1));
        assertEquals(value1, context1.get(key1));
        assertNull(context1.get(key2));
        assertNull(context2.get(key2));
    }

    @Test
    public void testOwner() {
        InterpreterContext context = new InterpreterContext();
        TestableMessageUser user1 = TestableMessageUser.create("123", null, "456", "");
        TestableMessageUser user2 = TestableMessageUser.create("abc", null, "def", "");
        UserContext context1 = context.getUserContext(user1);
        UserContext context2 = context.getUserContext(user2);
        assertEquals(user1, context1.getOwner());
        assertEquals(user2, context2.getOwner());
    }
}