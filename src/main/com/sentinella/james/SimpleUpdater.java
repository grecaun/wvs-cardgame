package com.sentinella.james;

import java.util.ArrayList;

/**
 * Created by James on 4/7/2016.
 */
public class SimpleUpdater implements WvSUpdater {
    Table       lTable;
    Lobby       lLobby;
    PlayerHand  lHand;
    String      lName;
    int         lStrikes;

    public SimpleUpdater(Table cTable, Lobby cLobby, PlayerHand cHand, String cName, int cStrikes) {
        lTable   = cTable;
        lLobby   = cLobby;
        lHand    = cHand;
        lName    = cName.trim();
        lStrikes = cStrikes;
    }

    @Override
    public void updateTable(Table table) {
        lTable.printTable(lName);
        lHand.printHand();
    }

    @Override
    public void updatePlayer() {
    }

    @Override
    public void updatePlayer(String name) {
        lName = name.trim();
    }

    @Override
    public void updateStatus(Table table) {
        if (lTable.getPlayerStatus(lName) == pStatus.ACTIVE) {
            System.out.println("The server is waiting on you.");
        }
    }

    @Override
    public void updateLobby(ArrayList<String> names) {
    }

    @Override
    public void updateChat(String name, String message) {
        System.out.println(String.format("%s: %s",name,message));
    }

    @Override
    public void updateAll() {
    }

    @Override
    public void updateHand(ArrayList<Card> cards) {
    }
}
