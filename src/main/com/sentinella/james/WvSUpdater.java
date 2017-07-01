package com.sentinella.james;

/**
 * Created by James on 4/7/2016.
 */
public interface WvSUpdater {
    void updateTable();
    void updatePlayer();
    void updatePlayer(String name);
    void updateStatus();
    void updateLobby();
    void updateChat(String name, String message);
    void updateAll();
    void updateHand();
}
