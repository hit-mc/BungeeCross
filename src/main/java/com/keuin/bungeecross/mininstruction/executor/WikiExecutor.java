package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.intercommunicate.user.MessageUserFactory;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import com.keuin.bungeecross.wiki.WikiFetcher;

import java.net.Proxy;
import java.util.Objects;
import java.util.Optional;

public class WikiExecutor extends AbstractInstructionExecutor {

    private static final String instruction = "wiki";
    private final WikiFetcher fetcher;

    public WikiExecutor(Proxy proxy) {
        super(instruction, "search Minecraft wiki with ease.", new String[0]);
        Objects.requireNonNull(proxy);
        this.fetcher = new WikiFetcher(proxy);
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
        fetcher.fetchEntry(
                params[0],
                echoRepeater::repeat,
                exception -> echo(echoRepeater, Optional.ofNullable(exception.getLocalizedMessage()).orElse(exception.toString())),
                MessageUserFactory.getConsoleUser(instruction)
        );
        return ExecutionResult.SUCCESS;
    }
}
