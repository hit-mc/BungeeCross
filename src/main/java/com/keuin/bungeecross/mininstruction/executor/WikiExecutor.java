package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;

import java.util.Optional;

public class WikiExecutor extends AbstractInstructionExecutor {

    public WikiExecutor() {
        // TODO
        super("wiki", "search Minecraft wiki with ease.", new String[0]);
    }

    @Override
    public ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
        String key = "wiki_counter";
        //TODO: implement wiki search (via proxy)
        int cnt = (int) Optional.ofNullable(context.get(key)).orElse(0);
        cnt++;
        context.set(key, cnt);
        echo(echoRepeater, String.format("never mind. (%d)", cnt));
        return ExecutionResult.SUCCESS;
    }
}
