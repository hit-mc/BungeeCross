package com.keuin.bungeecross.intercommunicate.repeater;

import com.keuin.bungeecross.recentmsg.HistoryMessageLogger;

/**
 * A message source is a handler that receives all messages from a certain source, such as IM chat room, in-game chat, etc.
 * A loggable message source is able to accept (or say `register`) history loggers (especially a history message manager),
 * which logs all history messages and keep them for a certain time range.
 * The class utilizes visitor pattern.
 */
public interface LoggableMessageSource {
    /**
     * Register a history logger, which will receive all messages from this message source in the future.
     * @param historyMessageLogger the message logger.
     */
    void registerHistoryLogger(HistoryMessageLogger historyMessageLogger);
}
