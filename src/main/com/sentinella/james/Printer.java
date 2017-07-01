package com.sentinella.james;

public interface Printer {
    public void printString(String string);
    public void printErrorMessage(String string);
    public void printDebugMessage(String string);
    public void printLine();
}
