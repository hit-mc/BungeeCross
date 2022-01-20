package com.keuin.psmb4j.cli;

import com.keuin.psmb4j.SubscribeClient;
import com.keuin.psmb4j.error.CommandFailureException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InteractiveSubscribeClient {
    public static void main(String[] args) throws IOException, CommandFailureException {
        var scanner = new Scanner(System.in);
        System.out.println("Host: ");
        var host = scanner.nextLine().trim();
        System.out.println("Port: ");
        var port = Integer.parseInt(scanner.nextLine());
        if (port <= 0 || port > Short.MAX_VALUE) {
            System.err.println("Invalid port!");
            return;
        }
        System.out.println("Subscribe pattern: ");
        var pattern = scanner.nextLine().trim();
        System.out.println("Subscriber ID: ");
        var subscriberId = Long.parseUnsignedLong(scanner.nextLine());
        System.out.println("Keep alive interval (ms): ");
        var keepAlive = Integer.parseInt(scanner.nextLine());
        if (keepAlive <= 0) {
            System.out.println("Keepalive is disabled.");
        }
        System.out.printf("Connecting to %s:%d...\n", host, port);
        var client = new SubscribeClient(host, port, pattern, keepAlive, msg -> {
            System.err.println("Message: " + new String(msg.array(), StandardCharsets.UTF_8));
        }, subscriberId);
        System.out.println("Connection established.");
        client.subscribe();
    }
}
