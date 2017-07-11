package com.sentinella.james;

import java.io.PrintStream;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class BasicPrinter implements Printer {
    PrintStream debugStream = System.out;
    PrintStream outStream = System.out;
    PrintStream errStream = System.err;

    public void printString(String string) {
        outStream.println(string);
    }

    @Override
    public void printErrorMessage(String string) { errStream.println(string); }

    @Override
    public void printDebugMessage(String string) { debugStream.println(string); }

    @Override
    public void printLine() { outStream.println("----------------------------------------------"); }

    public void setDebugStream(PrintStream debugStream) {
        this.debugStream = debugStream;
    }

    @Override
    public void setErrorStream(PrintStream stream) {
        errStream = stream;
    }

    @Override
    public void setOutputStream(PrintStream stream) {
        outStream = stream;
    }
}
