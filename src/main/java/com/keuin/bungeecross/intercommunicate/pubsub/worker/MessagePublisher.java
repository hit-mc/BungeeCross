package com.keuin.bungeecross.intercommunicate.pubsub.worker;

import com.keuin.bungeecross.config.MessageBrokerConfig;
import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.util.MessageUtil;
import com.keuin.psmb4j.PublishClient;
import com.keuin.psmb4j.error.CommandFailureException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Send messages received from Minecraft, to broker.
 */
public class MessagePublisher extends Thread implements MessageRepeatable {

    private final Logger logger = Logger.getLogger(MessagePublisher.class.getName());

    private final MessageBrokerConfig config;
    private final AtomicBoolean enabled;
    private final int joinWaitMillis = 125;
    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private final int sendCoolDownMillis = 500;
    private final String topicString;
    private PublishClient client = null;

    public MessagePublisher(MessageBrokerConfig messageBrokerConfig, AtomicBoolean enabled) {
        this.config = messageBrokerConfig;
        this.enabled = enabled;
        this.topicString = (messageBrokerConfig.getTopicPrefix() + messageBrokerConfig.getTopicId());
        logger.info(String.format("Set sender topic id to `%s`.", topicString));
    }


    /**
     * Try to release old client instance and reconnect.
     * May failed silently.
     */
    private void resetClient() {
        try {
            if (client != null) {
                client.close();
            }
            client = new PublishClient(config.getHost(), config.getPort(), topicString,
                    config.getKeepAliveIntervalMillis(), e -> {
                e.printStackTrace();
                resetClient();
            });
        } catch (IOException | CommandFailureException e) {
            logger.severe(String.format("Failed to connect/disconnect: %s", e));
        }
    }

    @Override
    public void run() {
        try {
            while (enabled.get()) { // while running

                resetClient();
                while (enabled.get()) {
                    // process the queue
                    handleSendQueue(); // may be interrupted

                    // send cool down, prevent spamming
                    Thread.sleep(sendCoolDownMillis);

//                        logger.info("Sender thread is stopped.");
                }

            }
        } catch (InterruptedException exception) {
            logger.info("Sender thread was interrupted. Quitting.");
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Send a message to the broker server. The message is guaranteed to be sent to the remote.
     * (otherwise `enabled` is set to false or interrupted)
     *
     * @param message the message to be sent.
     */
    private void sendMessage(Message message) throws InterruptedException {
        int failureCoolDownMillis = 0; // failure cool down
        // send outbound message
        while (enabled.get()) {
            try {
                client.publish(message.pack(config.getEndpointName()));
                logger.info("Message was sent to the broker.");
                return;
            } catch (Exception e) {
                logger.warning(String.format("Failed to push message: %s.", e));
            }
            failureCoolDownMillis += 1000;
            logger.info(String.format("Reconnecting sender client... (wait for %dms)", failureCoolDownMillis));
            Thread.sleep(failureCoolDownMillis);
            // failed. reset client
            resetClient();
        }
    }

    private void handleSendQueue() throws InterruptedException {
//            processPendingMessage(); // process the pending message firstly.

        Message firstMessage = sendQueue.take();
        if (config.getMaxJoinedMessageCount() > 1 && firstMessage.ifCanBeJoined()) {

            List<Message> joinList = new ArrayList<>(); // messages should be joined before sent (always contains the first message).
            joinList.add(firstMessage);

            Message tailMessage = null; // the last message that should be sent separately (if has).

            // get next messages with max count maxJoinedMessageCount.
            for (int i = 0; i < config.getMaxJoinedMessageCount() - 1; ++i) {
                Message nextMessage = sendQueue.poll(joinWaitMillis, TimeUnit.MILLISECONDS);
                if (nextMessage == null) {
                    // no more messages
                    // just send the joinList as a single message.
                    break;
                } else if (!nextMessage.ifCanBeJoined() || !nextMessage.getSender().equals(firstMessage.getSender())) {

                    // the next message is not join-able.
                    // they have to be sent separately.
                    // 1. join the joinList and send them as a single message.
                    // 2. send the nextMessage as a single standalone message.
                    tailMessage = nextMessage;
                    break;
                } else {
                    joinList.add(nextMessage); // this message should be joined
                }
            }

            // send the (1st) joined message.
            sendMessage(MessageUtil.joinMessages(joinList));

            // send the (2nd) separated message nextMessage.
            if (tailMessage != null)
                sendMessage(tailMessage);

        } else {
            // The first message is not joinable and should be sent separately.
            sendMessage(firstMessage);
        }
    }

    @Override
    public void repeat(Message message) {
        sendQueue.add(message);
    }
}