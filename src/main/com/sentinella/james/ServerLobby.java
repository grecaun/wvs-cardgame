package com.sentinella.james;

import java.io.PrintStream;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by James on 1/18/2017.
 */
public class ServerLobby extends Lobby {
    private ArrayList<Player>               atTable;

    private boolean     debug = false;
    private PrintStream debugStream;

    public ServerLobby() {
        super();
        atTable       = new ArrayList<>();
    }

    public void broadcastMessage(String message) {
        if (debug) debugStream.println(String.format("%s ServerLobby.broadcastMessage - Message: '%s'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),message));
        ArrayList<Player> toRemove = new ArrayList<>();
        for (Player p: players) {
            if (((ServerPlayer)p).sendMessage(message) == ServerPlayer.CONERROR.UNABLETOSEND) {
                toRemove.add(p);
                p.setStatus(pStatus.DISCONNECTED);
                ((ServerPlayer)p).closeSocket();
            }
        }
        players.removeAll(toRemove);
        for (Player p: atTable) {
            ((ServerPlayer)p).sendMessage(message);
        }
    }

    public Player getPlayer(SocketChannel sock) {
        for (Player p: players) {
            if (((ServerPlayer)p).isPlayer(sock)) return p;
        }
        for (Player p: atTable) {
            if (((ServerPlayer)p).isPlayer(sock)) return p;
        }
        return null;
    }

    public void sendEmptyTable() {
        for (Player p : players) {
            ((ServerPlayer)p).sendMessage("[stabl|e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00|52,52,52,52|0]");
        }
        for (Player p : atTable) {
            ((ServerPlayer)p).sendMessage("[stabl|e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00|52,52,52,52|0]");
        }
    }

    public Player getNextPlayer() {
        Player output = players.size() > 0 ? players.remove(0) : null;
        if (output != null) atTable.add(output);
        return output;
    }

    public void addToLobby(Player player) {
        if (!atTable.contains(player)) System.err.println("Unknown player at table.");
        else {
            atTable.remove(player);
            players.add(0, player);
        }
    }

    @Override
    public void addPlayer(String name) {
        Player thisPlayer = null;
        for (Player p: atTable) {
            if (p.isPlayer(name)) thisPlayer = p;
        }
        if (thisPlayer == null) super.addPlayer(name);
    }

    @Override
    public void addPlayer(Player player) {
        if (!atTable.contains(player)) super.addPlayer(player);
    }

    public void removePlayer(SocketChannel client) {
        Player temp = getPlayer(client);
        if (players.contains(temp)) players.remove(temp);
        if (temp != null) temp.setStatus(pStatus.DISCONNECTED);
    }

    public boolean playerExists(String name) {
        for (Player p: players) if (p.isPlayer(name)) return true;
        for (Player p: atTable) if (p.isPlayer(name)) return true;
        return false;
    }

    public String getLobbyMessage() {
        StringBuilder lobbyMessage;
        int numPlayers = 0;
        for (Player p: players) {
            if (p.getName() != null) numPlayers++;
        }
        if (numPlayers == 0) {
            lobbyMessage = new StringBuilder("[slobb|00]");
        } else {
            lobbyMessage = new StringBuilder(String.format("[slobb|%02d|", numPlayers));
            for (Player p: players) {
                if (p.getName() != null)lobbyMessage.append(String.format("%-8s,",p.getName()));
            }
            int messageLen = lobbyMessage.length();
            lobbyMessage.replace(messageLen-1,messageLen,"]");
        }
        return lobbyMessage.toString();
    }

    public boolean isInLobby(ServerPlayer thisGuy) {
        return players.contains(thisGuy);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setDebugStream(PrintStream debugStream) {
        this.debugStream = debugStream;
    }
}
