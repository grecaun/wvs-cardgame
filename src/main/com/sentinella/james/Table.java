package com.sentinella.james;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class Table {
    protected boolean     isNotRanked;
    protected Card[]      inPlay;
    protected Player[]    players;

    public Table() {
        isNotRanked = false;
        inPlay      = new Card[4];
        players     = new Player[7];
    }

    public boolean isNotRanked() {
        return isNotRanked;
    }

    public synchronized void setNotRanked(boolean notRanked) {
        isNotRanked = notRanked;
    }

    public synchronized void setInPlay(int c1, int c2, int c3, int c4) {
        inPlay[0] = Card.CardCreator(c1);
        inPlay[1] = Card.CardCreator(c2);
        inPlay[2] = Card.CardCreator(c3);
        inPlay[3] = Card.CardCreator(c4);
        sortInPlay();
    }

    public synchronized String getPlayerbySeat(int seatNo) {
        return players[seatNo] == null? "" : players[seatNo].getName();
    }

    public synchronized int[] getInPlay() {
        int[] retVal = {inPlay[0] == null? 52 : inPlay[0].getCardIndexNumber(),
                        inPlay[1] == null? 52 : inPlay[1].getCardIndexNumber(),
                        inPlay[2] == null? 52 : inPlay[2].getCardIndexNumber(),
                        inPlay[3] == null? 52 : inPlay[3].getCardIndexNumber()};
        return retVal;
    }

    public synchronized int numInPlay() {
        int counter = 0;
        counter += inPlay[0] == null ? 0 : 1;
        counter += inPlay[1] == null ? 0 : 1;
        counter += inPlay[2] == null ? 0 : 1;
        counter += inPlay[3] == null ? 0 : 1;
        return counter;
    }

    public synchronized void sortInPlay() {
        Card temp;
        for (int i=0; i<3; i++) {
            for (int j=i+1; j<4; j++) {
                if (inPlay[i] != null) {
                    if (inPlay[j] != null && inPlay[j].getCardIndexNumber() < inPlay[i].getCardIndexNumber()) {
                        temp      = inPlay[i];
                        inPlay[i] = inPlay[j];
                        inPlay[j] = temp;
                    }
                } else {
                    if (inPlay[j] != null) {
                        temp      = inPlay[i];
                        inPlay[i] = inPlay[j];
                        inPlay[j] = temp;
                    }
                }
            }
        }
    }

    public synchronized int getInPlayValue() {
        sortInPlay();
        return inPlay[0] == null ? -1 : inPlay[0].getCardNumericFaceValue();
    }

    public synchronized void setPlayer(int seatNumber, pStatus status, int strikes, String name, int cardCount) {
        if (seatNumber<0 || seatNumber > 6) return;
        if (players[seatNumber] == null) {
            players[seatNumber] = new Player(name, status, strikes, cardCount);
        } else {
            players[seatNumber].updatePlayer(name, status, strikes, cardCount);
        }
    }

    public synchronized void setPlayer(int seatNumber, Player player) {
        if (seatNumber<0 || seatNumber > 6) return;
        players[seatNumber] = player;
    }

    public synchronized void removePlayer(int seatNumber) {
        players[seatNumber] = null;
    }

    public synchronized pStatus getPlayerStatus(String playerName) {
        for (int i=0; i < 7; i++) {
            if (players[i] != null && players[i].getName().equalsIgnoreCase(playerName)) {
                return players[i].getStatus();
            }
        }
        return pStatus.NOTFOUND;
    }

    public synchronized void printTable(String playerName) {
        String statusString;
        for (int i=0; i<7; i++) {
            if (players[i]!=null) {
                if (players[i].getName().equalsIgnoreCase(playerName)) {
                    switch (players[i].getStatus()) {
                        case ACTIVE:
                            statusString = " It is your turn.";
                            break;
                        case PASSED:
                            statusString = " You passed or were skipped.";
                            break;
                        case WAITING:
                            statusString = " You are waiting for your turn.";
                            break;
                        case DISCONNECTED:
                            statusString = " You are apparently no longer connected.";
                            break;
                        default:
                            statusString = " Your status is unknown.";
                    }
                    System.out.printf("You are player %d.%s%n",i+1,statusString);

                } else {
                    switch (players[i].getStatus()) {
                        case ACTIVE:
                            statusString = " It is currently their turn.";
                            break;
                        case PASSED:
                            statusString = " They passed or were skipped last time around.";
                            break;
                        case WAITING:
                            statusString = " They are waiting for their turn.";
                            break;
                        case DISCONNECTED:
                            statusString = " They are no longer connected.";
                            break;
                        default:
                            statusString = " Their status is unknown.";
                    }
                    System.out.printf("Player %d is %s, who has %d cards left.%s%n",i+1,players[i].getName().trim(),players[i].getNumCards(),statusString);
                }
            }
        }
        if (isNotRanked) System.out.println("This round is not ranked.");
        else System.out.println("This round is ranked.");
        sortInPlay();
        if (inPlay[0] == null) {
            System.out.println("It's the beginning of a new round.");
        } else {
            StringBuilder cardString = new StringBuilder();
            cardString.append("These cards are on the table:");
            for (int j = 0; j < 4; j++) {
                if (inPlay[j] != null) {
                    cardString.append(" ");
                    cardString.append(inPlay[j].getStringRep());
                }
            }
            System.out.println(cardString.toString());
        }
    }

    public pStatus getSeatStatus(int seatNo) {
        return players[seatNo] == null ? pStatus.EMPTY : players[seatNo].getStatus();
    }

    public int getCardsLeft(String playerName) {
        for (int i=0; i < 7; i++) {
            if (players[i] != null && players[i].getName().equalsIgnoreCase(playerName)) {
                return players[i].getNumCards();
            }
        }
        return 0;
    }

    public int getCardsLeftBySeat(int seatNo) {
        return players[seatNo] == null ? 0 : players[seatNo].getNumCards();
    }

    public int getStrikes(String playerName) {
        for (int i=0; i < 7; i++) {
            if (players[i] != null && players[i].getName().equalsIgnoreCase(playerName)) {
                return players[i].getNumStrikes();
            }
        }
        return 0;
    }

    public int getStrikesBySeat(int seatNo) {
        return players[seatNo] == null ? 0 : players[seatNo].getNumStrikes();
    }
}
