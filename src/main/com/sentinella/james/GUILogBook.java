package com.sentinella.james;

/**
 * Copyright (c) 2017 James Sentinella.
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class GUILogBook extends LogBook {
    private LogBookCallback callback = msg -> { };

    public GUILogBook(int dL, boolean dC, String dS) {
        super(dL,dC,dS);
    }

    public GUILogBook() {}

    public GUILogBook(LogBook l, String dStr) {
        super(l,dStr);
    }

    private void printMsg(String msg, LogStream stream) {
        switch (stream) {
            case DEB:
                break;
            default:
                callback.addToMessages(msg);
        }
    }

    @Override
    public void printOutLine() { printMsg("----------------------------------------------",LogStream.OUT);}

    @Override
    public void printOutMsg(String msg) {
        printMsg(msg, LogStream.OUT);
    }

    @Override
    public void printErrMsg(String msg) {
        printMsg(msg, LogStream.ERR);
    }

    @Override
    public void printDebMsg(String msg, int level) {
        if (level <= debugLvl) printMsg(msg, LogStream.DEB);
    }

    public void setCallback(LogBookCallback cb) {
        this.callback = cb;
    }
}
