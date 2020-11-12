package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Receive from Redis server.
 * Send to Minecraft.
 */
class RedisReceiverThread extends Thread {

    private final Logger logger = Logger.getLogger(RedisReceiverThread.class.getName());

    private final AtomicBoolean enabled;
    private int cooldownMillis = 0;

    private final RedisConfig redisConfig;
    private final InBoundMessageDispatcher inBoundMessageDispatcher;
    private final InstructionDispatcher instructionDispatcher;
    private final MessageRepeater instructionEchoRepeater;

    public RedisReceiverThread(AtomicBoolean enabled, RedisConfig redisConfig, InBoundMessageDispatcher inBoundMessageDispatcher, InstructionDispatcher instructionDispatcher, MessageRepeater instructionEchoRepeater) {
        super(RedisReceiverThread.class.getName());
        this.enabled = enabled;
        this.redisConfig = redisConfig;
        this.inBoundMessageDispatcher = inBoundMessageDispatcher;
        this.instructionDispatcher = instructionDispatcher;
        this.instructionEchoRepeater = instructionEchoRepeater;
    }

    @Override
    public void run() {
        try {
            while (enabled.get()) {
                if (cooldownMillis > 0) // cool down after encountered a failure
                    Thread.sleep(cooldownMillis);

                try (Jedis jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false)) {
                    jedis.auth(redisConfig.getPassword());

                    while (enabled.get()) {
                        // receive from Redis
                        List<String> list = null;
                        int retryCounter = redisConfig.getMaxRetryTimes();
                        while (retryCounter > 0) {
                            try {
                                list = jedis.brpop(redisConfig.getPopTimeoutSeconds(), redisConfig.getPopQueueName());
                            } catch (JedisException e) {
                                logger.warning(String.format("Failed to pop message: %s. Retrying... (remaining times: %d)", e, retryCounter));
                                --retryCounter;
                                continue;
                            }
                            if (cooldownMillis > 0) {
                                cooldownMillis = 0; // success. Reset cool down time interval.
                                logger.info("Connection is recovered. Receiver cool down time is set to 0.");
                            }
                            break;
                        }
                        // max retry times reached. Increment the cool down time interval.
                        if (retryCounter == 0) {
                            cooldownMillis += 1000;
                            logger.severe(String.format("Max retry times reached. Cool down time interval increased to %dms.", cooldownMillis));
                            break; // break to reconnect to the Redis
                        }

                        if (list != null) {
                            for (String rawSting : list) {
                                Message inboundMessage = Message.fromRedisRawString(rawSting);
                                if (inboundMessage != null) {
                                    boolean isCommand = inboundMessage.getMessage().startsWith(redisConfig.getRedisCommandPrefix());
                                    // send to Minecraft
                                    if (!isCommand) {
                                        logger.info(String.format("Received inbound message: %s (rawString=%s).", inboundMessage, rawSting));
                                        inBoundMessageDispatcher.repeatInboundMessage(inboundMessage);
                                    }

                                    // Execute instruction
                                    if (isCommand && instructionDispatcher != null) {
                                        String cmd = inboundMessage.getMessage();
                                        if (redisConfig.getRedisCommandPrefix().length() == cmd.length())
                                            cmd = "";
                                        else
                                            cmd = cmd.substring(redisConfig.getRedisCommandPrefix().length());
                                        // dispatch the command
                                        instructionDispatcher.dispatchExecution(cmd, instructionEchoRepeater);
                                    }
                                } else {
                                    logger.warning(String.format("Malformed inbound message: %s. Ignored.", rawSting));
                                }
                            }
                        } else {
                            Thread.sleep(1000);
                        }
                    }

                } catch (JedisConnectionException e) {
                    cooldownMillis += 1000;
                    logger.severe(String.format("Failed to connect Redis server: %s. Cool down time interval is set to %dms.", e, cooldownMillis));
                }
                // while enabled
            }
        } catch (InterruptedException ignored) {
            logger.info("Receiver thread was interrupted. Quitting.");
        }

        logger.info("Receiver thread is stopped.");
    } // void run()
}
