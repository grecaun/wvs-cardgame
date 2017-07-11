package com.sentinella.james.gui.view;

/**
 * Created by James on 7/10/2017.
 */
class ServerOptionHolder {
    private int portNumber = 36789;
    private int lobbyTime  = 15;
    private int playTime   = 90;
    private int minPlayers = 3;
    private int maxClients = 90;
    private int maxStrikes = 5;

    int getPortNumber() {
        return portNumber;
    }

    void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    int getLobbyTime() {
        return lobbyTime;
    }

    void setLobbyTime(int lobbyTime) {
        this.lobbyTime = lobbyTime;
    }

    int getPlayTime() {
        return playTime;
    }

    void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    int getMinPlayers() {
        return minPlayers;
    }

    void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    int getMaxClients() {
        return maxClients;
    }

    void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    int getMaxStrikes() {
        return maxStrikes;
    }

    void setMaxStrikes(int maxStrikes) {
        this.maxStrikes = maxStrikes;
    }
}
