package com.sentinella.james;

import java.io.PrintStream;

/**
 * Created by James on 4/7/2016.
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
