package com.keuin.bungeecross.recentmsg;

import com.keuin.bungeecross.intercommunicate.message.HistoryMessage;

import java.util.Collection;

/**
 * A recent message manager records recent messages in a certain time range, provides a getter to access them,
 * in the form of `HistoryMessage`, which adds send time belong with the original message.
 */
public interface RecentMessageManager extends HistoryMessageLogger {
    /**
     * Get recent messages.
     * @return all recent messages.
     */
    Collection<HistoryMessage> getRecentMessages();
}
