package com.keuin.bungeecross.intercommunicate.pubsub.worker;

import com.keuin.bungeecross.config.MessageBrokerConfig;
import com.keuin.bungeecross.intercommunicate.message.FixedTimeMessage;
import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.recentmsg.HistoryMessageLogger;
import com.keuin.psmb4j.SubscribeClient;
import com.keuin.psmb4j.error.CommandFailureException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class MessageSubscriber extends AbstractMessageSubscriber {

    private final Logger logger = Logger.getLogger(MessageSubscriber.class.getName());
    private final Set<HistoryMessageLogger> loggers = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Consumer<Message> inboundMessageHandler;
    private final MessageBrokerConfig config;
    private SubscribeClient client = null;

    public MessageSubscriber(MessageBrokerConfig messageBrokerConfig,
                             Consumer<Message> inboundMessageHandler) {
        this.inboundMessageHandler = inboundMessageHandler;
        this.config = messageBrokerConfig;
    }

    private static String getSubscribePattern(String prefix, String selfTopic) {
        // TODO: handle or filter special chars
        prefix = prefix.replace(".", "\\.");
        selfTopic = selfTopic.replace(".", "\\.");
        return String.format("%s(?:(?:(?!%s).+)|(?:%s.+))$", prefix, selfTopic, selfTopic);
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                var subscribePattern = getSubscribePattern(config.getTopicPrefix(), config.getTopicId());
                logger.info("Subscribing on " + subscribePattern);
                try {
                    client = new SubscribeClient(config.getHost(), config.getPort(),
                            subscribePattern,
                            config.getKeepAliveIntervalMillis(), this::onMessage, config.getSubscriberId());
                    client.subscribe();
                } catch (IOException | CommandFailureException e) {
                    e.printStackTrace();
                } finally {
                    client = null;
                }
                logger.severe("Waiting for reconnect...");
                Thread.sleep(config.getSubscriberReconnectIntervalMillis());
            }
        } catch (InterruptedException ignored) {
            logger.info("Subscriber is quitting...");
        }
    }

    private void onMessage(ByteBuffer buffer) {
        var bson = buffer.array();
        try {
            logger.info("Received message from broker.");

            var message = Message.unpack(bson);
            logger.info("Unpacked message: " + message);

            var messageCreateTime = message.getCreateTime();
            var timeDelta = Math.abs(System.currentTimeMillis() - messageCreateTime);
            if (timeDelta > 20 * 60 * 1000) {
                logger.warning(String.format("Too far UTC timestamp %d. Potentially wrong time?", messageCreateTime));
            }

            // TODO: this should not be synchronized, or it will block the jedis pool loop
            inboundMessageHandler.accept(message);
        } catch (FixedTimeMessage.IllegalPackedMessageException e) {
            e.printStackTrace();
            Optional.ofNullable(e.getCause())
                    .ifPresent(cause -> {
                        logger.warning("Reason: " + cause.getMessage());
                        cause.printStackTrace();
                    });
        }
    }

    @Override
    public void registerHistoryLogger(HistoryMessageLogger historyMessageLogger) {
        this.loggers.add(historyMessageLogger);
    }
}
