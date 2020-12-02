package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.testutil.*;
import junit.framework.TestCase;

import java.util.UUID;

public class ConcreteInGameChatProcessorTest extends TestCase {

    private final TestableRepeater inGameRepeater = new TestableRepeater();
    private final TestableRepeater outboundRepeater = new TestableRepeater();
    private final TestableInstructionDispatcher instructionDispatcher = new TestableInstructionDispatcher();

    private String repeatMessagePrefix = "98^iuho786I^FG7667";
    private String inGameCommandPrefix = "8^89y7&*7GF5$%57d";

    private final int sleepMillis = 500;

    private ConcreteInGameChatProcessor processor;

    private void initChatProcessor() {
        processor = new ConcreteInGameChatProcessor(
                repeatMessagePrefix,
                inGameCommandPrefix,
                inGameRepeater,
                outboundRepeater,
                instructionDispatcher
        );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initChatProcessor();
    }

    public void tearDown() throws Exception {
        processor.close();
    }

    public void testIssueNormalMessage() {
        UUID uuid = UUID.randomUUID();
        TestableInGameMessage message1 = TestableInGameMessage.create(
                "Hello, world!",
                TestableMessageUser.create("user", uuid, "id", "location"),
                TestableProxiedPlayer.createSkeleton()
        );
        processor.issue(message1);
        sleep();
        assertTrue(inGameRepeater.getMessageList().toString(), inGameRepeater.getMessageList().contains(message1));
        assertTrue(outboundRepeater.getMessageList().isEmpty());
        assertTrue(instructionDispatcher.getCommandList().isEmpty());
        assertEquals(1, inGameRepeater.getMessageList().size());
    }

    public void testIssueCommand() {
        UUID uuid = UUID.randomUUID();
        String commandString = "command";
        TestableInGameMessage message1 = TestableInGameMessage.create(
                inGameCommandPrefix + " " + commandString,// !bc <cmd>
                TestableMessageUser.create("user", uuid, "id", "location"),
                TestableProxiedPlayer.createSkeleton()
        );
        processor.issue(message1);
        sleep();
        assertTrue(inGameRepeater.getMessageList().isEmpty());
        assertTrue(outboundRepeater.getMessageList().isEmpty());
        assertTrue(instructionDispatcher.getCommandList().toString(), instructionDispatcher.getCommandList().contains(commandString));
    }

    public void testIssueCommand2() {
        inGameCommandPrefix = "!";
        initChatProcessor();

        UUID uuid = UUID.randomUUID();
        String commandString = "command";
        TestableInGameMessage message1 = TestableInGameMessage.create(
                inGameCommandPrefix + commandString,
                TestableMessageUser.create("user", uuid, "id", "location"),
                TestableProxiedPlayer.createSkeleton()
        );
        processor.issue(message1);
        sleep();
        assertTrue(inGameRepeater.getMessageList().isEmpty());
        assertTrue(outboundRepeater.getMessageList().isEmpty());
        assertTrue(instructionDispatcher.getCommandList().toString(), instructionDispatcher.getCommandList().contains(commandString));
    }

    public void testIssueOutboundMessage() {
        UUID uuid = UUID.randomUUID();
        String messageString = "message";
        TestableInGameMessage message1 = TestableInGameMessage.create(
                repeatMessagePrefix + messageString,
                TestableMessageUser.create("user", uuid, "id", "location"),
                TestableProxiedPlayer.createSkeleton()
        );
        processor.issue(message1);
        sleep();
        assertEquals(1, inGameRepeater.getMessageList().size());
        assertTrue(instructionDispatcher.getCommandList().isEmpty());
        boolean contains = false;
        for (Message m : outboundRepeater.getMessageList()) {
            if (m.getMessage().equals(messageString))
                contains = true;
        }
        assertTrue(contains);
    }

    public void testIssueOutboundMessage2() {
        repeatMessagePrefix = "#";
        initChatProcessor();

        UUID uuid = UUID.randomUUID();
        String messageString = "message";
        TestableInGameMessage message1 = TestableInGameMessage.create(
                repeatMessagePrefix + messageString,
                TestableMessageUser.create("user", uuid, "id", "location"),
                TestableProxiedPlayer.createSkeleton()
        );
        processor.issue(message1);
        sleep();
        assertEquals(1, inGameRepeater.getMessageList().size());
        assertTrue(instructionDispatcher.getCommandList().isEmpty());
        boolean contains = false;
        for (Message m : outboundRepeater.getMessageList()) {
            if (m.getMessage().equals(messageString))
                contains = true;
        }
        assertTrue(contains);
    }

    private void sleep() {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException ignored) {
        }
    }
}