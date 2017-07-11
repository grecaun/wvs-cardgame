package com.sentinella.james;

import java.io.PrintStream;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public interface Printer {
    public void printString(String string);
    public void printErrorMessage(String string);
    public void printDebugMessage(String string);
    public void printLine();
    public void setDebugStream(PrintStream stream);
    public void setErrorStream(PrintStream stream);
    public void setOutputStream(PrintStream stream);
}
