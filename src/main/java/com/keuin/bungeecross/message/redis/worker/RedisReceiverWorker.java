package com.keuin.bungeecross.message.redis.worker;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.RedisConfig;
import com.keuin.bungeecross.message.redis.RedisManager;
import com.keuin.bungeecross.message.repeater.LoggableMessageSource;
import com.keuin.bungeecross.message.repeater.MessageRepeatable;
import com.keuin.bungeecross.message.repeater.RedisUserRepeater;
import com.keuin.bungeecross.message.user.SimpleRepeatableUser;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import com.keuin.bungeecross.recentmsg.HistoryMessageLogger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Receive from Redis server.
 * Send to Minecraft.
 */
public class RedisReceiverWorker extends Thread implements LoggableMessageSource {

    private final Logger logger = Logger.getLogger(RedisReceiverWorker.class.getName());

    private int cooldownMillis = 0;
    private final AtomicBoolean enabled;
    private final RedisConfig redisConfig;
    private final MessageRepeatable inBoundMessageDispatcher;
    private final RedisManager redisManager;

    private final String popQueueName;
    private final int POP_TIMEOUT = 1;
    private final String redisCommandPrefix = "!";
    private InstructionDispatcher instructionDispatcher;

    private final Set<HistoryMessageLogger> loggers = Collections.newSetFromMap(new IdentityHashMap<>());


    public RedisReceiverWorker(AtomicBoolean enableFlag, RedisConfig config, MessageRepeatable inBoundMessageDispatcher, RedisManager redisManager) {
        this.enabled = enableFlag;
        this.redisConfig = config;
        this.inBoundMessageDispatcher = inBoundMessageDispatcher;
        this.popQueueName = config.getPopQueueName();
        this.redisManager = redisManager;
    }

    public void setInstructionDispatcher(InstructionDispatcher instructionDispatcher) {
        this.instructionDispatcher = instructionDispatcher;
    }

    @Override
    public void run() {
        try {
            while (enabled.get()) {
                if (cooldownMillis > 0) // cool down after encountered a failure
                    Thread.sleep(cooldownMillis);
                try (Jedis jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false)) {
                    jedis.auth(redisConfig.getPassword());

                    // receive from Redis
                    List<String> list;
                    try {
                        while (enabled.get()) {
                            list = jedis.brpop(POP_TIMEOUT, popQueueName);
                            if (list == null) {
                                Thread.sleep(1000);
                                continue;
                            }

                            for (String rawSting : list) {
                                Message inboundMessage = Message.fromRedisRawString(rawSting);
                                if (inboundMessage != null) {
                                    boolean isCommand = inboundMessage.getMessage().startsWith(redisCommandPrefix);
                                    // send to Minecraft
                                    if (!isCommand) {
                                        logger.info(String.format("Received inbound message: %s (rawString=%s).", inboundMessage, rawSting));
                                        inBoundMessageDispatcher.repeat(inboundMessage);

                                        // send to loggers
                                        loggers.forEach(logger -> logger.recordMessage(inboundMessage));
                                    }

                                    // Execute instruction
                                    if (isCommand && instructionDispatcher != null) {
                                        String cmd = inboundMessage.getMessage();
                                        if (redisCommandPrefix.length() == cmd.length())
                                            cmd = "";
                                        else
                                            cmd = cmd.substring(redisCommandPrefix.length());
                                        // dispatch the command
                                        instructionDispatcher.dispatchExecution(
                                                cmd,
                                                new SimpleRepeatableUser(
                                                        inboundMessage.getSender(),
                                                        new RedisUserRepeater(
                                                                redisManager,
                                                                inboundMessage.getSender()
                                                        )
                                                )
                                        );
                                    }
                                } else {
                                    logger.warning(String.format("Malformed inbound message: %s. Ignored.", rawSting));
                                }
                            }

                            if (cooldownMillis > 0) {
                                cooldownMillis = 0; // success. Reset cool down time interval.
                                logger.info("Connection recovered. Set receiver cool down to 0.");
                            }
                        }
                    } catch (JedisException e) {
                        cooldownMillis += 1000;
                        logger.warning(String.format("Failed to pop message: %s. Retrying... (wait for %dms)", e, cooldownMillis));
                    }

                } catch (JedisConnectionException e) {
                    cooldownMillis += 1000;
                    logger.severe(String.format("Failed to connect Redis server: %s. Set cool down time interval to %dms.", e, cooldownMillis));
                }
            } // while enabled
        } catch (InterruptedException ignored) {
            logger.info("Receiver thread was interrupted. Quitting.");
        }
        logger.info("Receiver thread stopped.");
    } // void run()

    @Override
    public void registerHistoryLogger(HistoryMessageLogger historyMessageLogger) {
        loggers.add(historyMessageLogger);
    }
}