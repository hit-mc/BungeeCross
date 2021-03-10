package com.keuin.bungeecross.mininstruction.context;

import com.keuin.bungeecross.intercommunicate.user.MessageUser;

import java.util.HashMap;

/**
 * Holds all context-related key-value pairs.
 */
public class UserContext {

    private final MessageUser owner;
    private final HashMap<String, Object> map = new HashMap<>();

    public UserContext(MessageUser owner) {
        this.owner = owner;
    }

    /**
     * Get value by key.
     *
     * @param key the key string.
     * @return the value.
     */
    public Object get(String key) {
        return map.get(key);
    }

    /**
     * Set value by key.
     *
     * @param key   the key string.
     * @param value the value.
     * @return the previous value. May be null.
     */
    public Object set(String key, Object value) {
        return map.put(key, value);
    }

    /**
     * Get the owner user of this context.
     *
     * @return the owner.
     */
    public MessageUser getOwner() {
        return owner;
    }
}
