package com.sentinella.james;

import java.util.ArrayList;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public interface WvSUpdater {
    void updateTable(Table table);
    void updateJoin(String name);
    void updateStatus(Table table);
    void updateLobby(ArrayList<String> names);
    void updateChat(String name, String message);
    void updateHand(ArrayList<Card> cards);
    void updateStrike(int strikeVal, int numStrikes);
    void updateSwapW(Card newCard);
    void updateSwapS(Card newCard, Card oldCard);
    void setLogBookInfo(LogBook log, String debugStr);
}
