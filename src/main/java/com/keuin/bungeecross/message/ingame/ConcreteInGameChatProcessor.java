package com.keuin.bungeecross.message.ingame;

import com.keuin.bungeecross.message.InGameMessage;
import com.keuin.bungeecross.message.repeater.InGameCommandEchoRepeater;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ConcreteInGameChatProcessor implements InGameChatProcessor {

    private final Logger logger = Logger.getLogger(ConcreteInGameChatProcessor.class.getName());

    private final String repeatMessagePrefix;
    private final String inGameCommandPrefix;

    private final MessageRepeater outboundRepeater;
    private final MessageRepeater inGameRepeater;

    private final InstructionDispatcher instructionDispatcher;
    private final ChatProcessorDispatcher dispatcher = new ChatProcessorDispatcher();

    /**
     * Construct a in-game chat message processor,
     * which handles message transforming between servers and mc-redis.
     * @param repeatMessagePrefix the prefix marks that the message should be sent to Redis.
     */
    public ConcreteInGameChatProcessor(String repeatMessagePrefix, String inGameCommandPrefix, MessageRepeater inGameRepeater, MessageRepeater outboundRepeater, InstructionDispatcher instructionDispatcher) {
        this.repeatMessagePrefix = repeatMessagePrefix;
        if(repeatMessagePrefix == null)
            throw new IllegalArgumentException("repeatMessagePrefix must not be null.");
        this.inGameCommandPrefix = inGameCommandPrefix;
        if(inGameCommandPrefix == null)
            throw new IllegalArgumentException("inGameCommandPrefix must not be null.");
        this.inGameRepeater = inGameRepeater;
        if(inGameRepeater == null)
            throw new IllegalArgumentException("inGameRepeater must not be null.");
        this.outboundRepeater = outboundRepeater; // Connect to Redis server here.
        if(outboundRepeater == null)
            throw new IllegalArgumentException("redisManager must not be null.");
        this.instructionDispatcher = instructionDispatcher;
        if(instructionDispatcher == null)
            throw new IllegalArgumentException("instructionDispatcher must not be null.");
        dispatcher.start();
    }

    private synchronized void process(InGameMessage message) {
        logger.info(String.format("InGameChatProcessor: processing message %s", message.toString()));

        // process as a command
        if(message.getMessage().toLowerCase().startsWith(inGameCommandPrefix.toLowerCase())) {
            logger.info("Process as a command");
            String cmd = message.getMessage();
            if (cmd.length() > inGameCommandPrefix.length()) {
                int offset = (inGameCommandPrefix.length() == 1) ? 0 : 1; // support both `!<cmd>` and `!bc <cmd>`
                cmd = cmd.substring(inGameCommandPrefix.length() + offset);
            } else {
                cmd = "";
            }

            // delegate to the dispatcher
            instructionDispatcher.dispatchExecution(cmd, new InGameCommandEchoRepeater(message.getProxiedPlayer()));
            return;
        }

        // repeat to other servers
        logger.info("Repeat to other servers.");
        inGameRepeater.repeat(message);

        // repeat to Redis
        if(message.getMessage().startsWith(repeatMessagePrefix) &&
                message.getMessage().length() > repeatMessagePrefix.length()) {
            logger.info("Repeat to Redis.");
            outboundRepeater.repeat(new InGameMessage(
                    message.getMessage().substring(repeatMessagePrefix.length()), // remove the heading repeat prefix
                    message.getSender(),
                    message.getProxiedPlayer()
            ));
        }
    }

    @Override
    public void issue(InGameMessage message) {
        // add to queue
        dispatcher.issue(message);
    }

    @Override
    public void close() {
        if (dispatcher.isAlive())
            dispatcher.interrupt();
    }

    private class ChatProcessorDispatcher extends Thread {

        private final LinkedBlockingQueue<InGameMessage> messageQueue = new LinkedBlockingQueue<>();
        private final AtomicBoolean running = new AtomicBoolean(true);

        @Override
        public void run() {
            running.set(true);
            try {
                while(running.get()) {
                    InGameMessage message = messageQueue.take();
                    process(message);
                }
            } catch (InterruptedException ignored) {
                logger.info("ChatProcessorDispatcher is interrupted. Quitting.");
                return;
            }
            logger.info("ChatProcessorDispatcher is quitting.");
        }

        public void issue(InGameMessage message) {
            try {
                messageQueue.put(message);
            } catch (InterruptedException ignored) {
                logger.warning(String.format("Interrupted while issuing message %s.", message));
            }
        }
    }
}
