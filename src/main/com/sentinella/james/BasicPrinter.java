package com.sentinella.james;

/**
 * Created by James on 4/7/2016.
 */
public class BasicPrinter implements Printer {
    public void printString(String string) {
        System.out.println(string);
    }

    @Override
    public void printErrorMessage(String string) { System.err.println(string); }

    @Override
    public void printDebugMessage(String string) { System.out.println(string); }

    @Override
    public void printLine() { System.out.println("----------------------------------------------"); }
}
