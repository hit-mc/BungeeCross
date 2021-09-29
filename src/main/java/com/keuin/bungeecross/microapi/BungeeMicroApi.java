package com.keuin.bungeecross.microapi;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Logger;

public class BungeeMicroApi {

    private final HttpServer server;
    private final Logger logger = Logger.getLogger(BungeeMicroApi.class.getName());

    public BungeeMicroApi(int port, MessageRepeatable redisRepeater, MessageRepeatable inGameBroadcastRepeater)
            throws IOException {
        Objects.requireNonNull(redisRepeater);
        if (port <= 0)
            throw new IllegalArgumentException("Port must be positive.");

        logger.info(String.format("Starting MicroApi server at localhost:%d...", port));
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/message", new MessageHandler(redisRepeater, inGameBroadcastRepeater));
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        server.stop(3);
    }

}
