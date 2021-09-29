package com.keuin.bungeecross.intercommunicate.pubsub;

import com.keuin.bungeecross.config.ConfigManager;
import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.msghandler.InboundMessageHandler;
import com.keuin.bungeecross.intercommunicate.pubsub.worker.AbstractMessageSubscriber;
import com.keuin.bungeecross.intercommunicate.pubsub.worker.MessagePublisher;
import com.keuin.bungeecross.intercommunicate.pubsub.worker.MessageSubscriber;
import com.keuin.bungeecross.intercommunicate.repeater.LoggableMessageSource;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import com.keuin.bungeecross.recentmsg.HistoryMessageLogger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * This class manages connections to PSMB and their inbound/outbound queues.
 * It handles message input and output.
 */
@SuppressWarnings("FieldCanBeLocal")
public class BrokerManager implements MessageRepeatable, LoggableMessageSource {

    private final Logger logger = Logger.getLogger(BrokerManager.class.getName());

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final InboundMessageHandler inboundMessageHandler;
    private final InstructionDispatcher instructionDispatcher;

    private final MessagePublisher publisher;
    private final AbstractMessageSubscriber subscriber;

    public BrokerManager(MessageRepeatable inBoundMessageDispatcher, InstructionDispatcher instructionDispatcher) {
        var brokerConfig = ConfigManager.INSTANCE.getRootConfig().getBrokerServer();
        logger.info(String.format("%s created with PSMB info: %s", this.getClass().getName(), brokerConfig.toString()));
        this.instructionDispatcher = Objects.requireNonNull(instructionDispatcher);
        this.publisher = new MessagePublisher(brokerConfig, enabled);
        this.inboundMessageHandler = new InboundMessageHandler(
                instructionDispatcher, inBoundMessageDispatcher,
                this.publisher, brokerConfig.getCommandPrefix(),
                brokerConfig.getChatRelayPrefix());
        this.subscriber = new MessageSubscriber(brokerConfig, inboundMessageHandler);
    }

    public synchronized void start() {
        logger.info("Starting publisher and subscriber...");
        enabled.set(true);
        if (!publisher.isAlive()) {
            logger.info("Start publisher.");
            publisher.start();
        }
        if (!subscriber.isAlive()) {
            logger.info("Start subscriber.");
            subscriber.start();
        }
    }

    public synchronized void stop() {
        logger.info("BrokerManager is stopping...");
        enabled.set(false);
        try {
            if (publisher.isAlive()) {
                logger.info("Stopping publisher...");
                publisher.interrupt();
                publisher.join(5000);
            }
        } catch (InterruptedException ignored) {
        }
        try {
            if (subscriber.isAlive()) {
                logger.info("Stopping subscriber...");
                try {
                    subscriber.close();
                } catch (Exception ignored) {
                }
                subscriber.interrupt();
                subscriber.join(5000);
            }
        } catch (InterruptedException ignored) {
        }
    }

    public boolean isSenderAlive() {
        return publisher.isAlive();
    }

    public boolean isReceiverAlive() {
        return subscriber.isAlive();
    }

    @Override
    public void repeat(Message message) {
        publisher.repeat(message);
    }

    @Override
    public void registerHistoryLogger(HistoryMessageLogger historyMessageLogger) {
        Objects.requireNonNull(historyMessageLogger);
        logger.info("Registering history msg logger " + historyMessageLogger + ".");
        subscriber.registerHistoryLogger(historyMessageLogger);
    }
}
