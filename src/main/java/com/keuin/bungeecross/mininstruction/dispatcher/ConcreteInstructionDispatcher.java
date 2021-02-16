package com.keuin.bungeecross.mininstruction.dispatcher;

import com.keuin.bungeecross.message.user.RepeatableUser;
import com.keuin.bungeecross.mininstruction.MinInstructionInterpreter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles in-game and out-game instructions, process them in serial, then echo to the callback.
 */
public class ConcreteInstructionDispatcher implements InstructionDispatcher {

    private final MinInstructionInterpreter interpreter;
    //    private final MessageRepeater echoRepeater;
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final LinkedBlockingQueue<ScheduledExecution> instructionQueue = new LinkedBlockingQueue<>();
    private final DispatcherThread dispatcherThread = new DispatcherThread();

    public ConcreteInstructionDispatcher(MinInstructionInterpreter interpreter) {
        this.interpreter = interpreter;
//        this.echoRepeater = echoRepeater;
    }

    @Override
    public void dispatchExecution(String command, RepeatableUser echoRepeater) {
        instructionQueue.add(new ScheduledExecution(command, echoRepeater));
        if (!dispatcherThread.isAlive()) {
            running.set(true);
            dispatcherThread.start();
        }
    }

    @Override
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
                    ScheduledExecution inst = instructionQueue.take();
                    interpreter.execute(inst.getCommand(), inst.getExecutionSender());

                    // repeat
//                    inst.getEchoRepeater().repeat(new EchoMessage(inst.getCommand(),echoComponents));
//                    echoRepeater.repeat();
                }
            } catch (InterruptedException ignored) {

            }
        }
    }

    private static class ScheduledExecution {
        private final String command;
        private final RepeatableUser echoRepeater;

        private ScheduledExecution(String command, RepeatableUser echoRepeater) {
            this.command = command;
            this.echoRepeater = echoRepeater;
        }

        private String getCommand() {
            return command;
        }

        private RepeatableUser getExecutionSender() {
            return echoRepeater;
        }
    }
}
