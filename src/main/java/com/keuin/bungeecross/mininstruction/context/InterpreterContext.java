package com.keuin.bungeecross.mininstruction.context;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Hold all users' context.
 */
public class InterpreterContext {

    private final Map<MessageUser, UserContext> map = new HashMap<>();

    /**
     * Get the user's context.
     *
     * @param user the user
     * @return the context
     */
    public synchronized UserContext getUserContext(MessageUser user) {
        UserContext context = map.get(user);
        if (context == null) {
            context = new UserContext(user);
            map.put(user, context);
        }
        return context;
    }
}
