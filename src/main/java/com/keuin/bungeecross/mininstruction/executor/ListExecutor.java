package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.message.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import com.keuin.bungeecross.util.PrettyComponents;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

public final class ListExecutor extends AbstractInstructionExecutor {

    private static final ListExecutor INSTANCE = new ListExecutor(
            "show online players in all servers.",
            new String[0]
    );

    private ListExecutor(String description, String[] params) {
        super("list", description, params);
    }

    public static ListExecutor getInstance() {
        return INSTANCE;
    }

    private ExecutionResult printWithOldStyle(Collection<ProxiedPlayer> players, ProxyServer proxy, MessageRepeatable echoRepeater) {
        Objects.requireNonNull(players);
        if (players.isEmpty())
            return ExecutionResult.SUCCESS;
        // players
        List<BaseComponent> echoComponents = new ArrayList<>();
        proxy.getPlayers().forEach(p -> Optional.ofNullable(p).ifPresent(
                player -> echoComponents.addAll(Arrays.asList(getPlayerPrettyComponent(player)))
        ));
        if (!echoComponents.isEmpty())
            echo(echoRepeater, echoComponents.toArray(new BaseComponent[0]));
        return ExecutionResult.SUCCESS;
    }
    
    private ExecutionResult printWithGroupedStyle(Collection<ProxiedPlayer> players, ProxyServer proxy, MessageRepeatable echoRepeater) {
        Objects.requireNonNull(players);
        if (players.isEmpty())
            return ExecutionResult.SUCCESS;

        // group players by server
        final SortedMap<String, Collection<ProxiedPlayer>> playerMap = new TreeMap<>();
        for (ProxiedPlayer player : players) {
            final String serverName = player.getServer().getInfo().getName();
            if (!playerMap.containsKey(serverName))
                playerMap.put(serverName, new ArrayList<>(10));
            playerMap.get(serverName).add(player);
        }

        // print all servers' players in natural order
        List<BaseComponent> echoComponents = new ArrayList<>(20);
        playerMap.forEach((server, players2) -> {
            final BaseComponent title
                    = new TextComponent("[" + server + "]\n");
            title.setColor(ChatColor.GREEN);
            echoComponents.add(title);
            boolean first = true;
            for (ProxiedPlayer player : players2) {
                final BaseComponent line
                        = new TextComponent((first ? "" : ", ") + player.getName());
                line.setColor(ChatColor.WHITE);
                echoComponents.add(line);
                first = false;
            }
        });

        if (!echoComponents.isEmpty())
            echo(echoRepeater, echoComponents.toArray(new BaseComponent[0]));
        return ExecutionResult.SUCCESS;
    }
    
    @Override
    public ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
        ProxyServer proxy = ProxyServer.getInstance();
        Collection<ProxiedPlayer> players = proxy.getPlayers();
        int onlinePlayers = players.size();

        // response head
        echo(echoRepeater, new ComponentBuilder(String.format(
                "There %s %d %s online%s",
                onlinePlayers <= 1 ? "is" : "are",
                onlinePlayers,
                onlinePlayers <= 1 ? "player" : "players",
                onlinePlayers == 0 ? "." : ":"
        )).color(ChatColor.WHITE).create());
        
        if (onlinePlayers <= 3) {
            return printWithOldStyle(players, proxy, echoRepeater);
        } else {
            return printWithGroupedStyle(players, proxy, echoRepeater);
        }
    }

    private BaseComponent[] getPlayerPrettyComponent(ProxiedPlayer player) {
        ComponentBuilder prettyBuilder = new ComponentBuilder();

        // build server text
        BaseComponent componentServer = PrettyComponents.createNavigableServerButton(player.getServer().getInfo().getName());

        // build player text
        TextComponent componentPlayer = new TextComponent(" " + player.getName());
        componentPlayer.setColor(ChatColor.WHITE);
        componentPlayer.setUnderlined(false);
        componentPlayer.setHoverEvent(null);

        // build pretty text
        prettyBuilder.append(componentPlayer);
        prettyBuilder.append(new TextComponent(" "));
        prettyBuilder.append(componentServer);
        return prettyBuilder.create();
    }
}
