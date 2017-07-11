package com.sentinella.james.gui.view;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
