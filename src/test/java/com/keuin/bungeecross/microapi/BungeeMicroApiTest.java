package com.keuin.bungeecross.microapi;

import com.google.gson.Gson;
import com.keuin.bungeecross.BungeeCross;
import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.testutil.TestableRepeatable;
import okhttp3.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import static org.junit.Assert.*;

public class BungeeMicroApiTest {

    private final TestableRepeatable testOutboundRepeater = new TestableRepeatable();
    private final TestableRepeatable testInboundRepeater = new TestableRepeatable();
    private final int port = (new Random()).nextInt(30000) + 30000;
    private final BungeeMicroApi microApi = new BungeeMicroApi(port, testOutboundRepeater, testInboundRepeater);
    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    @After
    public void tearDown() throws Exception {
        microApi.stop();
    }

    public BungeeMicroApiTest() throws IOException {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRoot() {
        try {
            String response = get(String.format("http://localhost:%d", port));
            System.out.println(response);
            assertEquals(response, "{\"version\": \"" + BungeeCross.getVersion() + "\"}");
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testImplicitOutboundOnlyMessage() {
        try {
            String sender = "sender";
            String message = "message";
            String response = post(String.format("http://localhost:%d/message", port), String.format(
                    "{\"sender\":\"%s\", \"message\": \"%s\"}",
                    sender,
                    message
            ));
            Success success = (new Gson()).fromJson(response, Success.class);
            assertTrue("server response is not success", success.success);
            System.out.println(response);
            assertTrue(testOutboundRepeater.getMessageList().contains(Message.build(message, sender)));
            assertEquals(1, testOutboundRepeater.getMessageList().size());
            assertTrue(testInboundRepeater.getMessageList().isEmpty());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testOutboundOnlyMessage() {
        try {
            String sender = "sender";
            String message = "message";
            String response = post(String.format("http://localhost:%d/message", port), String.format(
                    "{\"sender\":\"%s\", \"message\": \"%s\", \"target\":\"out\"}",
                    sender,
                    message
            ));
            Success success = (new Gson()).fromJson(response, Success.class);
            assertTrue("server response is not success", success.success);
            System.out.println(response);
            assertTrue(testOutboundRepeater.getMessageList().contains(Message.build(message, sender)));
            assertEquals(1, testOutboundRepeater.getMessageList().size());
            assertTrue(testInboundRepeater.getMessageList().isEmpty());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testInboundOnlyMessage() {
        try {
            String sender = "sender";
            String message = "message";
            String response = post(String.format("http://localhost:%d/message", port), String.format(
                    "{\"sender\":\"%s\", \"message\": \"%s\", \"target\":\"in\"}",
                    sender,
                    message
            ));
            Success success = (new Gson()).fromJson(response, Success.class);
            assertTrue("server response is not success", success.success);
            System.out.println(response);
            assertTrue(testInboundRepeater.getMessageList().contains(Message.build(message, sender)));
            assertEquals(1, testInboundRepeater.getMessageList().size());
            assertTrue(testOutboundRepeater.getMessageList().isEmpty());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testBothTargetMessage() {
        try {
            String sender = "sender";
            String message = "message";
            String response = post(String.format("http://localhost:%d/message", port), String.format(
                    "{\"sender\":\"%s\", \"message\": \"%s\", \"target\":\"both\"}",
                    sender,
                    message
            ));
            Success success = (new Gson()).fromJson(response, Success.class);
            assertTrue("server response is not success", success.success);
            System.out.println(response);
            assertTrue(testOutboundRepeater.getMessageList().contains(Message.build(message, sender)));
            assertEquals(1, testOutboundRepeater.getMessageList().size());
            assertTrue(testInboundRepeater.getMessageList().contains(Message.build(message, sender)));
            assertEquals(1, testInboundRepeater.getMessageList().size());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testInvalidMessage1() {
        try {
            String sender = "sender";
            String message = "message";
            RequestBody body = RequestBody.create(String.format(
                    "{\"sender\":\"%s\" \"message\" \"%s\"}",
                    sender,
                    message
            ), JSON);
            Request request = new Request.Builder()
                    .url(String.format("http://localhost:%d/message", port))
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                assertEquals(400, response.code());
            }
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testInvalidMessage2() {
        try {
            String sender = "sender";
            String message = "message";
            RequestBody body = RequestBody.create("{}", JSON);
            Request request = new Request.Builder()
                    .url(String.format("http://localhost:%d/message", port))
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                assertEquals(400, response.code());
            }
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    private String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();
        }
    }

    private String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return Objects.requireNonNull(response.body()).string();
        }
    }

    private static class Success {
        public boolean success;
    }
}