package com.keuin.psmb4j;

import com.keuin.psmb4j.error.CommandFailureException;
import com.keuin.psmb4j.error.ServerMisbehaveException;
import com.keuin.psmb4j.util.InputStreamUtils;
import com.keuin.psmb4j.util.StringUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class SubscribeClient extends BaseClient {

    private final String pattern;
    private final long subscriberId;
    private final Consumer<ByteBuffer> callback;
    private final int keepAliveIntervalMillis;

    private boolean isSubscribed = false;
    private boolean isRunning = false;

    /**
     * Create a client in SUBSCRIBE mode.
     * @param host the host to connect to.
     * @param port the port to connect to.
     * @param pattern the pattern to subscribed.
     * @param keepAliveIntervalMillis interval between sending keep-alive messages. If <=0, keep-alive is disabled.
     * @param callback the callback which accepts message from server.
     * @param subscriberId an integer identifying a subscriber.
     * @throws IOException if an IO error occurred.
     */
    public SubscribeClient(String host, int port, String pattern, int keepAliveIntervalMillis, Consumer<ByteBuffer> callback, long subscriberId) throws IOException {
        super(host, port);
        this.pattern = pattern;
        this.subscriberId = subscriberId;
        this.callback = callback;
        this.keepAliveIntervalMillis = keepAliveIntervalMillis;
        if (keepAliveIntervalMillis < 3000) {
            throw new IllegalArgumentException("Keep alive interval is too small!");
        }
    }

    private void setSubscribe(String pattern, long subscriberId) throws IOException, CommandFailureException {
        if (!StringUtils.isPureAscii(pattern)) {
            throw new IllegalArgumentException("pattern cannot be encoded in ASCII");
        }
        os.writeBytes("SUB");
        os.writeInt(1); // options
        os.writeBytes(pattern);
        os.writeByte('\0');
        os.writeLong(subscriberId);
        os.flush();

        var response = InputStreamUtils.readCString(is, MAX_CSTRING_LENGTH);
        if (response.equals("FAILED")) {
            var errorMessage = InputStreamUtils.readCString(is, MAX_CSTRING_LENGTH);
            throw new CommandFailureException("Subscribe failed: " + errorMessage);
        } else if (!response.equals("OK")) {
            throw new ServerMisbehaveException("Unexpected response: " + response);
        }
    }

    /**
     * Start subscribing.
     * This method is blocking, the callback will be called in the same thread.
     * This method cannot run simultaneously by more than one thread,
     * or an {@link IllegalStateException} will be thrown.
     * @throws CommandFailureException If a command was rejected by the server.
     * @throws IOException if an IO error occurred. In this case,
     * it is usually unsafe to retry this function, since the internal socket is probably broken.
     * You should use another new instance in order to reconnect.
     */
    public void subscribe() throws CommandFailureException, IOException {
        if (isRunning) {
            throw new IllegalStateException();
        }
        try {
            if (!isSubscribed) {
                setSubscribe(pattern, subscriberId);
                isSubscribed = true;
            }
            while (true) {
                try {
                    // only timeout when reading the command
                    // in other reading, we use default timeout
                    if (keepAliveIntervalMillis > 0) {
                        setSocketTimeout(keepAliveIntervalMillis);
                    }
                    var command = new String(InputStreamUtils.readBytes(is, 3), StandardCharsets.US_ASCII);
                    if (keepAliveIntervalMillis > 0) {
                        setSocketTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS);
                    }
                    if (command.equals("MSG")) {
                        var length = is.readLong();
                        if ((length & 0xffffffff00000000L) != 0) {
                            throw new RuntimeException(String.format("Client implementation does not support " +
                                    "such long payload (%s Bytes)", Long.toUnsignedString(length)));
                        }
                        var message = InputStreamUtils.readBytes(is, (int) length);
                        callback.accept(ByteBuffer.wrap(message));
                    } else if (command.equals("NOP")) {
                        os.writeBytes("NIL");
                        os.flush();
                    } else if (command.equals("BYE")) {
                        break;
                    } else if (!command.equals("NIL")) {
                        throw new ServerMisbehaveException("Illegal command from server: " + command);
                    }
                } catch (SocketTimeoutException e) {
                    os.writeBytes("NOP");
                    os.flush();
                }
            }
        } finally {
            isRunning = false;
        }
    }
}
