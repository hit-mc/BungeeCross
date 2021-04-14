package com.keuin.bungeecross.microapi;

import com.keuin.bungeecross.BungeeCross;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

class RootHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "{\"version\": \"" + BungeeCross.getVersion() + "\"}";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.flush();
        exchange.close();
    }
}
