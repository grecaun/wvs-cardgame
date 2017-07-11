package com.sentinella.james;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.regex.Matcher;


/**
 * Created by James on 4/6/2016.
 */
public class Client implements Runnable {
    private   InetAddress                 hostName    = InetAddress.getLocalHost();
    private   int                         hostPort    = 36789;
    private   boolean                     isAuto      = true;
    protected volatile ClientState        cState      = ClientState.INIT;
    private   boolean                     debug       = false;
    private   ClientCallback              uiThread;

    private   WvSUpdater          updater;
    protected Printer             printer = new BasicPrinter();
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

    @Override
    public void run() {
        try {
            socket = new ClientSocket(hostName.getHostName(), hostPort);
            socket.establishConnection();
            String message;
            StringBuilder carryOver;
            uiThread.setOutConnection(socket);
            int len = cName.length();
            if (len < 8) {
                cName = cName + "       ".substring(0, 8 - len);
            }
            socket.sendMessage(String.format("[cjoin|%8s]", cName));
            if (debug) printer.printDebugMessage(String.format("[cjoin|%8s]", cName));
            cState = ClientState.WAIT;
            carryOver = new StringBuilder();
            while (cState != ClientState.QUIT) {
                if (debug) printer.printDebugMessage("Start of main loop.");
                for (SelectionKey key : socket.select(1000)) {
                    if (key.isReadable()) {
                        try {
                            message = socket.readLine();
                        } catch (IOException e) {
                            printer.printErrorMessage("Unable to receive message from server.");
                            cState = ClientState.QUIT;
                            break;
                        }
                        Matcher finder;
                        if ( message != null && message.length() > 0 ) {
                            carryOver.append(message);
                        }
                        message = carryOver.toString();
                        carryOver.setLength(0);
                        finder = RegexPatterns.oneMessage.matcher(message);
                        if (debug) printer.printDebugMessage(String.format("Messages to be processed: %s", message));
                        while (finder.find()) {
                            printer.printErrorMessage(String.format("First group: '%s' Second group: '%s'", finder.group(1), finder.group(2)));
                            switch (parseMessage(finder.group(1))) {
                                case NAMERR:
                                    printer.printErrorMessage("server sent you a name that's the wrong length. Oh the horror.");
                                    cState = ClientState.QUIT;
                                    break;
                                case NOMATCH:
                                    printer.printErrorMessage("Unable to process the information in the message the server sent.");
                                    break;
                                case STRIKERR:
                                    printer.printErrorMessage("server sent some weird message masquerading as a strike message.");
                                    break;
                                case TABLERR:
                                    printer.printErrorMessage("server sent some random crap for a table message that isn't the right length");
                                    break;
                                case SWAPERRW:
                                    printer.printErrorMessage("Something went wrong with the server's warlord swap message.");
                                    break;
                                case SWAPERRS:
                                    printer.printErrorMessage("Something went wrong with the server's scumbag swap message.");
                                    break;
                                case LOBBYERR:
                                    printer.printErrorMessage("The wrong number of people in the lobby reported.");
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
            socket.sendMessage("[cquit]");
            socket.close();
        } catch (IOException e) {
            printer.printErrorMessage("Unable to establish connection to server. Program terminating.");
            if (uiThread!=null) uiThread.unableToConnect();
        }
        if (debug) printer.printDebugMessage("Notifying uithread that we're done.");
        if (uiThread!=null) uiThread.finished();
        if (debug) printer.printDebugMessage("All done.");
    }

    private void doSomething() {
        switch (cState) {
            case CLIENTTURN:
                try {
                    Thread.sleep(delay * 1000);
                } catch (InterruptedException e) {
                    printer.printErrorMessage("Something went wrong when we tried to wait.");
                }
                if (cHand.count() != 0) {
                    int toMatch = cTable.numInPlay();
                    int valToMatch = cTable.getInPlayValue();
                    if (toMatch == 0) {
                        valToMatch = 0;
                    }
                    cTable.sortInPlay();
                    List<Card> playCards = cHand.getLowest(valToMatch, cTable.numInPlay());
                    cState = sendPlay(playCards);
                } else {
                    cState = ClientState.WAIT;
                }
                break;
            case SWAP:
                try {
                    Thread.sleep(delay * 1000);
                } catch (InterruptedException e) {
                    printer.printErrorMessage("Something went wrong when we tried to wait.");
                }
                if (cHand.count() != 0) {
                    cHand.sort();
                    cState = sendSwap();
                } else {
                    cState = ClientState.WAIT;
                }
                break;
        }
    }

    private ClientState sendSwap() {
        Card outCard = cHand.getLowest();
        printer.printString(String.format("Giving the %s to the scumbag.", outCard.getStringRep()));
        String output = String.format("[cswap|%02d]",outCard.getCardIndexNumber());
        try {
            socket.sendMessage(output);
        } catch (IOException e) {
            quit();
        }
        if (debug) printer.printDebugMessage(String.format("Sending swap message: %s",output));
        return ClientState.WAITSWAP;
    }

    private ClientState sendPlay(List<Card> playCards) { // this does not have the proper algorithm backing it.
        StringBuilder outMsg = new StringBuilder();
        outMsg.append("[cplay|");
        int numPlayed = 0;
        for (Card c : playCards) {
            outMsg.append(String.format("%02d",c.getCardIndexNumber()));
            if (++numPlayed < 4) outMsg.append(",");
        }
        while (numPlayed < 4) {
            outMsg.append("52");
            if (++numPlayed < 4) outMsg.append(",");
        }
        outMsg.append("]");
        String output = outMsg.toString();
        try {
            socket.sendMessage(outMsg.toString());
        } catch (IOException e) {
            quit();
        }
        if (debug) printer.printDebugMessage(String.format("Sending play message: %s",output));
        return ClientState.WAITTURN;
    }

    public ClientConnection getOutConnection() { return socket; }

    public errVal parseMessage(String iMessage) {
        Matcher lMatch = RegexPatterns.generalMessage.matcher(iMessage);
        if (lMatch.find()) {
            String cmd = lMatch.group(1);
            String information = lMatch.group(2);
            if (cmd.equalsIgnoreCase("slobb")) {
                return dealWithLobby(information);
            } else if (cmd.equalsIgnoreCase("stabl")) {
                return dealWithTable(information);
            } else if (cmd.equalsIgnoreCase("sjoin")) {
                return dealWithJoin(information);
            } else if (cmd.equalsIgnoreCase("shand")) {
                return dealWithHand(information);
            } else if (cmd.equalsIgnoreCase("strik")) {
                return dealWithStrike(information);
            } else if (cmd.equalsIgnoreCase("schat")) {
                return dealWithChat(information);
            } else if (cmd.equalsIgnoreCase("swapw")) {
                return dealWithSwapW(information);
            } else if (cmd.equalsIgnoreCase("swaps")) {
                return dealWithSwapS(information);
            } else if (cmd.equalsIgnoreCase("squit")) {
                return dealWithSquit(information);
            }
        }
        System.err.println(String.format("Can't match '%s'",iMessage));
        return errVal.NOMATCH;
    }

    private errVal dealWithSquit(String information) {
        cState = ClientState.QUIT;
        return errVal.NOERR;
    }

    private errVal dealWithLobby(String iInformation) {
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
            return errVal.LOBBYERR;
        }
        updater.updateLobby(names);
        return errVal.NOERR;
    }

    private errVal dealWithTable(String iInformation) {
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
            return errVal.NOERR;
        }
        return errVal.NOMATCH;
    }

    private errVal dealWithJoin(String iInformation) {
        if (iInformation.length() != 8) {
            return errVal.NAMERR;
        }
        cName = iInformation.trim();
        printer.printString(String.format("Joined server successfully. Your name is %s.", cName));
        updater.updatePlayer(cName);
        return errVal.NOERR;
    }

    private errVal dealWithHand(String iInformation) {
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
        return errVal.NOERR;
    }

    private errVal dealWithStrike(String iInformation) {
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
            printStrike(strikeVal);
            updater.updatePlayer();
            return errVal.NOERR;
        }
        System.err.println("Strike error.");
        return errVal.NOMATCH;
    }

    private errVal dealWithChat(String iInformation) {
        Matcher matcher = RegexPatterns.serverChat.matcher(iInformation);
        if (matcher.find()) {
            updater.updateChat(matcher.group(1).trim(),matcher.group(2).trim());
            return errVal.NOERR;
        }
        return errVal.NOMATCH;
    }

    private errVal dealWithSwapW(String iInformation) {
        printer.printErrorMessage("Received card from scumbag. Need to send new card out.");
        if (iInformation.length() != 2) {
            return errVal.SWAPERRW;
        }
        cState = ClientState.SWAP;
        Card newCard = Card.CardCreator(Integer.parseInt(iInformation));
        cHand.add(newCard);
        cHand.sort();
        printer.printString(String.format("The scumbag gave you (the warlord) the %s. They are demanding you give them a card in return",newCard.getStringRep()));
        cHand.printHand();
        updater.updateStatus(cTable);
        return errVal.NOERR;
    }

    private errVal dealWithSwapS(String iInformation) {
        if (iInformation.length() != 5) {
            return errVal.SWAPERRS;
        }
        Matcher matcher = RegexPatterns.serverSwapS.matcher(iInformation);
        if (matcher.find()) {
            Card newCard = Card.CardCreator(Integer.parseInt(matcher.group(1)));
            Card oldCard = Card.CardCreator(Integer.parseInt(matcher.group(2)));
            cHand.remove(oldCard);
            cHand.add(newCard);
            printer.printString(String.format("You received the %s from the warlord.  You lost the %s.",newCard.getStringRep(),oldCard.getStringRep()));
            updater.updateStatus(cTable);
            return errVal.NOERR;
        }
        System.err.println("Swap error");
        return errVal.NOMATCH;
    }

    private void printStrike(int strikeVal) {
        printer.printString(String.format("You received strike number %d because: %s",cStrikes,StrikeErrors.getErrorMessage(strikeVal)));
    }

    public void setPrinter(Printer newPrinter) {
        printer = newPrinter;
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
        if (debug) printer.printDebugMessage("Setting state to quit.");
        cState = ClientState.QUIT;
    }

    public ClientState getState() {
        return cState;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
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