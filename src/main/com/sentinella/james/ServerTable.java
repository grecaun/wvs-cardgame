package com.sentinella.james;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ServerTable extends Table {
    private ServerLobby lobby;

    private ArrayList<Player>   finishedPlayers;

    private int         minPlayers;
    private int         currentSeat = 0;
    private Card        warlordCard;

    public ServerTable(int minPlayers, ServerLobby lobby) {
        super();
        this.minPlayers = minPlayers;
        this.lobby      = lobby;
        finishedPlayers = new ArrayList<>();
    }

    public Server.SERVERSTATE newHand() {
        log.printDebMsg(String.format("%s ServerTable.newHand", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        log.printOutMsg("Arranging the table.");
        log.printOutLine();
        arrangeTable();
        if (getActivePlayers() < minPlayers) {
            log.printOutMsg("Insufficient players. Returning players to lobby until more clients join.");
            log.printOutLine();
            insufficientPlayers();
            log.printDebMsg(String.format("%s ServerTable.newHand - Setting State to INSUFFPLAYERS", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            return Server.SERVERSTATE.INSUFFPLAYERS;
        }
        log.printOutMsg("Setup up cards on table.");
        log.printOutLine();
        resetCards();
        log.printOutMsg("Shuffling deck.");
        log.printOutLine();
        if (shuffle()) {
            log.printOutMsg("Something went wrong with the shuffle.");
            log.printOutLine();
            insufficientPlayers();
            log.printDebMsg(String.format("%s ServerTable.newHand - Shuffle problems. Setting to INSUFFPLAYERS", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            return Server.SERVERSTATE.INSUFFPLAYERS;
        }
        if (isNotRanked) {
            log.printOutMsg("Starting unranked game. Player with three of clubs goes first.");
            log.printOutLine();
            log.printDebMsg(String.format("%s ServerTable.newHand - Unranked Start, returning startGame() value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            return startGame();
        }
        log.printOutMsg("Performing card swap.");
        log.printOutLine();
        log.printDebMsg(String.format("%s ServerTable.newHand - Ranked start, swapping cards, returning swap() value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        return swap();
    }

    public Server.SERVERSTATE startGame() {
        log.printDebMsg(String.format("%s ServerTable.startGame", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        if (isNotRanked) {
            log.printDebMsg(String.format("%s ServerTable.startGame - unranked start, find card 0", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            for (int i=0;i<7;i++) {
                if (players[i].hasCard(0)) {
                    setCurrentSeat(i);
                    setInPlay(0,52,52,52);
                    players[i].removeCard(0);
                    nextPlayer();
                    break;
                }
            }
        }
        for (Player p : players) {
            if (p != null) {
                ((ServerPlayer) p).sendHand();
                log.printOutMsg(String.format("%s has %s%s", p.getName(),p.getNumCards()==0?"no cards. ":"these cards: ", p.getHand().toString()));
            }
        }
        log.printOutLine();
        log.printDebMsg(String.format("%s ServerTable.startGame - Returning play() value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        return play();
    }

    public Server.SERVERSTATE play() {
        log.printDebMsg(String.format("%s ServerTable.play", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        if (players[currentSeat] == null || players[currentSeat].getNumCards() < 1) nextPlayer();
        if (playersWithCards() < 2) {
            addLastPlayer();
            log.printDebMsg(String.format("%s ServerTable.play - Less than 2 players with cards - State NEWGAME", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            return Server.SERVERSTATE.NEWGAME;
        }
        checkStatus();
        lobby.broadcastMessage(getTableMessage());
        log.printDebMsg(String.format("%s ServerTable.play - State WAITFORPLAYMSG", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        return Server.SERVERSTATE.WAITFORPLAYMSG;
    }

    private Server.SERVERSTATE swap() {
        log.printDebMsg(String.format("%s ServerTable.swap", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        warlordCard = players[getScumbagSeat()].getHand().getHighest();
        setCurrentSeat(getWarlordSeat());
        if (((ServerPlayer)(players[getWarlordSeat()])).sendSwapW(warlordCard) == ServerPlayer.CONERROR.UNABLETOSEND) {
            log.printDebMsg(String.format("%s ServerTable.swap - unable to contact warlord", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            log.printOutMsg("Unable to send swap message to warlord.");
            log.printOutLine();
            players[getWarlordSeat()].removeCard(warlordCard);
            players[getScumbagSeat()].addCard(warlordCard);
            ((ServerPlayer)players[getScumbagSeat()]).sendSwapS(null, null);
            nextPlayer();
            log.printDebMsg(String.format("%s ServerTable.swap - returning startGame value", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            return startGame();
        }
        log.printDebMsg(String.format("%s ServerTable.swap - State WAITFORSWAPMSG", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        return Server.SERVERSTATE.WAITFORSWAPMSG;
    }

    public void setWarlordCard(Card card) {
        warlordCard = card;
    }

    public void addStrike(int seatNo, int strikeNo) {
        if (players[seatNo] != null) ((ServerPlayer)players[seatNo]).sendStrike(strikeNo);
    }

    public void strikeActiveAndPass(int strikeNo) {
        int activePerson = currentSeat;
        nextPlayer();
        if (players[activePerson] != null) {
            ((ServerPlayer)players[activePerson]).sendStrike(strikeNo);
            players[activePerson].setStatus(pStatus.PASSED);
        }
    }

    public void sendSwapScum(Card card) {
        if (players[getWarlordSeat()] != null) players[getWarlordSeat()].removeCard(card);
        if (players[getScumbagSeat()] != null) ((ServerPlayer)players[getScumbagSeat()]).sendSwapS(card, warlordCard);
    }

    public pStatus getCurrentPlayerStatus() {
        return players[currentSeat] == null ? pStatus.EMPTY : players[currentSeat].getStatus();
    }

    /* CHECKED */

    private void initializeVariables() {
        for (int i=0; i<7; i++) players[i] = null;
        currentSeat = 0;
        lobby.sendEmptyTable();
    }

    public void arrangeTable() {
        log.printDebMsg(String.format("%s ServerTable.arrangeTable", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        initializeVariables();
        int numAtTable = 0;
        while (finishedPlayers.size() > 0 && numAtTable < 7) {
            players[numAtTable] = finishedPlayers.remove(0);
            ((ServerPlayer)players[numAtTable]).clearCards();
            players[numAtTable++].setStatus(pStatus.WAITING);
        }
        if (numAtTable <= 1) {
            setNotRanked(true);
        } else if (numAtTable < 7 && !isNotRanked) {
            players[getScumbagSeat()] = players[numAtTable-1];
            players[numAtTable-1] = null;
        }
        for (int i=0; i<7; i++) {
            if (lobby.numInLobby() == 0) break;
            if (players[i] == null) {
                players[i] = lobby.getNextPlayer();
                players[i].setStatus(pStatus.WAITING);
                ((ServerPlayer)players[i]).clearCards();
                numAtTable++;
            }
        }
        if (numAtTable > 7) {
            log.printOutMsg("Counted too many players at the table.");
            log.printOutLine();
        }
        if (players[currentSeat]!=null) players[currentSeat].setStatus(pStatus.ACTIVE);
    }

    public int getActivePlayers() {
        int numActive = 0;
        for (int i=0;i<7;i++) {
            if (players[i]!=null && players[i].getStatus() != pStatus.DISCONNECTED) numActive++;
        }
        return numActive;
    }

    private void resetCards() {
        inPlay[0] = null;
        inPlay[1] = null;
        inPlay[2] = null;
        inPlay[3] = null;
    }

    private void insufficientPlayers() {
        for (int i=0; i<7; i++) {
            if (players[i] != null) lobby.addToLobby(players[i]);
        }
        setNotRanked(true);
        initializeVariables();
    }

    private boolean shuffle() {
        log.printDebMsg(String.format("%s ServerTable.shuffle", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        ArrayList<Integer>  deck      = new ArrayList<>();
        Random              generator = new Random();
        for (int i=0; i<52; i++) deck.add(i);
        int cardsLeft  = deck.size();
        int errorcheck = 0;
        while (cardsLeft > 0) {
            errorcheck = cardsLeft;
            for (int i=0; i<7; i++) {
                if (players[i] != null && cardsLeft > 0) {
                    players[i].addCard(deck.remove(generator.nextInt(cardsLeft--)));
                }
            }
            if (errorcheck == cardsLeft) return true;
        }
        return false;
    }

    /**
     * Here we don't care what the status is unless it's disconnected.  If passed they were passed before their current turn, so it doesn't matter.
     * @return
     */
    public boolean nextPlayer() {
        log.printDebMsg(String.format("%s ServerTable.nextPlayer", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        int oldPlayer = currentSeat;
        for (int i=1;i<8;i++) {
            setCurrentSeat((oldPlayer + i) % 7);
            if (players[currentSeat] != null && players[currentSeat].getStatus() != pStatus.DISCONNECTED && players[currentSeat].getNumCards() > 0) break;
        }
        if (oldPlayer == currentSeat) return false;
        return true;
    }

    public Player getCurrentPlayer() {
        return players[currentSeat];
    }

    private int playersWithCards() {
        int output = 0;
        for (int i=0; i<7; i++) {
            if (players[i]!=null && players[i].getNumCards()>0) output++;
        }
        return output;
    }

    private void addLastPlayer() {
        int count = 0;
        for (int i=0; i<7; i++) {
            if (players[i]!=null && players[i].getNumCards()>0) {
                count++;
                finishedPlayers.add(players[i]);
            }
        }
        if (count > 1) { log.printErrMsg("More than one player left."); }
    }

    private void checkStatus() {
        boolean allPassed = true;
        for (int i=0; i<7; i++) {
            if (players[i] != null && i != currentSeat && players[i].getNumCards() > 0 && players[i].getStatus() == pStatus.WAITING) {
                allPassed = false;
            }
        }
        if (allPassed) newRound();
    }

    private void setCurrentSeat(int newSeat) {
        if (players[currentSeat] != null && players[currentSeat].getStatus() == pStatus.ACTIVE) players[currentSeat].setStatus(pStatus.WAITING);
        currentSeat = newSeat;
        if (players[currentSeat] != null && players[currentSeat].getStatus() != pStatus.DISCONNECTED) players[currentSeat].setStatus(pStatus.ACTIVE);
    }

    public void newRound() {
        resetCards();
        for (int i=0; i<7; i++) {
            if (players[i] != null && players[i].getStatus() != pStatus.DISCONNECTED) {
                players[i].setStatus(i==currentSeat ? pStatus.ACTIVE : pStatus.WAITING);
            }
        }
    }

    public String getTableMessage() {
        log.printDebMsg(String.format("%s ServerTable.getTableMessage", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        StringBuilder outBuild = new StringBuilder("[stabl|");
        for (int i=0; i<7; i++) {
            if (players[i] != null) {
                switch (players[i].getStatus()) {
                    case DISCONNECTED:
                        outBuild.append("d");
                        break;
                    case PASSED:
                        outBuild.append("p");
                        break;
                    default:
                        if (i==currentSeat) outBuild.append("a");
                        else outBuild.append("w");
                }
                outBuild.append(String.format("%01d:%-8s:%02d,",players[i].getNumStrikes(),players[i].getName(),players[i].getNumCards()));
            } else {
                outBuild.append("e0:        :00,");
            }
        }
        outBuild.deleteCharAt(outBuild.length()-1);
        int[] cards = new int[4];
        for (int i=0; i<4; i++) cards[i] = inPlay[i] == null ? 52 : inPlay[i].getCardIndexNumber();
        outBuild.append(String.format("|%02d,%02d,%02d,%02d|%01d]",cards[0],cards[1],cards[2],cards[3],isNotRanked ? 0 : 1));
        return outBuild.toString();
    }

    public int getScumbagSeat() { return 6; }

    public int getWarlordSeat() {return 0; }

    public int checkPlay(int[] play) {
        log.printDebMsg(String.format("%s ServerTable.checkPlay", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        if (currentSeat < 0 || currentSeat > 6 || players[currentSeat] == null) return 99;
        int        currentNumCards = 0;
        boolean    match           = false;
        PlayerHand currentHand     = players[currentSeat].getHand();
        sortInPlay();
        Arrays.sort(play);
        for (int cardNo=0; cardNo<4; cardNo++) {
            if (play[cardNo] != 52) {
                currentNumCards++;
                if (!currentHand.hasCard(play[cardNo])) return 14;
                else if (cardNo > 0) {
                    for (int verify=0; verify < cardNo; verify++) {
                        if (play[cardNo] == play[verify]) return 17;
                        if (play[cardNo]/4 != play[verify]/4) return 11;
                    }
                }
                if (inPlay[0] != null && play[cardNo]/4 < inPlay[0].getCardNumericFaceValue()) return 12;
            }
            if (inPlay[0] != null && play[cardNo]/4 == inPlay[0].getCardNumericFaceValue()) match = true;
        }
        if (currentNumCards == 0) {
            if (numInPlay() == 0) return 18;
            else return -1;
        } else if (match && currentNumCards == numInPlay()) return -2;
        else if (numInPlay() > currentNumCards) return 13;
        else if (play[0]/4 == 12) return -3;
        return 0;
    }

    public boolean isAtTable(ServerPlayer thisGuy) {
        for (Player p: players) {
            if (p != null && p.isPlayer(thisGuy)) return true;
        }
        return false;
    }

    public boolean isCurrentPlayer(ServerPlayer thisGuy) {
        return players[currentSeat].isPlayer(thisGuy);
    }

    public void addToFinished(ServerPlayer thisGuy) {
        for (int i=0; i<7; i++) {
            if (players[i]!=null && players[i].isPlayer(thisGuy) && players[i].getStatus()!=pStatus.DISCONNECTED) finishedPlayers.add(thisGuy);
        }
    }

    public boolean isWarlord(Player player) {
        return players[getWarlordSeat()] != null && players[getWarlordSeat()].isPlayer(player);
    }
}
