package com.sentinella.james;

import java.util.ArrayList;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class SimpleUpdater implements WvSUpdater {
    Table       lTable;
    Lobby       lLobby;
    PlayerHand  lHand;
    String      lName;
    int         lStrikes;

    LogBook log = new LogBook();

    public SimpleUpdater(Table cTable, Lobby cLobby, PlayerHand cHand, String cName, int cStrikes) {
        lTable   = cTable;
        lLobby   = cLobby;
        lHand    = cHand;
        lName    = cName.trim();
        lStrikes = cStrikes;
    }

    public SimpleUpdater(Table tab, Lobby lob, PlayerHand han, String name, int strikes, LogBook lg) {
        this(tab,lob,han,name,strikes);
        log = lg;
    }

    @Override
    public void updateTable(Table table) {
        ArrayList<String> lines = lTable.printTable(lName);
        for (String line: lines) {
            log.printOutMsg(line);
        }
        log.printOutMsg(lHand.printHand());
    }

    @Override
    public void updateJoin(String name) {
        lName = name.trim();
        log.printOutMsg(String.format("Joined server successfully. Your name is %s.", lName));
    }

    @Override
    public void updateStatus(Table table) {
        if (lTable.getPlayerStatus(lName) == pStatus.ACTIVE) {
            log.printOutMsg("The server is waiting on you.");
        }
    }

    @Override
    public void updateLobby(ArrayList<String> names) {
    }

    @Override
    public void updateChat(String name, String message) {
        log.printOutMsg(String.format("%s: %s",name,message));
    }

    @Override
    public void updateHand(ArrayList<Card> cards) {
    }

    @Override
    public void updateStrike(int strikeVal, int numStrikes) {
        lStrikes = numStrikes;
        log.printOutMsg(String.format("You received strike number %d because: %s",lStrikes,StrikeErrors.getErrorMessage(strikeVal)));
    }

    @Override
    public void updateSwapW(Card newCard) {
        log.printOutMsg(String.format("The scumbag gave you (the warlord) the %s. They are demanding you give them a card in return",newCard.getStringRep()));
    }

    @Override
    public void updateSwapS(Card newCard, Card oldCard) {
        log.printOutMsg(String.format("You received the %s from the warlord.  You lost the %s.",newCard.getStringRep(),oldCard.getStringRep()));
    }

    @Override
    public void setLogBookInfo(LogBook l, String debugStr) {
        this.log = LogBookFactory.getLogBook(l,debugStr);
    }
}
