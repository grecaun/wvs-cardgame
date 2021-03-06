package com.sentinella.james;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class MainWorker {
    private ClientConnection outConnection;
    private PlayerHand       hand;

    protected LogBook log = new LogBook();

    /**
     * PrintWriter connection sends messages to server
     * with regards to WvZ protocol.
     *
     * @param newConnection
     */
    public MainWorker(ClientConnection newConnection) {
        outConnection = newConnection;
        hand          = new PlayerHand();
    }

    public MainWorker(ClientConnection newCon, LogBook l, String debugStr) {
        this(newCon);
        log = LogBookFactory.getLogBook(l,debugStr);
    }

    public void setOutConnection(ClientSocket out) {
        outConnection = out;
    }

    /**
     * Sends a play message to the server.
     * @param msg List of cards. No spaces required.
     */
    public void sendPlay(String msg) {
        log.printDebMsg("sendPlay (TUI VERSION) - START", 3);
        ArrayList<Integer> outCardNumbers = new ArrayList<>();
        Matcher cardMatcher               = RegexPatterns.inputCardMatch.matcher(msg);
        int cardValue, cardSuit;
        while (cardMatcher.find()) {        // find all matches
            cardValue = 0;                  // base card value of 0
            switch (cardMatcher.group(1)) { // group 1 is value
                case "2":
                    cardValue = 12;
                    break;
                case "A":
                case "a":
                    cardValue = 11;
                    break;
                case "K":
                case "k":
                    cardValue = 10;
                    break;
                case "Q":
                case "q":
                    cardValue = 9;
                    break;
                case "J":
                case "j":
                    cardValue = 8;
                    break;
                case "10":                  // fall through makes 10 into 7, etc
                    cardValue++;
                case "9":                   // ensures that we get proper values
                    cardValue++;
                case "8":
                    cardValue++;
                case "7":
                    cardValue++;
                case "6":
                    cardValue++;
                case "5":
                    cardValue++;
                case "4":
                    cardValue++;
                case "3":
                    break;
                default:                    // unknown input, sp -1
                    cardValue = -1;
            }
            switch (cardMatcher.group(2)) { // group 2 is suit
                case "c":                   // clubs
                case "C":
                    cardSuit = 0;
                    break;
                case "d":                   // diamonds
                case "D":
                    cardSuit = 1;
                    break;
                case "h":                   // hearts
                case "H":
                    cardSuit = 2;
                    break;
                case "s":                   // spades
                case "S":
                    cardSuit = 3;
                    break;
                default:                    // unknown
                    cardSuit = -1;
            }                               // if suit or value were found, add appropriate number
            if (cardValue != -1 && cardSuit != -1) outCardNumbers.add(cardValue * 4 + cardSuit);
        }
        StringBuilder outMessage = new StringBuilder();
        outMessage.append("[cplay|");       // build string, max of 4 cards can be played
        int endIndex = outCardNumbers.size() > 4 ? 4 : outCardNumbers.size(), numPlayed = 0;
        for (int i=0; i<endIndex; i++) {
            outMessage.append(String.format("%02d", outCardNumbers.get(i)));
            hand.remove(outCardNumbers.get(i));
            if (i < 3) outMessage.append(","); // put commas after every card but last
            numPlayed++;
        }                                   // pad the play to 4 cards with 52 (no card)
        for (int j=numPlayed; j<4; j++) {
            outMessage.append("52");
            if (j < 3) outMessage.append(",");
            numPlayed++;
        }
        outMessage.append("]");
        if (outConnection!= null) {
            try {
                log.printDebConMsg(outMessage.toString());
                outConnection.println(outMessage.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.printDebMsg("sendPlay - END", 3);
    }

    /**
     * Sends swap message to server.
     * @param msg Card value given. Ignores multiples. If invalid card given first, uses it.
     */
    public void sendSwap(String msg) {
        log.printDebMsg("sendSwap (TUI version) - START", 3);
        Matcher cardMatcher               = RegexPatterns.inputCardMatch.matcher(msg);
        int cardValue = -1, cardSuit = -1, cardNumber = 52;
        if (cardMatcher.find()) {
            cardValue = 0;                  // set to base value (3)
            switch (cardMatcher.group(1)) { // group is value
                case "2":
                    cardValue = 12;
                    break;
                case "A":
                case "a":
                    cardValue = 11;
                    break;
                case "K":
                case "k":
                    cardValue = 10;
                    break;
                case "Q":
                case "q":
                    cardValue = 9;
                    break;
                case "J":
                case "j":
                    cardValue = 8;
                    break;
                case "10":                  // fallthrough means 10 becomes 7
                    cardValue++;
                case "9":
                    cardValue++;
                case "8":
                    cardValue++;
                case "7":
                    cardValue++;
                case "6":
                    cardValue++;
                case "5":
                    cardValue++;
                case "4":
                    cardValue++;
                case "3":
                    break;
                default:                    // unknown value
                    cardValue = -1;
            }
            switch (cardMatcher.group(2)) { // group 2 is suit
                case "c":                   // clubs
                case "C":
                    cardSuit = 0;
                    break;
                case "d":                   // diamonds
                case "D":
                    cardSuit = 1;
                    break;
                case "h":                   // hearts
                case "H":
                    cardSuit = 2;
                    break;
                case "s":                   // spades
                case "S":
                    cardSuit = 3;
                    break;
                default:                    // unknown
                    cardSuit = -1;
            }
        }                                   // transmits 52 if no number found in msg
        cardNumber = cardValue == -1 && cardSuit == -1 ? 52 : cardValue * 4 + cardSuit;
        hand.remove(cardNumber);
        if (outConnection!= null) {
            try {
                log.printDebConMsg(String.format("[cswap|%02d]", cardNumber));
                outConnection.println(String.format("[cswap|%02d]", cardNumber));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.printDebMsg("sendSwap (TUI version) - END", 3);
    }

    /**
     * Sends chat message(s) to server.
     * Breaks into 64 character chunks. Trims whitespace off ends.
     * @param msg Message to be transmitted to server.
     */
    public void sendChat(String msg) {
        log.printDebMsg("sendChat - START", 3);
        ArrayList<String> outMessages = new ArrayList<>();      // array for strings to send
        String trimmed = msg.trim();                            // start by trimming incoming message
        String spacePad = "                                                                      ";
        if (trimmed.length() > 63) {                            // max message length of 64
            String[] subStrings = trimmed.split("\\s+");  // split on whitespace
            int numSubStrings = subStrings.length;              // get number of messages
            StringBuilder outBuilder = new StringBuilder(100);
            String workInProgress;
            int curLength;
            for (int index=0;index<numSubStrings;index++) {     // all substrings
                workInProgress = subStrings[index];             // current substring
                                                                // get the length plus partial string
                curLength = workInProgress.length() + (outBuilder.length() != 0 ? outBuilder.length()+1 : 0);
                if (curLength > 62) {                           // if too long to send
                    if (outBuilder.length() != 0) {             // and it was because of the pre-built msg
                        outBuilder.append(spacePad.substring(0, 63-outBuilder.length()));
                        outMessages.add(outBuilder.toString()); // add space padded message to outbound msgs
                        outBuilder.setLength(0);                // reset the StringBuilder
                    }
                    if (workInProgress.length() < 62) {         // check to see if our current string is not too big
                        outBuilder.append(workInProgress);      // append to builder if not
                                                                // note: if WIP < 63, then WIP + OB > 64, thus OB was cleared
                    } else {                                    // else it's too large to send
                        while (workInProgress.length() >= 62) { // then split it up as often as necessary
                            outMessages.add(workInProgress.substring(0, 63));
                            workInProgress = workInProgress.substring(63);
                        }                                       // now add whatever is left
                        outBuilder.append(workInProgress);
                    }
                } else {                                        // the combo wasn't too big
                    if (outBuilder.length() !=0) outBuilder.append(" ");
                    outBuilder.append(workInProgress);          // only add space if something was there
                }
            }                                                   // add padded leftovers to messages
            if (outBuilder.length() != 0) outMessages.add(outBuilder.toString() + spacePad.substring(0,63-outBuilder.length()));
        } else {                                                // the message fits just fine
            outMessages.add(trimmed + spacePad.substring(0, 63-trimmed.length()));
        }
        assert(outMessages.size() > 0);                         // guaranteed to have a single message
        for (String s: outMessages) {                           // print all messages
            assert(s.length() == 63);                           // messages length should match
            if (outConnection!= null) {
                try {
                    log.printDebConMsg(String.format("[cchat|%s]",s));
                    outConnection.println(String.format("[cchat|%s]",s));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.printDebMsg("sendChat - END", 3);
    }

    public void sendPlay(int[] cards) {
        log.printDebMsg("sendPlay (array of int) - START", 3);
        StringBuilder outMessage = new StringBuilder();
        outMessage.append("[cplay|");       // build string, max of 4 cards can be played
        int endIndex = cards.length > 4 ? 4 : cards.length, numPlayed = 0;
        for (int i=0; i<endIndex; i++) {
            outMessage.append(String.format("%02d", cards[i]));
            hand.remove(cards[i]);
            if (i < 3) outMessage.append(","); // put commas after every card but last
            numPlayed++;
        }                                   // pad the play to 4 cards with 52 (no card)
        for (int j=numPlayed; j<4; j++) {
            outMessage.append("52");
            if (j < 3) outMessage.append(",");
            numPlayed++;
        }
        outMessage.append("]");
        if (outConnection!= null) {
            try {
                log.printDebConMsg(outMessage.toString());
                outConnection.println(outMessage.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.printDebMsg("sendPlay (array of int) - END", 3);
    }

    public void sendSwap(int card) {
        log.printDebMsg("sendSwap (int) - START", 3);
        hand.remove(card);
        if (outConnection!= null) {
            try {
                log.printDebConMsg(String.format("[cswap|%02d]", card));
                outConnection.println(String.format("[cswap|%02d]", card));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.printDebMsg("sendSwap (int) - END", 3);
    }

    public void setHand(PlayerHand iHand) {
        log.printDebMsg("setHand - START", 3);
        this.hand = iHand;
        log.printDebMsg("setHand - END", 3);
    }
}
