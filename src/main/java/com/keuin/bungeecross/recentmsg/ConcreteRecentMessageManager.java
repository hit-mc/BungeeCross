package com.keuin.bungeecross.recentmsg;

import com.keuin.bungeecross.message.HistoryMessage;
import com.keuin.bungeecross.message.Message;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

public class ConcreteRecentMessageManager implements RecentMessageManager {

    private final long keptSecondsThreshold;
    private final Deque<HistoryMessage> messages = new LinkedList<>();
    private long checkId = 0;

    public ConcreteRecentMessageManager(long keptSecondsThreshold) {
        this.keptSecondsThreshold = keptSecondsThreshold;
    }

    public ConcreteRecentMessageManager() {
        this.keptSecondsThreshold = 600;
    }

    @Override
    public synchronized Iterable<HistoryMessage> getRecentMessages() {
        optimizeQueue(true);
        return new ArrayList<>(messages);
    }

    @Override
    public synchronized void recordMessage(Message message) {
        messages.addLast(new HistoryMessage(message, LocalDateTime.now()));
        optimizeQueue();
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
        if (++checkId == 20 || forceCheck) {
            checkId = 0; // reduce check count by using an internal counter.
            while (true) {
                HistoryMessage oldestMessage = messages.peekFirst();
                if (oldestMessage != null && (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                        - oldestMessage.getSentTime().toEpochSecond(ZoneOffset.UTC)
                        >= keptSecondsThreshold)) {
                    messages.pollFirst(); // too old, remove
                } else {
                    break;
                }
            }
        }
    }
}
