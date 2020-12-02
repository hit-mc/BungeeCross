package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.context.UserContext;

import java.util.Optional;

public class WikiExecutor extends AbstractInstructionExecutor {

    public WikiExecutor(String discription, String[] params) {
        super("wiki", discription, params);
    }

    private static final WikiExecutor INSTANCE = new WikiExecutor(
            "search on Minecraft wiki.",
            new String[0]
    );

    public static WikiExecutor getInstance() {
        return INSTANCE;
    }

    protected WikiExecutor(String instruction, String description, String[] params) {
        super(instruction, description, params);
    }

    @Override
    public void doExecute(UserContext context, MessageRepeater echoRepeater) {
        String key = "wiki_counter";
        int cnt = (int) Optional.ofNullable(context.get(key)).orElse(0);
        cnt++;
        context.set(key, cnt);
        echo(echoRepeater, String.format("never mind. (%d)", cnt));
    }
}