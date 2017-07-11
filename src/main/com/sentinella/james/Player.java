package com.sentinella.james;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class Player {
    protected String      name;
    protected pStatus     status;
    protected int         numCards;
    protected int         strikes;
    protected PlayerHand  cards;

    public Player(String iName, pStatus iStatus, int iStrikes, int iNumCards) {
        name     = iName == null? null : iName.trim();
        status   = iStatus;
        strikes  = iStrikes;
        numCards = iNumCards;
        cards    = new PlayerHand();
    }

    public Player(String iName) {
        name     = iName == null? null : iName.trim();
        status   = pStatus.LOBBY;
        strikes  = 0;
        numCards = 0;
        cards    = new PlayerHand();
    }

    public void updatePlayer(String iName, pStatus iStatus, int iStrikes, int iNumCards) {
        name     = iName.trim();
        status   = iStatus;
        strikes  = iStrikes;
        numCards = iNumCards;
        cards.clear();
    }

    public void addCard(Card iCard) {
        if (iCard != null) cards.add(iCard);
        numCards = cards.count();
    }

    public void addCard(int iCard) {
        Card newCard = Card.CardCreator(iCard);
        if (newCard != null) cards.add(newCard);
        numCards = cards.count();
    }

    public boolean hasCard(int iCard) {
        return cards.hasCard(iCard);
    }

    public boolean hasCard(Card iCard) {
        return cards.hasCard(iCard.getCardIndexNumber());
    }

    public void removeCard(Card iCard) {
        cards.remove(iCard);
        numCards = cards.count();
    }

    public void removeCard(int iCard) {
        cards.remove(iCard);
        numCards = cards.count();
    }

    public int getNumCards() {
        return numCards;
    }

    public int getNumStrikes() { return strikes; }

    public void setNumCards(int numCards) {
        this.numCards = numCards > 26 ? 26 : numCards < 0 ? 0 : numCards;
    }

    public pStatus getStatus() {
        return status;
    }

    public void setStatus(pStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public boolean isPlayer(Player iPlayer) {
        return this.name.equalsIgnoreCase(iPlayer.getName());
    }

    public boolean isPlayer(String iPlayer) {
        return (this.name != null) && (iPlayer != null) && this.name.equalsIgnoreCase(iPlayer.trim());
    }

    public PlayerHand getHand() {
        return cards;
    }
}
