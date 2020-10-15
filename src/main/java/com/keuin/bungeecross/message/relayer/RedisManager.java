package com.keuin.bungeecross.message.relayer;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.InBoundMessageDispatcher;
import com.keuin.bungeecross.message.redis.RedisInstructionDispatcher;
import com.keuin.bungeecross.message.redis.RedisConfig;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * This manager holds a worker thread processing message inbound and outbound.
 */
public class RedisManager implements MessageRelayer {

    // TODO: Connect to redis server in constructor.

    private final Logger logger = BungeeCross.logger;

    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final RedisConfig redisConfig;

//    private final Jedis jedis;
    private final String pushQueueName;
    private final String popQueueName;

    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();
    private final SenderThread senderThread = new SenderThread();
    private final ReceiverThread receiverThread = new ReceiverThread();

    private RedisInstructionDispatcher instructionDispatcher;

    private final InBoundMessageDispatcher inBoundMessageDispatcher;

    private final int POP_TIMEOUT = 1;
    private final boolean relayCommandFromRedis = true;
    private final String redisCommandPrefix = "!";

    public RedisManager(RedisConfig redisConfig, InBoundMessageDispatcher inBoundMessageDispatcher) {
        logger.info(String.format("RedisManager created with redis info: %s", redisConfig.toString()));

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
    public void relay(Message message) {
        // TODO
        sendQueue.add(message);
//        jedis.lpush(pushQueueName, message.pack());
    }

    public void setInstructionDispatcher(RedisInstructionDispatcher instructionDispatcher) {
        this.instructionDispatcher = instructionDispatcher;
    }

    /**
     * Send to Redis server.
     * Receive from Minecraft.
     */
    private class SenderThread extends Thread {
        @Override
        public void run() {
            try(Jedis jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false)) {
                jedis.auth(redisConfig.getPassword());
                try {
                    while (enabled.get()) {
                        Message outboundMessage = sendQueue.take();
                        // send outbound message
                        jedis.lpush(pushQueueName, outboundMessage.pack());
                    }
                } catch (InterruptedException ignored){
                    logger.info("Sender thread was interrupted. Quitting.");
                }
                logger.info("Sender thread is stopped.");
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
                    // TODO
                    List<String> list = jedis.brpop(POP_TIMEOUT, popQueueName);
                    if (list != null) {
                        for (String rawSting : list) {
                            Message inboundMessage = Message.fromRedisRawString(rawSting);
                            if (inboundMessage != null) {
                                // send to Minecraft
                                logger.info(String.format("Received inbound message: %s (rawString=%s).", inboundMessage, rawSting));
                                inBoundMessageDispatcher.relayInboundMessage(inboundMessage);

                                // instructions
                                if (instructionDispatcher != null && inboundMessage.getMessage().startsWith(redisCommandPrefix)) {
                                    // command from redis
                                    String cmd = inboundMessage.getMessage();
                                    if (redisCommandPrefix.length() == cmd.length())
                                        cmd = "";
                                    else
                                        cmd = cmd.substring(redisCommandPrefix.length());
                                    // dispatch the command
                                    instructionDispatcher.dispatch(cmd);
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
            }
            logger.info("Receiver thread is stopped.");
        }
    }

}
