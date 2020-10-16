package com.keuin.bungeecross.mininstruction.dispatcher;

import com.keuin.bungeecross.message.EchoMessage;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles in-game and out-game instructions, process them in serial, then echo to the callback.
 */
public class InstructionDispatcher {

    private final MinInstructionInterpreter interpreter;
//    private final MessageRepeater echoRepeater;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final LinkedBlockingQueue<ScheduledExecution> instructionQueue = new LinkedBlockingQueue<>();
    private final DispatcherThread dispatcherThread = new DispatcherThread();

    public InstructionDispatcher(MinInstructionInterpreter interpreter) {
        this.interpreter = interpreter;
//        this.echoRepeater = echoRepeater;
    }

    public void dispatchExecution(String command, MessageRepeater echoRepeater) {
        instructionQueue.add(new ScheduledExecution(command, echoRepeater));
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
                    Thread.sleep(100);
                    ScheduledExecution inst = instructionQueue.take();

                    StringBuilder echoBuilder = new StringBuilder();
                    BaseComponent[] components = interpreter.execute(inst.getCommand());
                    for (BaseComponent component : components) {
                        echoBuilder.append(component.toPlainText());
                    }
                    // repeat as the SERVER user
                    inst.getEchoRepeater().repeat(new EchoMessage(echoBuilder.toString(), inst.getCommand()));
//                    echoRepeater.repeat();
                }
            } catch (InterruptedException ignored) {

            }
        }
    }

    private static class ScheduledExecution {
        private final String command;
        private final MessageRepeater echoRepeater;

        private ScheduledExecution(String command, MessageRepeater echoRepeater) {
            this.command = command;
            this.echoRepeater = echoRepeater;
        }

        private String getCommand() {
            return command;
        }

        private MessageRepeater getEchoRepeater() {
            return echoRepeater;
        }
    }
}
