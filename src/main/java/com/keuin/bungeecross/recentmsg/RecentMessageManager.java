package com.keuin.bungeecross.recentmsg;

import com.keuin.bungeecross.message.HistoryMessage;
import com.keuin.bungeecross.message.Message;

public interface RecentMessageManager {
    /**
     * Get recent messages.
     * @return all recent messages.
     */
    Iterable<HistoryMessage> getRecentMessages();

    /**
     * Add a just sent message. Sent time will be set to current local time.
     * @param message the message.
     */
    void recordMessage(Message message);
}
