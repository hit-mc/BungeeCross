package com.keuin.bungeecross.recentmsg;

import com.keuin.bungeecross.message.HistoryMessage;

import java.util.Collection;

public interface RecentMessageManager extends HistoryMessageLogger {
    /**
     * Get recent messages.
     * @return all recent messages.
     */
    Collection<HistoryMessage> getRecentMessages();
}
