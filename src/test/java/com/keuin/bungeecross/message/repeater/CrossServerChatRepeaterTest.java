package com.keuin.bungeecross.message.repeater;

import com.keuin.bungeecross.message.user.PlayerUser;
import com.keuin.bungeecross.testutil.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CrossServerChatRepeaterTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void repeatToEmptyServerList() {
        CrossServerChatRepeater repeater = new CrossServerChatRepeater(TestableProxyServer.createSkeleton());
        repeater.repeat(TestableInGameMessage.create(
                "message",
                TestableMessageUser.createSkeleton(),
                TestableProxiedPlayer.createSkeleton()
                )
        );
    }

    @Test
    public void repeatToServersWithNoPlayers() {
        TestableProxyServer proxy = TestableProxyServer.createSkeleton();
        TestableServer[] servers = new TestableServer[]{
                TestableServer.createSkeleton(),
                TestableServer.createSkeleton(),
                TestableServer.createSkeleton()
        };
        Arrays.stream(servers).forEach(server -> proxy.addServerInfo(server.getInfo()));

        CrossServerChatRepeater repeater = new CrossServerChatRepeater(proxy);
        repeater.repeat(TestableInGameMessage.create(
                "message",
                TestableMessageUser.createSkeleton(),
                TestableProxiedPlayer.createSkeleton()
                )
        );
    }

    @Test
    public void repeatToServersWithPlayers() {
        TestableProxyServer proxy = TestableProxyServer.createSkeleton();
        TestableServer[] servers = new TestableServer[]{
                new TestableServer("server1"),
                new TestableServer("server2"),
                new TestableServer("server3")
        };
        TestableProxiedPlayer[] players = new TestableProxiedPlayer[]{
                new TestableProxiedPlayer("player11", UUID.randomUUID(), servers[0]),
                new TestableProxiedPlayer("player12", UUID.randomUUID(), servers[0]),
                new TestableProxiedPlayer("player21", UUID.randomUUID(), servers[1]),
                new TestableProxiedPlayer("player22", UUID.randomUUID(), servers[1]),
                new TestableProxiedPlayer("player31", UUID.randomUUID(), servers[2]),
                new TestableProxiedPlayer("player32", UUID.randomUUID(), servers[2])
        };

        servers[0].getTestableServerInfo().addPlayer(players[0]);
        servers[0].getTestableServerInfo().addPlayer(players[1]);
        servers[1].getTestableServerInfo().addPlayer(players[2]);
        servers[1].getTestableServerInfo().addPlayer(players[3]);
        servers[2].getTestableServerInfo().addPlayer(players[4]);
        servers[2].getTestableServerInfo().addPlayer(players[5]);

//        Arrays.stream(servers).forEach(server -> );
        for (TestableServer server : servers) {
            proxy.addServerInfo(server.getInfo());
        }

        CrossServerChatRepeater repeater = new CrossServerChatRepeater(proxy);

        for (TestableProxiedPlayer player : players) {
            assertEquals(0, player.getMessageCount());
        }

        repeater.repeat(TestableInGameMessage.create(
                "message",
                PlayerUser.fromProxiedPlayer(players[0]),
                players[0]
                )
        );

        assertEquals(0, players[0].getMessageCount());
        assertEquals(0, players[1].getMessageCount());
        assertEquals(1, players[2].getMessageCount());
        assertEquals(1, players[3].getMessageCount());
        assertEquals(1, players[4].getMessageCount());
        assertEquals(1, players[5].getMessageCount());

    }

    @Test
    public void repeatInboundMessage() {
    }
}