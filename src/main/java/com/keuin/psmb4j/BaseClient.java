package com.keuin.psmb4j;

import com.keuin.psmb4j.error.IllegalParameterException;
import com.keuin.psmb4j.error.UnsupportedProtocolException;
import com.keuin.psmb4j.util.InputStreamUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public abstract class BaseClient implements AutoCloseable {

    private final Socket socket;

    protected final int protocolVersion = 1;
    protected final int MAX_CSTRING_LENGTH = 1024;
    protected final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 0;

    protected final DataInputStream is;
    protected final DataOutputStream os;


    public BaseClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS);
        this.is = new DataInputStream(this.socket.getInputStream());
        this.os = new DataOutputStream(this.socket.getOutputStream());
        handshake();
    }

    protected void handshake() throws IOException {
        os.writeBytes("PSMB");
        os.writeInt(protocolVersion);
        os.writeInt(0); // options
        os.flush();
        var response = InputStreamUtils.readCString(is, MAX_CSTRING_LENGTH);
        if (response.equals("UNSUPPORTED PROTOCOL")) {
            throw new UnsupportedProtocolException();
        } else if (response.equals("OK")) {
            var serverOptions = is.readInt();
            if (serverOptions != 0) {
                throw new IllegalParameterException("Illegal server options: " + serverOptions);
            }
        }
    }

    protected void setSocketTimeout(int t) throws SocketException {
        this.socket.setSoTimeout(t);
    }

    @Override
    public void close() {
        try {
            os.writeBytes("BYE");
            os.flush();
            os.close();
        } catch (IOException ignored) {
        }
        try {
            is.close();
        } catch (IOException ignored) {
        }
    }
}
