package com.keuin.bungeecross.message.redis;

import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.ingame.InGameMessage;
import com.keuin.bungeecross.message.relayer.MessageRelayer;
import com.keuin.bungeecross.message.user.MessageUser;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles instructions issued by RedisManager, execute them in serial, then echo to the MessageRelayer.
 */
public class RedisInstructionDispatcher {

    private final MinInstructionInterpreter interpreter;
    private final MessageRelayer echoRelayer;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final LinkedBlockingQueue<String> instructionQueue = new LinkedBlockingQueue<>();
    private final DispatcherThread dispatcherThread = new DispatcherThread();

    public RedisInstructionDispatcher(MinInstructionInterpreter interpreter, MessageRelayer echoRelayer) {
        this.interpreter = interpreter;
        this.echoRelayer = echoRelayer;
    }

    public void dispatch(String command) {
        instructionQueue.add(command);
        if (!dispatcherThread.isAlive()) {
            running.set(true);
            dispatcherThread.start();
        }
    }

    public void close() {
        running.set(false);
        if (dispatcherThread.isAlive())
            dispatcherThread.interrupt();
    }

    private class DispatcherThread extends Thread {
        @Override
        public void run() {
            try {
                while (running.get()) {
                    String inst = instructionQueue.take();

                    StringBuilder echoBuilder = new StringBuilder();
                    BaseComponent[] components = interpreter.execute(inst);
                    for (BaseComponent component : components) {
                        echoBuilder.append(component.toPlainText());
                    }
                    echoRelayer.relay(new Message() {
                        @Override
                        public String getMessage() {
                            return echoBuilder.toString();
                        }

                        @Override
                        public MessageUser getSender() {
                            return new MessageUser() {
                                @Override
                                public String getName() {
                                    return "SERVER";
                                }

                                @Override
                                public UUID getUUID() {
                                    return null;
                                }

                                @Override
                                public String getId() {
                                    return null;
                                }
                            };
                        }
                    });
                }
            } catch (InterruptedException ignored) {

            }
        }
    }
}
