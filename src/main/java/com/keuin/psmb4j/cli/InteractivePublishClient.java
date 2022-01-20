package com.keuin.psmb4j.cli;

import com.keuin.psmb4j.PublishClient;
import com.keuin.psmb4j.error.CommandFailureException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class InteractivePublishClient {
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
        System.out.println("Topic ID: ");
        var topicId = scanner.nextLine().trim();
        System.out.println("Keep alive interval (ms): ");
        var keepAlive = Long.parseLong(scanner.nextLine());
        if (keepAlive <= 0) {
            System.out.println("Keepalive is disabled.");
        }
        System.out.printf("Connecting to %s:%d...\n", host, port);
        var client = new PublishClient(host, port, topicId, keepAlive, e -> {
            System.err.println("Async exception occurred:");
            e.printStackTrace();
        });
        while (true) {
            try {
                System.out.println("Message (empty line for exit): ");
                var input = scanner.nextLine();
                if (input.isEmpty()) {
                    client.close();
                    break;
                }
                var message = input.getBytes(StandardCharsets.UTF_8);
                client.publish(message);
            } catch (CommandFailureException e) {
                System.err.println("Command failed:");
                e.printStackTrace();
            }
        }
    }
}
