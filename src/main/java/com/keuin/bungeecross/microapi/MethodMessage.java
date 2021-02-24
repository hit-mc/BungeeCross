package com.keuin.bungeecross.microapi;

import java.util.Objects;
import java.util.Optional;

class MethodMessage {
    private final String sender;
    private final String message;

    public MethodMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Check if the request misses `sender` or `message`.
     *
     * @return if it is valid.
     */
    public boolean isValid() {
        return !Optional.ofNullable(sender).orElse("").isEmpty()
                && !Optional.ofNullable(message).orElse("").isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodMessage that = (MethodMessage) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, message);
    }
}
