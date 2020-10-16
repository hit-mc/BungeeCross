package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * This class manages the Redis connection and its inbound/outbound queue.
 * It handles the message input and output.
 */
public class RedisManager implements MessageRepeater {

    private final Logger logger = Logger.getLogger(RedisManager.class.getName());

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final RedisConfig redisConfig;

    private final String pushQueueName;
    private final String popQueueName;

    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private final SenderThread senderThread = new SenderThread();
    private final ReceiverThread receiverThread = new ReceiverThread();

    private InstructionDispatcher instructionDispatcher;

    private final InBoundMessageDispatcher inBoundMessageDispatcher;

    private final int POP_TIMEOUT = 1;
    private final int MAX_RETRY_TIMES = 10;
    private final String redisCommandPrefix = "!";

    public RedisManager(RedisConfig redisConfig, InBoundMessageDispatcher inBoundMessageDispatcher) {
        logger.info(String.format("%s created with redis info: %s", this.getClass().getName(), redisConfig.toString()));

        this.pushQueueName = redisConfig.getPushQueueName();
        this.popQueueName = redisConfig.getPopQueueName();
        this.inBoundMessageDispatcher = inBoundMessageDispatcher;
        this.redisConfig = redisConfig;
    }

    public synchronized void start() {
        enabled.set(true);
        if (!senderThread.isAlive()) {
            senderThread.start();
        }
        if (!receiverThread.isAlive()) {
            receiverThread.start();
        }
    }

    public synchronized void stop() {
        enabled.set(false);
        if(senderThread.isAlive()) {
            senderThread.interrupt();
        }
        if(receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
    }

    public boolean isSenderAlive() {
        return senderThread.isAlive();
    }

    public boolean isReceiverAlive() {
        return receiverThread.isAlive();
    }

    @Override
    public void repeat(Message message) {
        sendQueue.add(message);
//        jedis.lpush(pushQueueName, message.pack());
    }

    public void setInstructionDispatcher(InstructionDispatcher instructionDispatcher) {
        this.instructionDispatcher = instructionDispatcher;
    }

    /**
     * Send to Redis server.
     * Receive from Minecraft.
     */
    private class SenderThread extends Thread {
        @Override
        public void run() {
            int retryCounter = MAX_RETRY_TIMES;
            try(Jedis jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false)) {
                jedis.auth(redisConfig.getPassword());
                try {
                    while (enabled.get()) {
                        Message outboundMessage = sendQueue.take();
                        // send outbound message
                        while (retryCounter > 0) {
                            try {
                                jedis.lpush(pushQueueName, outboundMessage.pack());
                            } catch (JedisException e) {
                                logger.warning(String.format("Failed to push message: %s. Retrying... (remaining times: %d)", e, retryCounter));
                                --retryCounter;
                                continue;
                            }
                            retryCounter = MAX_RETRY_TIMES;
                            break;
                        }
                        // max retry times reached
                        if (retryCounter == 0) {
                            logger.severe("Max retry times reached. Sender thread will quit.");
                            return;
                        }
                    }
                } catch (InterruptedException ignored){
                    logger.info("Sender thread was interrupted. Quitting.");
                }
                logger.info("Sender thread is stopped.");
            } catch (JedisConnectionException e) {
                logger.severe("Failed to connect Redis server: " + e + " Sender thread will quit.");
            }
        }
    }

    /**
     * Receive from Redis server.
     * Send to Minecraft.
     */
    private class ReceiverThread extends Thread {
        @Override
        public void run() {
            try(Jedis jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false)) {
                jedis.auth(redisConfig.getPassword());
                while (enabled.get()) {
                    // receive from Redis
                    List<String> list = null;
                    int retryCounter = MAX_RETRY_TIMES;
                    while (retryCounter > 0) {
                        try {
                            list = jedis.brpop(POP_TIMEOUT, popQueueName);
                        } catch (JedisException e) {
                            logger.warning(String.format("Failed to push message: %s. Retrying... (remaining times: %d)", e, retryCounter));
                            --retryCounter;
                            continue;
                        }
                        break;
                    }
                    // max retry times reached
                    if (retryCounter == 0) {
                        logger.severe("Max retry times reached. Receiver thread will quit.");
                        return;
                    }

                    if (list != null) {
                        for (String rawSting : list) {
                            Message inboundMessage = Message.fromRedisRawString(rawSting);
                            if (inboundMessage != null) {
                                boolean isCommand = inboundMessage.getMessage().startsWith(redisCommandPrefix);
                                // send to Minecraft
                                if (!isCommand) {
                                    logger.info(String.format("Received inbound message: %s (rawString=%s).", inboundMessage, rawSting));
                                    inBoundMessageDispatcher.repeatInboundMessage(inboundMessage);
                                }

                                // Execute instruction
                                if (isCommand && instructionDispatcher != null) {
                                    String cmd = inboundMessage.getMessage();
                                    if (redisCommandPrefix.length() == cmd.length())
                                        cmd = "";
                                    else
                                        cmd = cmd.substring(redisCommandPrefix.length());
                                    // dispatch the command
                                    instructionDispatcher.dispatchExecution(cmd, RedisManager.this);
                                }
                            } else {
                                logger.warning(String.format("Malformed inbound message: %s. Ignored.", rawSting));
                            }
                        }
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException ignored) {
                logger.info("Receiver thread was interrupted. Quitting.");
            } catch (JedisConnectionException e) {
                logger.severe("Failed to connect Redis server: " + e + " Receiver thread will quit.");
            }
            logger.info("Receiver thread is stopped.");
        }
    }

}
