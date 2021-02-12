package com.keuin.bungeecross.recentmsg;

import com.keuin.bungeecross.message.Message;

/**
 * A history message logger is able to log messages sent to it, and keep them for a certain time range.
 */
public interface HistoryMessageLogger {
    /**
     * Add a just-sent message and record it. Sent time will be set to current local time.
     * @param message the message.
     */
    void recordMessage(Message message);
}
