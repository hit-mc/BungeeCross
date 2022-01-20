package com.keuin.psmb4j;

import com.keuin.psmb4j.error.CommandFailureException;
import com.keuin.psmb4j.error.ServerMisbehaveException;
import com.keuin.psmb4j.util.InputStreamUtils;
import com.keuin.psmb4j.util.StringUtils;

import java.io.IOException;
import java.util.function.Consumer;

public class PublishClient extends BaseClient {

    private final Object socketWriteLock = new Object();
    private final ActiveKeepAliveThread keepAliveThread;

    /**
     * Create a client in PUBLISH mode.
     * @param host the host to connect to.
     * @param port the port to connect to.
     * @param topicId the topic to publish messages.
     * @param keepAliveIntervalMillis interval between sending keep-alive messages. If <=0, keep-alive is disabled.
     * @param asynchronousExceptionHandler the function to handle exceptions occurred in internal asynchronous thread.
     * @throws IOException if an IO error occurred.
     */
    public PublishClient(String host, int port, String topicId, long keepAliveIntervalMillis,
                         Consumer<IOException> asynchronousExceptionHandler) throws IOException, CommandFailureException {
        super(host, port);
        setPublish(topicId);
        if (keepAliveIntervalMillis > 0) {
            if (keepAliveIntervalMillis < 3000) {
                throw new IllegalArgumentException("Keep alive interval is too small!");
            }
            this.keepAliveThread = new ActiveKeepAliveThread(this.socketWriteLock, this.os,
                    asynchronousExceptionHandler, keepAliveIntervalMillis);
            this.keepAliveThread.start();
        } else {
            this.keepAliveThread = null;
        }
    }

    private void setPublish(String topicId) throws IOException, CommandFailureException {
        if (!StringUtils.isPureAscii(topicId)) {
            throw new IllegalArgumentException("topicId cannot be encoded with ASCII");
        }
        setSocketTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS);
        synchronized (this.socketWriteLock) {
            os.writeBytes("PUB");
            os.writeBytes(topicId);
            os.writeByte('\0');
            os.flush();
        }

        var response = InputStreamUtils.readCString(is, MAX_CSTRING_LENGTH);
        if (response.equals("FAILED")) {
            var errorMessage = InputStreamUtils.readCString(is, MAX_CSTRING_LENGTH);
            throw new CommandFailureException("Publish failed: " + errorMessage);
        } else if (!response.equals("OK")) {
            throw new ServerMisbehaveException("Unexpected response: " + response);
        }
    }

    /**
     * Publish a message.
     * Note that this method is not thread-safe.
     * @param message the message to publish.
     * @throws CommandFailureException If a command was rejected by the server.
     * @throws IOException if an IO error occurred.
     */
    public void publish(byte[] message) throws CommandFailureException, IOException {
        synchronized (this.socketWriteLock) {
            os.writeBytes("MSG");
            os.writeLong(message.length);
            os.write(message);
            os.flush();
        }
    }

    @Override
    public void close() {
        try {
            if (this.keepAliveThread != null) {
                this.keepAliveThread.interrupt();
                this.keepAliveThread.join();
            }
        } catch (InterruptedException ignored) {
        }
        super.close();
    }
}
