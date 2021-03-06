package com.sentinella.james;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class Server implements Runnable {
    protected ServerTable     sTable;
    protected Lobby           sLobby;
    private   ClientCallback  uiThread;
    private   boolean         keepAlive = true;

    private   int             handsDealt  = 1;
    private   SERVERSTATE     state;
    private   long            timeoutTime = 0;
    private   long            lobbyTime   = 0;

    protected ArrayList<SocketChannel> cons;

    private   int             port    = 36789;
    private   int             lobbyTimeOut;
    private   int             playTimeOut;
    private   int             minPlayers;
    private   int             strikesAllowed;
    private   int             maxClients;
    private   int             lobbyInterval = 10000;
    private   int             clientSinceBroadcast = 0;

    protected LogBook log = new LogBook();

    public Server(int lobbyTimeOut, int playTimeOut, int minPlayers, int strikesAllowed, int maxClients, int port) throws UnknownHostException {
        this.lobbyTimeOut   = lobbyTimeOut * 1000;
        this.playTimeOut    = playTimeOut * 1000;
        this.minPlayers     = minPlayers;
        this.strikesAllowed = strikesAllowed;
        this.maxClients     = maxClients;
        this.port           = port;

        sLobby = new ServerLobby();
        sTable = new ServerTable(minPlayers, (ServerLobby) sLobby);
        cons   = new ArrayList<>();
    }

    public Server(int lTO, int pTO, int minPl, int stAl, int maxCl, int port, LogBook l, String dStr) throws UnknownHostException {
        this(lTO,pTO, minPl,stAl,maxCl,port);
        this.log = LogBookFactory.getLogBook(l,dStr);
        sLobby.setLogBookInfo(l,String.format("%s:%s",dStr,"SERVERLOBBY"));
        sTable.setLogBookInfo(l,String.format("%s:%s",dStr,"SERVERTABLE"));
    }

    @Override
    public void run() {
        ServerSocket socket = null;
        try {
            log.printDebMsg(String.format("%s server.run - Starting Procedure to Establish Socket", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),1);
            log.printOutMsg(String.format("Attempting to establish server on port %d",port));
            socket = new ServerSocket(port);
            socket.establishConnection();
            log.printOutMsg(String.format("Server live at %s", InetAddress.getLocalHost().getHostAddress()));
            log.printOutLine();
            log.printOutMsg(String.format("Clients connected: %d",cons.size()));
            log.printOutLine();
            log.printDebMsg(String.format("%s server.run - Setting server State to INSUFFICIENT PLAYERS", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            state = SERVERSTATE.INSUFFPLAYERS;
            log.printDebMsg(String.format("%s server.run - Starting Loop", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),1);
            while (keepAlive) {
                Iterator<SelectionKey>  keyIter = socket.select(1000).iterator();
                while (keyIter.hasNext()) {
                    SelectionKey thisKey = keyIter.next();
                    if (thisKey.isAcceptable()) {
                        log.printDebMsg(String.format("%s server.run - New Connection", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                        // Accept new connection.
                        SocketChannel client = socket.accept();
                        if (cons.size() < maxClients) {
                            log.printDebMsg(String.format("%s server.run - Adding Client", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                            cons.add(client);
                            updateClients();
                            log.printOutMsg(String.format("Clients connected: %d", cons.size()));
                            log.printOutLine();
                        } else {
                            log.printDebMsg(String.format("%s server.run - Too many clients.", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                            PrintWriter outWriter = new PrintWriter(client.socket().getOutputStream(), true);
                            log.printDebConMsg("[strik|81|0]");
                            outWriter.println("[strik|81|0]");
                            client.close();
                        }
                    } else if (thisKey.isReadable()) {
                        log.printDebMsg(String.format("%s server.run - New Message", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                        SocketChannel client  = (SocketChannel) thisKey.channel();
                        ByteBuffer    buffer  = ByteBuffer.allocate(256);
                        ServerPlayer  thisGuy = (ServerPlayer) ((ServerLobby)sLobby).getPlayer(client);
                        if (thisGuy == null) {
                            log.printDebMsg(String.format("%s server.run - Unknown User, adding new server Player", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                            thisGuy = new ServerPlayer(null,client,thisKey);
                            sLobby.addPlayer(thisGuy);
                        }
                        int bufSz = 0;
                        try {
                            bufSz = client.read(buffer);
                        } catch (IOException e) {
                            removeClient(client);
                        }
                        do {
                            if (bufSz == -1) {
                                log.printDebMsg(String.format("%s server.run - No Message, Remove Client", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                                removeClient(client);
                            } else {
                                thisGuy.addItem(new String(buffer.array()).trim());
                            }
                            try {
                                bufSz = client.read(buffer);
                            } catch (IOException e) {
                                removeClient(client);
                            }
                        } while (bufSz > 0);
                        processMessage(thisGuy);
                    }
                    keyIter.remove();
                }
                if (clientSinceBroadcast > 0) broadcastLobby(null, false);
                log.printDebMsg(String.format("%s server.run - Checking State", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),1);
                switch (state) {
                    case INSUFFPLAYERS:
                        log.printDebMsg(String.format("%s server.run - INSUFFICIENT PLAYERS", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                        if (minPlayers <= sLobby.numInLobby()) {
                            state = SERVERSTATE.STARTTIMEBUFFER;
                            timeoutTime = System.currentTimeMillis();
                            log.printOutMsg(String.format("Play starting in %d seconds.",lobbyTimeOut/1000));
                            log.printOutLine();
                        }
                        break;
                    case WAITFORPLAYMSG:
                        log.printDebMsg(String.format("%s server.run - WAITING FOR PLAY MESSAGE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                        long currentTime;
                        if (sTable.getActivePlayers() == 0) {
                            insuffPlayers();
                        } else if (sTable.getCurrentPlayerStatus() == pStatus.DISCONNECTED) {
                            sTable.nextPlayer();
                            state = sTable.play();
                            timeoutTime = System.currentTimeMillis();
                        } else {
                            if (playTimeOut == 0) break;
                            currentTime = System.currentTimeMillis();
                            log.printDebMsg(String.format("%s server.run - Checking Timeout Values - TimeoutVal: '%d' Current Time:'%d'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),timeoutTime+playTimeOut,currentTime),2);
                            if (currentTime > timeoutTime + playTimeOut) {
                                sTable.strikeActiveAndPass(20);
                                sTable.nextPlayer();
                                state = sTable.play();
                                timeoutTime = System.currentTimeMillis();
                            }
                        }
                        break;
                    case WAITFORSWAPMSG:
                        log.printDebMsg(String.format("%s server.run - WAITING FOR SWAP MESSAGE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                        if (sTable.getActivePlayers() == 0) {
                            insuffPlayers();
                        } else if (sTable.getSeatStatus(sTable.getScumbagSeat()) == pStatus.DISCONNECTED) {
                            sTable.sendSwapScum(null);
                            state = sTable.startGame();
                            timeoutTime = System.currentTimeMillis();
                        } else {
                            if (playTimeOut == 0) break;
                            currentTime = System.currentTimeMillis();
                            if (currentTime > timeoutTime + playTimeOut) {
                                sTable.addStrike(sTable.getWarlordSeat(), 20);
                                sTable.sendSwapScum(null);
                                state = sTable.startGame();
                                timeoutTime = System.currentTimeMillis();
                            }
                        }
                        break;
                    case STARTTIMEBUFFER: // Only brand new game gets here.
                        log.printDebMsg(String.format("%s server.run - Buffer before game start", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                        currentTime = System.currentTimeMillis();
                        if (currentTime > timeoutTime + lobbyTimeOut) {
                            log.printOutMsg(String.format("Let the game begin! %d hands dealt.", handsDealt++));
                            log.printOutLine();
                            sTable.setNotRanked(true);
                            state = sTable.newHand();
                            timeoutTime = System.currentTimeMillis();
                        }
                        break;
                    case NEWGAME:
                        log.printDebMsg(String.format("%s server.run - NEW GAME", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                        log.printOutMsg(String.format("A new round is starting. %d hands dealt.", handsDealt++));
                        log.printOutLine();
                        sTable.setNotRanked(false);
                        state = sTable.newHand();
                        break;
                }
            }
            log.printDebConMsg("[squit]");
            ((ServerLobby)sLobby).broadcastMessage("[squit]");
            socket.close();
        } catch (BindException e) {
            log.printErrMsg("Unable to establish server connection. Terminating.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                log.printDebConMsg("[squit]");
                ((ServerLobby)sLobby).broadcastMessage("[squit]");
                if (socket != null) socket.close();
            } catch (Exception e) {
                log.printErrMsg("Unable to send quit message.");
            }
        }
        uiThread.finished();
        this.done();
    }

    private void processMessage(ServerPlayer thisGuy) {
        log.printDebMsg(String.format("%s server.processMessage", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        String leftOvers = thisGuy.getMessage();
        if (leftOvers.length() < 1) return;
        Matcher outMatcher, inMatcher;
        outMatcher = RegexPatterns.oneMessage.matcher(leftOvers);
        String msg, cmd;
        while (outMatcher.find()) {
            log.printDebMsg(String.format("%s server.processMessage - Message found. User: %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),thisGuy.getName()),2);
            inMatcher = RegexPatterns.generalMessage.matcher(outMatcher.group("msg"));
            if (inMatcher.find()) {
                msg = inMatcher.group("msg");
                cmd = inMatcher.group("cmd");
                log.printDebMsg(String.format("%s server.processMessage - Message found. Total: '%s' Cmd: '%s' Msg: '%s'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),leftOvers, cmd, msg),2);
                switch (cmd.toLowerCase()) {
                    case "cquit":
                        removeClient(thisGuy.getCon());
                        break;
                    case "cjoin":
                        dealWithJoin(thisGuy, msg);
                        break;
                    case "cchat":
                        dealWithChat(thisGuy, msg);
                        break;
                    case "cplay":
                        dealWithPlay(thisGuy, msg);
                        break;
                    case "chand":
                        dealWithHand(thisGuy);
                        break;
                    case "cswap":
                        dealWithSwap(thisGuy, msg);
                        break;
                    default:
                        sendStrike(thisGuy, 33);
                }
            }
            leftOvers  = outMatcher.group("leftover");
            outMatcher = RegexPatterns.oneMessage.matcher(leftOvers);
        }
        thisGuy.setMessageBuffer(leftOvers);
    }

    private void dealWithJoin(ServerPlayer thisGuy, String msg) {
        log.printDebMsg(String.format("%s server.dealWithJoin - User: '%s' Msg: '%s'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),thisGuy.getName(),msg),3);
        if (thisGuy.getName() != null) {
            sendStrike(thisGuy, 30);
            return;
        }
        StringBuilder clientName = new StringBuilder(msg);
        Random        gen        = new Random();
        if (clientName.length() > 8) { // too long
            sendStrike(thisGuy, 32);
            return;
        } else if (clientName.length() < 8) { // too short
            sendStrike(thisGuy, 34);
            return;
        }
        if (Character.isDigit(clientName.charAt(0))) { // not allowed to start with digit
            clientName.insert(0,'A');
            clientName.deleteCharAt(8);
            assert clientName.length() == 8;
        }
        int charLen = clientName.length(); // 8
        for (int i=0; i < charLen; i++) {
            char thisChar = clientName.charAt(i);
            if (!isValidChar(thisChar)) { // replace invalid char with number
                clientName.replace(i,i+1,Character.toString((char)('0' + gen.nextInt(10))));
            } else if (thisChar == ' ') { // no spaces allowed, end rest with spaces if found
                clientName.replace(i, clientName.length(), "        ");
                break;
            }
        }
        boolean checkAgain = true;
        while (checkAgain) {
            checkAgain = false;
            if (((ServerLobby)sLobby).playerExists(clientName.toString().trim())) {
                checkAgain = true;
                int spaceIndex = 8;
                for (int i=0; i<8; i++) {
                    if (clientName.charAt(i) == ' ') {
                        spaceIndex = i;
                        break;
                    }
                }
                spaceIndex = spaceIndex > 5 ? 5 : spaceIndex;
                clientName.replace(spaceIndex, 8, String.format("%03d",gen.nextInt(1000)));
            }
        }
        String finalName = clientName.substring(0,8);
        log.printDebConMsg(String.format("[sjoin|%-8s]",finalName));
        if (thisGuy.sendMessage(String.format("[sjoin|%-8s]",finalName)) == ServerPlayer.CONERROR.UNABLETOSEND) {
            removeClient(thisGuy.getCon());
            return;
        }
        thisGuy.setName(finalName.trim());
        broadcastLobby(thisGuy, false);
        if (state == SERVERSTATE.WAITFORPLAYMSG) {
            log.printDebConMsg(sTable.getTableMessage());
            if (thisGuy.sendMessage(sTable.getTableMessage()) == ServerPlayer.CONERROR.UNABLETOSEND) removeClient(thisGuy.getCon());
        }
        updateClients();
    }

    private void broadcastLobby(ServerPlayer thisGuy, boolean force) {
        log.printDebMsg(String.format("%s server.broadcastLobby - Sending lobby message", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        String lobbyString = ((ServerLobby)sLobby).getLobbyMessage();
        long newTime = System.currentTimeMillis();
        if (newTime > lobbyTime + lobbyInterval || force) {
            clientSinceBroadcast = 0;
            log.printDebConMsg(lobbyString);
            ((ServerLobby) sLobby).broadcastMessage(lobbyString);
            lobbyTime = newTime;
        } else if (thisGuy != null) {
            clientSinceBroadcast++;
            log.printDebConMsg(lobbyString);
            if (thisGuy.sendMessage(lobbyString) == ServerPlayer.CONERROR.UNABLETOSEND) removeClient(thisGuy.getCon());
        }
    }

    protected void updateClients() {}

    private boolean isLetter(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isValidChar(char c) {
        return (isLetter(c) || isDigit(c) || c == '_' || c == ' ');
    }

    private void dealWithChat(ServerPlayer thisGuy, String msg) {
        log.printDebMsg(String.format("%s server.dealWithChat - User: '%s' Msg: '%s'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),thisGuy.getName(),msg),3);
        if (msg.length() > 63) {
            sendStrike(thisGuy,32);
            return;
        } else if (msg.length() < 63) {
            sendStrike(thisGuy,34);
            return;
        }
        ((ServerLobby)sLobby).broadcastMessage(String.format("[schat|%-8s|%63s]",thisGuy.getName(),msg));
    }

    private void dealWithPlay(ServerPlayer thisGuy, String msg) {
        log.printDebMsg(String.format("%s server.dealWithPlay - User: '%s' Msg: '%s'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),thisGuy.getName(),msg),3);
        Matcher cardMatcher = RegexPatterns.clientPlay.matcher(msg);
        if (thisGuy.getName() == null) {
            removeClient(thisGuy.getCon());
            return;
        } if (((ServerLobby)sLobby).isInLobby(thisGuy)) {
            sendStrike(thisGuy,31);
            return;
        } if (!sTable.isCurrentPlayer(thisGuy)) {
            sendStrike(thisGuy,15);
            return;
        } if (msg.length() > 11) {
            sendStrike(thisGuy,33);
            return;
        }
        if (cardMatcher.find()) {
            int[] cards = { Integer.parseInt(cardMatcher.group("c1")), Integer.parseInt(cardMatcher.group("c2")), Integer.parseInt(cardMatcher.group("c3")), Integer.parseInt(cardMatcher.group("c4"))};
            log.printDebMsg(String.format("%s Server.dealWithPlay Cards: %d %d %d %d", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),cards[0],cards[1],cards[2],cards[3]),2);
            int errorVal = sTable.checkPlay(cards);
            switch (errorVal) {
                case 0:
                    sTable.setInPlay(cards[0],cards[1],cards[2],cards[3]);
                    sTable.nextPlayer();
                    break;
                case -1: // pass
                    sTable.nextPlayer();
                    thisGuy.setStatus(pStatus.PASSED);
                    break;
                case -2:
                    sTable.nextPlayer();
                    sTable.getCurrentPlayer().setStatus(pStatus.PASSED);
                    sTable.nextPlayer();
                    sTable.setInPlay(cards[0],cards[1],cards[2],cards[3]);
                    sTable.sortInPlay();
                    break;
                case -3:
                    sTable.newRound();
                    break;
                default:
                    sendStrike(thisGuy, errorVal);
                    thisGuy.sendHand();
                    return;
            }
            for (int c: cards) {
                if (thisGuy.hasCard(c)) thisGuy.removeCard(c);
                else if (c != 52) log.printErrMsg("Card not in player's hand.");
            }

            if (thisGuy.getNumCards() == 0) {
                sTable.addToFinished(thisGuy);
                log.printOutMsg(String.format("%s added to finished list.",thisGuy.getName()));
                log.printOutLine();
            }
            if (state == SERVERSTATE.WAITFORPLAYMSG) {
                state = sTable.play();
                timeoutTime = System.currentTimeMillis();
            }
        } else {
            sendStrike(thisGuy,34);
        }
    }

    private void dealWithHand(ServerPlayer thisGuy) {
        log.printDebMsg(String.format("%s server.dealWithHand - User: '%s'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),thisGuy.getName()),3);
        if (sTable.isAtTable(thisGuy) && thisGuy.sendHand() == ServerPlayer.CONERROR.UNABLETOSEND) removeClient(thisGuy.getCon());
    }

    private void dealWithSwap(ServerPlayer thisGuy, String msg) {
        log.printDebMsg(String.format("%s server.dealWithSwap - User: '%s' Msg: '%s'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),thisGuy.getName(),msg),3);
        if (msg.length()>2) {
            sendStrike(thisGuy,32);
            return;
        } if (msg.length()<2) {
            sendStrike(thisGuy,34);
            return;
        } if (!sTable.isWarlord(thisGuy)) {
            sendStrike(thisGuy,71);
            return;
        } if (state != SERVERSTATE.WAITFORSWAPMSG) {
            sendStrike(thisGuy,72);
            return;
        }
        int cardNo;
        try {
            cardNo = Integer.parseInt(msg);
            if (!thisGuy.hasCard(cardNo)) {
                sendStrike(thisGuy,70);
                thisGuy.sendHand();
                timeoutTime = System.currentTimeMillis();
                return;
            }
            sTable.sendSwapScum(Card.CardCreator(cardNo));
            state = SERVERSTATE.WAITFORPLAYMSG;
            sTable.startGame();
        } catch (NumberFormatException e) {
            sendStrike(thisGuy,30);
        }
    }

    private void insuffPlayers() {
        state = SERVERSTATE.INSUFFPLAYERS;
        log.printOutMsg("No players at table. Waiting for players.");
        log.printOutLine();
    }

    private void sendStrike(ServerPlayer player, int strikeNo) {
        log.printDebMsg(String.format("%s server.sendStrike - User: '%s' No: '%d'", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),player.getName(),strikeNo),3);
        log.printOutMsg(String.format("Sending strike to %s because: %s",player.getName(),StrikeErrors.getErrorMessage(strikeNo)));
        log.printOutLine();
        if (player.sendStrike(strikeNo) == ServerPlayer.CONERROR.UNABLETOSEND) removeClient(player.getCon());
        else if (player.getNumStrikes() >= strikesAllowed) removeClient(player.getCon());
    }

    public void setUiThread(ClientCallback uiThread) {
        this.uiThread = uiThread;
    }

    public void quit() {
        keepAlive = false;
    }

    public void removeClient(SocketChannel client) {
        log.printDebMsg(String.format("%s server.dealWithChat - Removing Client", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),3);
        try {
            client.close();
        } catch (IOException e) { log.printErrMsg("Unable to close socket."); }
        ((ServerLobby)sLobby).removePlayer(client);
        broadcastLobby(null, false);
        if (cons.contains(client)) {
            cons.remove(client);
            log.printOutMsg(String.format("Clients connected: %d",cons.size()));
            log.printOutLine();
        }
        updateClients();
    }

    public void done() { }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void setLobbyTimeOut(int lobbyTimeOut) {
        this.lobbyTimeOut = lobbyTimeOut;
    }

    public void setPlayTimeOut(int playTimeOut) {
        this.playTimeOut = playTimeOut;
    }

    public void setStrikesAllowed(int strikesAllowed) {
        this.strikesAllowed = strikesAllowed;
    }

    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    public enum SERVERSTATE { INSUFFPLAYERS, WAITFORPLAYMSG, WAITFORSWAPMSG, STARTTIMEBUFFER, NEWGAME }
}
