package com.keuin.bungeecross.message.relayer;

import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.redis.InBoundMessageDispatcher;
import com.keuin.bungeecross.message.redis.RedisConfig;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * This manager holds a worker thread processing message inbound and outbound.
 */
public class RedisManager implements MessageRelayer {

    // TODO: Connect to redis server in constructor.

    private final Logger logger = BungeeCross.logger;

    private final Jedis jedis;
    private final String pushQueueName;
    private final String popQueueName;
    private final BlockingQueue<Message> sendQueue = new LinkedBlockingQueue<>();

    private final SenderThread senderThread = new SenderThread();
    private final ReceiverThread receiverThread = new ReceiverThread();

    private final InBoundMessageDispatcher inBoundMessageDispatcher;

    private final int POP_TIMEOUT = 1;

    public RedisManager(RedisConfig redisConfig, InBoundMessageDispatcher inBoundMessageDispatcher) {
        logger.info(String.format("RedisManager created with redis info: %s", redisConfig.toString()));
        jedis = new Jedis(redisConfig.getHost(), redisConfig.getPort(), false);
        jedis.auth(redisConfig.getPassword());
        this.pushQueueName = redisConfig.getPushQueueName();
        this.popQueueName = redisConfig.getPopQueueName();
        this.inBoundMessageDispatcher = inBoundMessageDispatcher;
    }

    public synchronized void start() {
        if (!senderThread.isAlive()) {
            senderThread.start();
        }
        if (!receiverThread.isAlive()) {
            receiverThread.start();
        }
    }

    public synchronized void stop() {
        if(senderThread.isAlive()) {
            senderThread.interrupt();
        }
        if(receiverThread.isAlive()) {
            receiverThread.interrupt();
        }
        jedis.close();
    }

    @Override
    public void relay(Message message) {
        // TODO
        sendQueue.add(message);
//        jedis.lpush(pushQueueName, message.pack());
    }

    /**
     * Send to Redis server.
     * Receive from Minecraft.
     */
    private class SenderThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
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

    /**
     * Receive from Redis server.
     * Send to Minecraft.
     */
    private class ReceiverThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    // TODO
                    List<String> list = jedis.brpop(POP_TIMEOUT, popQueueName);
                    if (list != null) {
                        for (String rawSting : list) {
                            Message inboundMessage = Message.fromRedisRawString(rawSting);
                            // send to Minecraft
                            inBoundMessageDispatcher.relayInboundMessage(inboundMessage);
                        }
                    }
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ignored) {
                logger.info("Receiver thread was interrupted. Quitting.");
            }
            logger.info("Receiver thread is stopped.");
        }
    }

}
