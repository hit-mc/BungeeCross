package com.keuin.bungeecross.microapi;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.message.Message;
import com.keuin.bungeecross.message.repeater.MessageRepeater;
import com.keuin.bungeecross.util.InputStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class BungeeMicroApi {

    private final HttpServer server;
    private final Logger logger = Logger.getLogger(BungeeMicroApi.class.getName());

    public BungeeMicroApi(int port, MessageRepeater redisRepeater) throws IOException {
        Objects.requireNonNull(redisRepeater);
        if (port <= 0)
            throw new IllegalArgumentException("API listening port must be positive.");

        logger.info(String.format("Starting MicroApi at localhost:%d...", port));
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "{\"version\": \"" + BungeeCross.getVersion() + "\"}";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.flush();
                exchange.close();
            }
        });
        server.createContext("/message", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    if ("POST".equals(exchange.getRequestMethod())) {
                        String responseSuccess = "{\"success\": true}";
                        String responseFailed = "{\"success\": failed}";
                        InputStream is = exchange.getRequestBody();
                        String request = new String(InputStreams.toByteArray(is), StandardCharsets.UTF_8);
                        MethodMessage mm = (new Gson()).fromJson(request, MethodMessage.class);
                        if (mm.isValid()) {
                            logger.info(String.format(
                                    "Send message{sender=%s, message=%s} to redis.",
                                    mm.sender,
                                    mm.message
                            ));
                            redisRepeater.repeat(Message.build(mm.message, mm.sender));
                            exchange.sendResponseHeaders(200, responseSuccess.getBytes().length);
                            exchange.getResponseBody().write(responseSuccess.getBytes());
                        } else {
                            exchange.sendResponseHeaders(400, -1);
                        }
                    } else {
                        exchange.sendResponseHeaders(405, -1);
                    }
                } catch (JsonSyntaxException e) {
                    exchange.sendResponseHeaders(400, -1);
                }
                exchange.getResponseBody().flush();
                exchange.close();
            }
        });
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        server.stop(3);
    }

    private static class MethodMessage {
        private final String sender;
        private final String message;

        public MethodMessage(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }

        /**
         * Check if the request misses `sender` or `message`.
         * @return if it is valid.
         */
        public boolean isValid() {
            return !Optional.ofNullable(sender).orElse("").isEmpty()
                    && !Optional.ofNullable(message).orElse("").isEmpty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodMessage that = (MethodMessage) o;
            return Objects.equals(sender, that.sender) &&
                    Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sender, message);
        }
    }
}
