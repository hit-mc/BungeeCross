package com.keuin.psmb4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class ActiveKeepAliveThread extends Thread {

    private final Object lock;
    private final Consumer<IOException> exceptionHandler;
    private final long timeoutMillis;
    private final OutputStream os;

    public ActiveKeepAliveThread(Object lock, OutputStream os, Consumer<IOException> exceptionHandler, long timeoutMillis) {
        this.lock = lock;
        this.os = os;
        this.exceptionHandler = exceptionHandler;
        this.timeoutMillis = timeoutMillis;
    }

    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    @Override
    public void run() {
        final var nop = new byte[]{'N', 'O', 'P'};
        try {
            while (true) {
                synchronized (this.lock) {
                    os.write(nop);
                    os.flush();
                }
                Thread.sleep(this.timeoutMillis);
            }
        } catch (IOException e) {
            this.exceptionHandler.accept(e);
        } catch (InterruptedException ignored) {
        }
    }
}
