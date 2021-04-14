package com.keuin.bungeecross.microapi;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.util.InputStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

class MessageHandler implements HttpHandler {

    private final Logger logger = Logger.getLogger("MicroApiMessageHandler");

    private final MessageRepeatable redisRepeater;

    MessageHandler(MessageRepeatable redisRepeater) {
        this.redisRepeater = redisRepeater;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
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
                                mm.getSender(),
                                mm.getMessage()
                        ));
                        redisRepeater.repeat(Message.build(mm.getMessage(), mm.getSender()));
                        throw new Response(200, responseSuccess.getBytes(StandardCharsets.UTF_8));
                    }
                    throw new Response(400);
                }
                throw new Response(405);
            } catch (JsonSyntaxException e) {
                throw new Response(400);
            } catch (IOException e) {
                logger.warning("IOException when processing /message request: " + e);
                e.printStackTrace();
                throw new Response(400);
            }
        } catch (Response response) {
            var body = response.getResponseBody();
            exchange.sendResponseHeaders(response.getResponseCode(), (body.length <= 0) ? -1 : body.length);
            if (body.length > 0) {
                exchange.getResponseBody().write(body);
            }
        }
        exchange.getResponseBody().flush();
        exchange.close();
    }

    private static class Response extends Exception {
        private final int responseCode;
        private final byte[] responseBody;

        public Response(int responseCode) {
            this.responseCode = responseCode;
            this.responseBody = new byte[0];
        }

        public Response(int responseCode, byte[] responseBody) {
            this.responseCode = responseCode;
            this.responseBody = Arrays.copyOf(responseBody, responseBody.length);
        }

        public int getResponseCode() {
            return responseCode;
        }

        public byte[] getResponseBody() {
            return Arrays.copyOf(responseBody, responseBody.length);
        }
    }
}
