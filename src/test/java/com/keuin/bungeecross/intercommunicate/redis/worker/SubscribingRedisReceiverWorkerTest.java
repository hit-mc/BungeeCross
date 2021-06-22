package com.keuin.bungeecross.intercommunicate.redis.worker;

import com.google.gson.Gson;
import com.keuin.bungeecross.config.mutable.MutableRedisConfig;
import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.testutil.TestConfig;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class SubscribingRedisReceiverWorkerTest {

    private final Logger logger = Logger.getLogger(SubscribingRedisReceiverWorkerTest.class.getName());
    private TestConfig config;

    /**
     * Load Redis server (for test-only) configuration.
     */
    @Before
    public void setUp() throws Exception {
        try {
            Reader configReader = Files.newBufferedReader(Path.of("test_config.json"));
            config = (new Gson()).fromJson(configReader, TestConfig.class);
            if (config.host == null)
                throw new RuntimeException();
            logger.info(config.toString());
        } catch (NoSuchFileException | RuntimeException e) {
            fail("Please configure your test Redis server in `test_config.json`" +
                    "according to `TestConfig.java`.");
        }
    }

    /**
     * Ensure we can send and receive messages.
     */
    @Test
    public void testSubscribe() throws InterruptedException {
        int failCounter = 0;
        while (failCounter < 3) {
            try {
                var topicPrefix = "bcdev.";
                var senderTopic = "test-topic_sender";
                var receiverTopic = "test-topic_receiver";
                var senderEndpoint = "test-endpoint_sender";
                var receiverEndpoint = "test-endpoint_receiver";

                var flg = new AtomicBoolean(true);
                var que = new LinkedBlockingDeque<>();
                var senderConfig = new MutableRedisConfig(config.host, config.port, config.password,
                        senderTopic, senderEndpoint, topicPrefix);
                var receiverConfig = new MutableRedisConfig(config.host, config.port, config.password,
                        receiverTopic, receiverEndpoint, topicPrefix);

                var msgReceived = new Message[1];
                var receiver = new MessageRepeatable() {
                    @Override
                    public void repeat(Message message) {
                        msgReceived[0] = message;
                        flg.set(false);
                        que.add(0);
                    }
                };
                logger.info("starting sender and receiver...");

                var receiveWorker = new SubscribingRedisReceiverWorker(receiverConfig, receiver::repeat);
                var sendWorker = new RedisSenderWorker(senderConfig, flg);
                sendWorker.start();
                receiveWorker.start();

                var msg = Message.build("test message\n测试消息", "test sender");
                logger.info("sending message...");
                sendWorker.repeat(msg);
                logger.info("waiting receiver to respond...");
                if (que.poll(5, TimeUnit.SECONDS) == null)
                    throw new RuntimeException("receiver timed out."); // timed out
                assertNotNull(msgReceived[0]);
                assertEquals("received message does not equal to what had been sent",
                        msg.getMessage(), msgReceived[0].getMessage());
                return;
            } catch (RuntimeException ignored) {
                logger.warning("Receiver timed out. Retry.");
            }
            ++failCounter;
        }
        fail("Test failed. Maybe timed out too many times.");
    }

    /**
     * Ensure we won't receive what we *ourselves* have sent. (message looping)
     */
    @Test
    public void testNotReceivingSelfSentMessages() throws InterruptedException {
        var topicPrefix = "bcdev.";
        var senderTopic = "test-topic_sender";
        var senderEndpoint = "test-endpoint_sender";

        var flg = new AtomicBoolean(true);
        var que = new LinkedBlockingDeque<>();
        var senderConfig = new MutableRedisConfig(config.host, config.port, config.password,
                senderTopic, senderEndpoint, topicPrefix);

        var msgReceived = new Message[1];
        var receiver = new MessageRepeatable() {
            @Override
            public void repeat(Message message) {
                msgReceived[0] = message;
                flg.set(false);
                que.add(0);
            }
        };
        logger.info("starting sender and receiver...");

        var receiveWorker = new SubscribingRedisReceiverWorker(senderConfig, receiver::repeat);
        var sendWorker = new RedisSenderWorker(senderConfig, flg);
        sendWorker.start();
        receiveWorker.start();

        var msg = Message.build("test message\n测试消息", "test sender");
        logger.info("sending message...");
        sendWorker.repeat(msg);
        logger.info("waiting receiver to respond...");
        assertNull("Received a message which we should not receive",
                que.poll(5, TimeUnit.SECONDS));
        assertNull("Received a message which we should not receive", msgReceived[0]);
    }
}