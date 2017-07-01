package com.sentinella.james;

import java.util.regex.Pattern;

/**
 * Created by James on 4/6/2016.
 */
class RegexPatterns {
    // General messages
    static final Pattern generalMessage  = Pattern.compile("\\[(?<cmd>\\w{5})\\|{0,1}(?<msg>[^\\]]*)\\]");
    static final Pattern oneMessage      = Pattern.compile("(?<msg>^[^\\]]*\\])(?<leftover>.*)");
    static final Pattern input           = Pattern.compile("(\\w{1,6})(.*)");
    static final Pattern inputCardMatch  = Pattern.compile("([\\dAJKQajkq]{1,2})([cCdDsShH]{0,1})");


    // server messages to client.
    static final Pattern serverTable     = Pattern.compile("([apwdeAPWDE]\\d):([A-Za-z0-9_ ]{8}):(\\d{2}),([apwdeAPWDE]\\d):([A-Za-z0-9_ ]{8}):(\\d{2}),([apwdeAPWDE]\\d):([A-Za-z0-9_ ]{8}):(\\d{2}),([apwdeAPWDE]\\d):([A-Za-z0-9_ ]{8}):(\\d{2}),([apwdeAPWDE]\\d):([A-Za-z0-9_ ]{8}):(\\d{2}),([apwdeAPWDE]\\d):([A-Za-z0-9_ ]{8}):(\\d{2}),([apwdeAPWDE]\\d):([A-Za-z0-9_ ]{8}):(\\d{2})\\|(\\d{2}),(\\d{2}),(\\d{2}),(\\d{2})\\|(\\d)");
    /*  The regex above is a beast.  Let's explain what's going on. We're separating each player first and foremost.
     *  Each player has three attributes. It goes status+strikes:name:number of cards.
     *  This is followed by the last play, and if it is the starting round.
     */

    static final Pattern serverHand      = Pattern.compile("(\\d{2})");
    static final Pattern serverSwapS     = Pattern.compile("(\\d{2})\\|(\\d{2})"); // First card is card received, second is card lost.
    static final Pattern serverLobby     = Pattern.compile("([A-Za-z0-9_ ]{8})");
    static final Pattern serverChat      = Pattern.compile("([A-Za-z0-9_ ]{8})\\|(.{0,63})");
    static final Pattern serverStrike    = Pattern.compile("(\\d{2})\\|(\\d{1})");

    // Client messages to server.
    static final Pattern clientPlay      = Pattern.compile("(?<c1>\\d{2}),(?<c2>\\d{2}),(?<c3>\\d{2}),(?<c4>\\d{2})");
}
