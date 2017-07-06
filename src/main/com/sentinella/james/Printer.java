package com.sentinella.james;

import java.io.PrintStream;

public interface Printer {
    public void printString(String string);
    public void printErrorMessage(String string);
    public void printDebugMessage(String string);
    public void printLine();
    public void setDebugStream(PrintStream stream);
    public void setErrorStream(PrintStream stream);
    public void setOutputStream(PrintStream stream);
}
