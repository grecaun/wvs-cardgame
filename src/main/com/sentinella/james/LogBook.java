package com.sentinella.james;

import java.io.PrintStream;

/**
 * Copyright (c) 2017 James Sentinella.
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class LogBook {
    protected static PrintStream outStream = System.out;
    protected static PrintStream errStream = System.err;
    protected static PrintStream debStream = System.out;

    protected int     debugLvl = 0;
    protected boolean debugCon   = false;
    protected String  debugStr   = "";

    public LogBook(LogBook old) {
        this.debugCon  = old.debugCon;
        this.debugLvl  = old.debugLvl;
    }

    public LogBook(LogBook old, String dStr) {
        this(old);
        this.debugStr = dStr;
    }

    public LogBook(int dL, boolean dC, String dS) {
        this.debugLvl = dL;
        this.debugCon = dC;
        this.debugStr = dS;
    }

    public LogBook() {}

    private void printMsg(String msg, LogStream s) {
        switch (s) {
            case OUT:
                outStream.println(msg);
                break;
            case DEB:
                debStream.println(String.format("%s: %s",debugStr,msg));
                break;
            case ERR:
                errStream.println(msg);
        }
    }

    public void printOutLine() { printMsg("----------------------------------------------",LogStream.OUT);}

    public void printOutMsg(String msg) {
        printMsg(msg, LogStream.OUT);
    }

    public void printErrMsg(String msg) {
        printMsg(msg, LogStream.ERR);
    }

    public void printDebMsg(String msg, int level) {
        if (level <= debugLvl) printMsg(msg, LogStream.DEB);
    }

    public void printDebConMsg(String msg) {
        if (debugCon) printDebMsg(msg, 0);
    }

    public void setOutStream(PrintStream s) {
        this.outStream = s;
    }

    public void setErrStream(PrintStream s) {
        this.errStream = s;
    }

    public void setDebStream(PrintStream s) {
        this.debStream = s;
    }

    public boolean debugCon() {
        return this.debugCon;
    }

    public void setDebugCon(boolean d) {
        this.debugCon = d;
    }

    public boolean debug() {
        return this.debugLvl > 0;
    }

    public void setDebugLvl(int d) {
        this.debugLvl = d;
    }

    public void setDebugString(String debugString) {
        this.debugStr = debugString;
    }

    public String getDebugStr() {
        return this.debugStr;
    }

    protected enum LogStream {OUT, ERR, DEB}
}
