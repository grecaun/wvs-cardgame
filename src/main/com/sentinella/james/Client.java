package com.sentinella.james;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.regex.Matcher;


/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class Client implements Runnable {
    private   InetAddress                 hostName    = InetAddress.getLocalHost();
    private   int                         hostPort    = 36789;
    private   boolean                     isAuto      = true;
    protected volatile ClientState        cState      = ClientState.INIT;
    private   ClientCallback              uiThread;

    private   WvSUpdater          updater;
    protected LogBook log = new LogBook();
    private   ClientSocket        socket;

    private   Table               cTable;
    private   Lobby               cLobby;
    private   PlayerHand          cHand;
    private   String              cName;
    private   int                 cStrikes;
    private   int                 delay = 0;

    public Client(String iHostName, int iHostPort, String iName, boolean iIsAuto) throws UnknownHostException {
        if (iHostName != null)
            hostName    = InetAddress.getByName(iHostName);
        if (iHostPort != 0)
            hostPort    = iHostPort;
        if (iName != null)
            cName = iName;
        else {
            Random seed = new Random();
            ArrayList<String> names = new ArrayList<>(Arrays.asList("Rhaegar ", "Bran    ", "Hodor   ", "Jon     ", "TheFuzz ", "Ned     ", "Bozo    ", "Captain ", "Spirit  ", "Wolf    "));
            cName = names.get(seed.nextInt(names.size()));
        }
        isAuto      = iIsAuto;
        cTable      = new Table();
        cLobby      = new Lobby();
        cHand       = new PlayerHand();
        cStrikes    = 0;
        updater     = new SimpleUpdater(cTable, cLobby, cHand, cName, cStrikes);
    }

    public Client(String iHostName, int iHostPort, String iName, boolean iIsAuto, LogBook iLog, String debugName) throws UnknownHostException {
        this(iHostName,iHostPort,iName,iIsAuto);
        log = LogBookFactory.getLogBook(iLog, debugName);
        updater.setLogBookInfo(log, String.format("%s:%s",log.getDebugStr(),"UPDATER"));
        cTable.setLogBookInfo(log, String.format("%s:%s",log.getDebugStr(),"TABLE"));
        cLobby.setLogBookInfo(log, String.format("%s:%s",log.getDebugStr(),"LOBBY"));
        cHand.setLogBookInfo(log, String.format("%s:%s",log.getDebugStr(),"PLAYERHAND"));
    }

    @Override
    public void run() {
        log.printDebMsg("run-START",3);
        try {
            log.printDebMsg("Establishing connection.",1);
            socket = new ClientSocket(hostName.getHostName(), hostPort, log, String.format("%s:%s",log.getDebugStr(),"CLIENTSOCKET"));
            socket.establishConnection();
            String message;
            StringBuilder carryOver;
            log.printDebMsg("Setting callback for UI thread.",2);
            uiThread.setOutConnection(socket);
            int len = cName.length();
            if (len < 8) {
                cName = cName + "       ".substring(0, 8 - len);
            }
            log.printDebConMsg(String.format("[cjoin|%8s]", cName));
            socket.sendMessage(String.format("[cjoin|%8s]", cName));
            log.printDebMsg("Setting client state to WAIT.",2);
            cState = ClientState.WAIT;
            carryOver = new StringBuilder();
            while (cState != ClientState.QUIT) {
                log.printDebMsg("Start of main loop.",1);
                for (SelectionKey key : socket.select(1000)) {
                    log.printDebMsg("Select came back.",2);
                    if (key.isReadable()) {
                        try {
                            message = socket.readLine();
                        } catch (IOException e) {
                            log.printErrMsg("Unable to receive message from server.");
                            cState = ClientState.QUIT;
                            break;
                        }
                        Matcher finder;
                        if ( message != null && message.length() > 0 ) {
                            log.printDebMsg("Message found. Appending to old information.",2);
                            carryOver.append(message);
                        }
                        log.printDebMsg("Processing message(s).",1);
                        message = carryOver.toString();
                        carryOver.setLength(0);
                        finder = RegexPatterns.oneMessage.matcher(message);
                        log.printDebMsg(String.format("Messages to be processed: %s", message),2);
                        while (finder.find()) {
                            log.printDebMsg(String.format("First group: '%s' Second group: '%s'", finder.group(1), finder.group(2)),2);
                            switch (parseMessage(finder.group(1))) {
                                case NAMERR:
                                    log.printDebMsg("server sent you a name that's the wrong length. Oh the horror.",1);
                                    cState = ClientState.QUIT;
                                    break;
                                case NOMATCH:
                                    log.printDebMsg("Unable to process the information in the message the server sent.",1);
                                    break;
                                case STRIKERR:
                                    log.printDebMsg("server sent some weird message masquerading as a strike message.",1);
                                    break;
                                case TABLERR:
                                    log.printDebMsg("server sent some random crap for a table message that isn't the right length",1);
                                    break;
                                case SWAPERRW:
                                    log.printDebMsg("Something went wrong with the server's warlord swap message.",1);
                                    break;
                                case SWAPERRS:
                                    log.printDebMsg("Something went wrong with the server's scumbag swap message.",1);
                                    break;
                                case LOBBYERR:
                                    log.printDebMsg("The wrong number of people in the lobby reported.",1);
                                    break;
                            }
                            message = finder.group(2);
                            finder = RegexPatterns.oneMessage.matcher(message);
                        }
                        carryOver.append(message);
                        if (isAuto) {
                            doSomething();
                        }
                    }
                }
            }
            log.printDebConMsg("[cquit]");
            socket.sendMessage("[cquit]");
            socket.close();
        } catch (IOException e) {
            log.printErrMsg("Unable to establish connection to server. Program terminating.");
            if (uiThread!=null) uiThread.unableToConnect();
        }
        log.printDebMsg("Notifying uithread that we're done.",1);
        if (uiThread!=null) uiThread.finished();
        log.printDebMsg("All done.",1);
        log.printDebMsg("run-END",3);
    }

    private void doSomething() {
        log.printDebMsg("doSomething-START",3);
        switch (cState) {
            case CLIENTTURN:
                log.printDebMsg("Automatic play - client turn",2);
                try {
                    Thread.sleep(delay * 1000);  // artificial delay so games are slower
                } catch (InterruptedException e) {
                    log.printErrMsg("Something went wrong when we tried to wait.");
                }
                if (cHand.count() != 0) {
                    log.printDebMsg("Player has cards.",2);
                    int toMatch = cTable.numInPlay();
                    int valToMatch = cTable.getInPlayValue();
                    if (toMatch == 0) {
                        valToMatch = 0;
                    }
                    cTable.sortInPlay();
                    List<Card> playCards = cHand.getLowest(valToMatch, cTable.numInPlay());
                    cState = sendPlay(playCards);
                } else {
                    log.printDebMsg("Player has no cards.",2);
                    cState = sendPlay(null);
                }
                break;
            case SWAP:
                log.printDebMsg("Automatic play - swap",2);
                try {
                    Thread.sleep(delay * 1000);  // artificial delay so games are slower
                } catch (InterruptedException e) {
                    log.printErrMsg("Something went wrong when we tried to wait.");
                }
                if (cHand.count() != 0) {
                    log.printDebMsg("Player has cards.",2);
                    cHand.sort();
                    cState = sendSwap();
                } else {
                    log.printDebMsg("Player has no cards.",2);
                    cState = ClientState.WAIT;
                }
                break;
        }
        log.printDebMsg("doSomething-END",3);
    }

    private ClientState sendSwap() {
        log.printDebMsg("sendSwap-START",3);
        Card outCard = cHand.getLowest();
        log.printOutMsg(String.format("Giving the %s to the scumbag.", outCard.getStringRep()));
        String output = String.format("[cswap|%02d]",outCard.getCardIndexNumber());
        log.printDebConMsg(output);
        try {
            socket.sendMessage(output);
        } catch (IOException e) {
            quit();
        }
        log.printDebMsg("sendSwap-END",3);
        return ClientState.WAITSWAP;
    }

    private ClientState sendPlay(List<Card> playCards) {
        log.printDebMsg("sendPlay-START",3);
        StringBuilder outMsg = new StringBuilder();
        outMsg.append("[cplay|");
        int numPlayed = 0;
        if (playCards != null) {
            for (Card c : playCards) {
                outMsg.append(String.format("%02d", c.getCardIndexNumber()));
                if (++numPlayed < 4) outMsg.append(",");
            }
        }
        while (numPlayed < 4) {
            outMsg.append("52");
            if (++numPlayed < 4) outMsg.append(",");
        }
        outMsg.append("]");
        String output = outMsg.toString();
        log.printDebConMsg(output);
        try {
            socket.sendMessage(output);
        } catch (IOException e) {
            quit();
        }
        log.printDebMsg("sendPlay-END",3);
        return ClientState.WAITTURN;
    }

    public ClientConnection getOutConnection() { return socket; }

    public errVal parseMessage(String iMessage) {
        log.printDebMsg("parseMessage-START",3);
        Matcher lMatch = RegexPatterns.generalMessage.matcher(iMessage);
        if (lMatch.find()) {
            String cmd = lMatch.group(1);
            String information = lMatch.group(2);
            log.printDebMsg(String.format("Command: '%s' Information: '%s'",cmd,information),2);
            if (cmd.equalsIgnoreCase("slobb")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithLobby(information);
            } else if (cmd.equalsIgnoreCase("stabl")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithTable(information);
            } else if (cmd.equalsIgnoreCase("sjoin")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithJoin(information);
            } else if (cmd.equalsIgnoreCase("shand")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithHand(information);
            } else if (cmd.equalsIgnoreCase("strik")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithStrike(information);
            } else if (cmd.equalsIgnoreCase("schat")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithChat(information);
            } else if (cmd.equalsIgnoreCase("swapw")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithSwapW(information);
            } else if (cmd.equalsIgnoreCase("swaps")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithSwapS(information);
            } else if (cmd.equalsIgnoreCase("squit")) {
                log.printDebMsg("parseMessage-END",3);
                return dealWithSquit(information);
            }
        }
        System.err.println(String.format("Can't match '%s'",iMessage));
        log.printDebMsg("parseMessage-END",3);
        return errVal.NOMATCH;
    }

    private errVal dealWithSquit(String information) {
        log.printDebMsg("dealWithSquit-START",3);
        cState = ClientState.QUIT;
        log.printDebMsg("dealWithSquit-END",3);
        return errVal.NOERR;
    }

    private errVal dealWithLobby(String iInformation) {
        log.printDebMsg("dealWithLobby-START",3);
        Matcher matcher = RegexPatterns.serverLobby.matcher(iInformation);
        int numLobby = Integer.parseInt(iInformation.substring(0,2));
        cLobby.clear();
        ArrayList<String> names = new ArrayList<String>();
        while (matcher.find()) {
            cLobby.addPlayer(new Player(matcher.group(1)));
            names.add(matcher.group(1).trim());
        }
        if (numLobby != cLobby.numInLobby()) {
            cLobby.clear();
            log.printDebMsg("dealWithLobby-END",3);
            return errVal.LOBBYERR;
        }
        updater.updateLobby(names);
        log.printDebMsg("dealWithLobby-END",3);
        return errVal.NOERR;
    }

    private errVal dealWithTable(String iInformation) {
        log.printDebMsg("dealWithTable-START",3);
        if (iInformation.length() != 118) {
            return errVal.TABLERR;
        }
        Matcher matcher = RegexPatterns.serverTable.matcher(iInformation);
        if (matcher.find()) {
            String      statStrik;
            String      name;
            int         cardCount;
            pStatus     curStatus;
            int         curStrikes;
            cState      = ClientState.WAIT;
            for (int i=0; i<7; i++) {
                statStrik   = matcher.group((i*3)+1);
                name        = matcher.group((i*3)+2);
                cardCount   = Integer.parseInt(matcher.group((i*3)+3));
                switch (statStrik.charAt(0)) {
                    case 'a':
                    case 'A':
                        curStatus = pStatus.ACTIVE;
                        break;
                    case 'p':
                    case 'P':
                        curStatus = pStatus.PASSED;
                        break;
                    case 'w':
                    case 'W':
                        curStatus = pStatus.WAITING;
                        break;
                    case 'd':
                    case 'D':
                        curStatus = pStatus.DISCONNECTED;
                        break;
                    default:
                        curStatus = pStatus.EMPTY;
                }
                curStrikes = Integer.parseInt(statStrik.substring(1,2));
                if (curStatus == pStatus.EMPTY) {
                    cTable.removePlayer(i);
                } else {
                    cTable.setPlayer(i, curStatus, curStrikes, name, cardCount);
                    if (name.trim().equalsIgnoreCase(cName)) {
                        if (curStatus == pStatus.ACTIVE) {
                            updater.updateStatus(cTable);
                            cState = ClientState.CLIENTTURN;
                        }
                    }
                }
            }
            cTable.setInPlay(Integer.parseInt(matcher.group(22)),
                    Integer.parseInt(matcher.group(23)),
                    Integer.parseInt(matcher.group(24)),
                    Integer.parseInt(matcher.group(25)));
            cTable.setNotRanked(Integer.parseInt(matcher.group(26)) == 1);
            updater.updateTable(cTable);
            log.printDebMsg("dealWithTable-END",3);
            return errVal.NOERR;
        }
        log.printDebMsg("dealWithTable-END",3);
        return errVal.NOMATCH;
    }

    private errVal dealWithJoin(String iInformation) {
        log.printDebMsg("dealWithJoin-START",3);
        if (iInformation.length() != 8) {
            log.printDebMsg("dealWithJoin-END",3);
            return errVal.NAMERR;
        }
        cName = iInformation.trim();
        updater.updateJoin(cName);
        log.printDebMsg("dealWithJoin-END",3);
        return errVal.NOERR;
    }

    private errVal dealWithHand(String iInformation) {
        log.printDebMsg("dealWithHand-START",3);
        Matcher matcher = RegexPatterns.serverHand.matcher(iInformation);
        cHand.clear();
        int cardNum;
        while (matcher.find()) {
            cardNum = Integer.parseInt(matcher.group(1));
            if (cardNum < 52) {
                cHand.add(Card.CardCreator(cardNum));
            }
        }
        updater.updateHand(cHand.getHand());
        log.printDebMsg("dealWithHand-END",3);
        return errVal.NOERR;
    }

    private errVal dealWithStrike(String iInformation) {
        log.printDebMsg("dealWithStrike-START",3);
        if (iInformation.length() != 4) {
            return errVal.STRIKERR;
        }
        Matcher matcher = RegexPatterns.serverStrike.matcher(iInformation);
        if (matcher.find()) {
            int strikeVal = Integer.parseInt(matcher.group(1));
            if ((strikeVal / 10) == 7 && cState == ClientState.WAITSWAP) {
                cState = ClientState.SWAP;
                updater.updateStatus(cTable);
            } else if ((strikeVal / 10) == 1 && cState == ClientState.WAITTURN) {
                cState = ClientState.CLIENTTURN;
                updater.updateStatus(cTable);
            }
            cStrikes = Integer.parseInt(matcher.group(2));
            updater.updateStrike(strikeVal, cStrikes);
            log.printDebMsg("dealWithStrike-END",3);
            return errVal.NOERR;
        }
        log.printErrMsg("Strike error.");
        log.printDebMsg("dealWithStrike-END",3);
        return errVal.NOMATCH;
    }

    private errVal dealWithChat(String iInformation) {
        log.printDebMsg("dealWithChat-START",3);
        Matcher matcher = RegexPatterns.serverChat.matcher(iInformation);
        if (matcher.find()) {
            updater.updateChat(matcher.group(1).trim(),matcher.group(2).trim());
            log.printDebMsg("dealWithChat-END",3);
            return errVal.NOERR;
        }
        log.printDebMsg("dealWithChat-END",3);
        return errVal.NOMATCH;
    }

    private errVal dealWithSwapW(String iInformation) {
        log.printDebMsg("dealWithSwapW-START",3);
        log.printDebMsg("Received card from scumbag. Need to send new card out.",2);
        if (iInformation.length() != 2) {
            log.printErrMsg("SwapW message not the correct length.");
            log.printDebMsg("dealWithSwapW-END",3);
            return errVal.SWAPERRW;
        }
        cState = ClientState.SWAP;
        Card newCard = Card.CardCreator(Integer.parseInt(iInformation));
        cHand.add(newCard);
        cHand.sort();
        cHand.printHand();
        updater.updateSwapW(newCard);
        updater.updateStatus(cTable);
        log.printDebMsg("dealWithSwapW-END",3);
        return errVal.NOERR;
    }

    private errVal dealWithSwapS(String iInformation) {
        log.printDebMsg("dealWithSwapS-START",3);
        if (iInformation.length() != 5) {
            return errVal.SWAPERRS;
        }
        Matcher matcher = RegexPatterns.serverSwapS.matcher(iInformation);
        if (matcher.find()) {
            Card newCard = Card.CardCreator(Integer.parseInt(matcher.group(1)));
            Card oldCard = Card.CardCreator(Integer.parseInt(matcher.group(2)));
            cHand.remove(oldCard);
            cHand.add(newCard);
            updater.updateSwapS(newCard, oldCard);
            updater.updateStatus(cTable);
            log.printDebMsg("dealWithSwapS-END",3);
            return errVal.NOERR;
        }
        log.printErrMsg("Swap error");
        log.printDebMsg("dealWithSwapS-END",3);
        return errVal.NOMATCH;
    }

    public void setUpdater(WvSUpdater newUpdater) {
        updater = newUpdater;
    }

    public Table      getTable()   { return cTable; }
    public Lobby      getLobby()   { return cLobby; }
    public PlayerHand getHand()    { return cHand; }
    public String     getName()    { return cName; }
    public int        getStrikes() { return cStrikes; }

    public void       quit()     {
        log.printDebMsg("Setting state to quit.",1);
        cState = ClientState.QUIT;
    }

    public ClientState getState() {
        return cState;
    }

    public void setUiThread(ClientCallback uiThread) {
        this.uiThread = uiThread;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public enum ClientState {
        QUIT, CLIENTTURN, SWAP, WAITSWAP, WAIT, WAITTURN, INIT
    }

    public enum errVal {
        NOERR, NAMERR, STRIKERR, TABLERR, SWAPERRS, LOBBYERR, NOMATCH, SWAPERRW
    }
}