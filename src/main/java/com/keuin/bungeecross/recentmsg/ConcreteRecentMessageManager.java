package com.keuin.bungeecross.recentmsg;

import com.keuin.bungeecross.intercommunicate.message.HistoryMessage;
import com.keuin.bungeecross.intercommunicate.message.Message;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

public class ConcreteRecentMessageManager implements RecentMessageManager {

    private final Logger logger = Logger.getLogger(ConcreteRecentMessageManager.class.getName());
    private final long maxMessageLifeSeconds; // how long the messages should be kept in the manager.
    private final Deque<HistoryMessage> messages = new LinkedList<>();
    private long checkId = 0;

    public ConcreteRecentMessageManager(long maxMessageLifeSeconds) {
        this.maxMessageLifeSeconds = maxMessageLifeSeconds;
    }

    @Override
    public synchronized Collection<HistoryMessage> getRecentMessages() {
        optimizeQueue(true);
        return new ArrayList<>(messages);
    }

    @Override
    public synchronized void recordMessage(Message message) {
        logger.info("Record message " + message.getMessage());
        // a slightly but may incorrect optimization: we assume that
        // the threshold is large enough, so we can optimize-then-add
        // rather than add-then-optimize, which always iterate one time less,
        // but me miss the newly added message when optimizing
        // (when the threshold is very small, and we ignore that)
        optimizeQueue();
        messages.addLast(new HistoryMessage(message, LocalDateTime.now()));
    }

    /**
     * Thead unsafe!
     */
    private void optimizeQueue() {
        optimizeQueue(false);
    }

    /**
     * Thead unsafe!
     */
    private void optimizeQueue(boolean forceCheck) {
        if (++checkId == 40 || forceCheck) {
            checkId = 0; // reduce check count by using an internal counter.
            int messagesDeleted = 0;
            while (true) {
                HistoryMessage oldestMessage = messages.peekFirst();
                if (oldestMessage != null && (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                        - oldestMessage.getSentTime().toEpochSecond(ZoneOffset.UTC)
                        >= maxMessageLifeSeconds)) {
//                    logger.info("Message " + oldestMessage + " is too old. Delete it.");
                    ++messagesDeleted;
                    messages.pollFirst(); // too old, remove
                } else {
                    break;
                }
            }
            logger.info("Deleted " + messagesDeleted + " old message(s).");
        }
    }
}
