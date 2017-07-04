package com.sentinella.james;

import java.util.ArrayList;

/**
 * Created by James on 4/6/2016.
 */
public class Lobby {
    protected ArrayList<Player> players;

    public Lobby() {
        players = new ArrayList<>();
    }

    public void addPlayer(Player iPlayer) {
        if (!this.hasPlayer(iPlayer.getName())) players.add(iPlayer);
    }

    public void addPlayer(String iPlayer) {
        if (!this.hasPlayer(iPlayer)) players.add(new Player(iPlayer));
    }

    public void removePlayer(Player iPlayer) {
        Player toRemove = null;
        for (Player p : players) {
            if (p.isPlayer(iPlayer)) {
                toRemove = p;
                break;
            }
        }
        if (toRemove != null) players.remove(toRemove);
    }

    public void removePlayer(String iPlayer) {
        Player toRemove = null;
        for (Player p : players) {
            if (p.isPlayer(iPlayer)) {
                toRemove = p;
                break;
            }
        }
        if (toRemove != null) players.remove(toRemove);
    }

    public boolean hasPlayer(String iPlayer) {
        for (Player p: players) {
            if (p.isPlayer(iPlayer)) {
                return true;
            }
        }
        return false;
    }

    public int numInLobby() {
        return players.size();
    }

    public void clear() {
        players.clear();
    }

    public String getLobbyString() {
        StringBuilder nameStr = new StringBuilder();
        nameStr.append("Players in lobby:");
        for (Player s : players) {
            nameStr.append(" ");
            nameStr.append(s.getName().trim());
        }
        return nameStr.toString();
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }
}
