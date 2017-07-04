package com.sentinella.james;

import java.util.ArrayList;

/**
 * Created by James on 4/7/2016.
 */
public interface WvSUpdater {
    void updateTable();
    void updatePlayer();
    void updatePlayer(String name);
    void updateStatus();
    void updateLobby(ArrayList<String> names);
    void updateChat(String name, String message);
    void updateAll();
    void updateHand(ArrayList<Card> cards);
}
