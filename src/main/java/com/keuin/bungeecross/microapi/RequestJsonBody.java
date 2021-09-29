package com.keuin.bungeecross.microapi;

public class RequestJsonBody {
    private final String sender;
    private final String message;
    private final String target; // in, out, both

    public RequestJsonBody() {
        this.sender = null;
        this.message = null;
        this.target = "out";
    }

    public MethodMessage toMessage() {
        return new MethodMessage(sender, message);
    }

    public boolean sendToGame() {
        return target.equalsIgnoreCase("in") || target.equalsIgnoreCase("both");
    }

    public boolean sendToOutbound() {
        return target.equalsIgnoreCase("out") || target.equalsIgnoreCase("both");
    }
}
