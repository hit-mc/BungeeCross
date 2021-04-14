package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.intercommunicate.user.MessageUserFactory;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import com.keuin.bungeecross.util.wiki.WikiFetcher;

public class WikiExecutor extends AbstractInstructionExecutor {

    private static final String instruction = "wiki";

    public WikiExecutor() {
        // TODO
        super(instruction, "search Minecraft wiki with ease.", new String[0]);
    }

    @Override
    public ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
//        String key = "wiki_counter";
//        //TODO: implement wiki search (via proxy)
//        int cnt = (int) Optional.ofNullable(context.get(key)).orElse(0);
//        context.set(key, ++cnt);
//        echo(echoRepeater, String.format("never mind. (%d)", cnt));
//        return ExecutionResult.SUCCESS;
        if (params.length != 1) {
            echo(echoRepeater, "Invalid parameter.");
            return ExecutionResult.FAILED;
        }
        WikiFetcher.fetchEntry(
                params[0],
                echoRepeater::repeat,
                exception -> echo(echoRepeater, exception.getMessage()),
                MessageUserFactory.getConsoleUser(instruction)
        );
        return ExecutionResult.SUCCESS;
    }
}
