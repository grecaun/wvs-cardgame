package com.sentinella.james;

import org.junit.*;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class MainWorkerTest {
    private MainWorker            worker;
    private MainWorkerPrintWriter printer;

    @BeforeClass
    public static void oneTimeSetup() {
        System.out.println("Running MainWorker tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with MainWorker tests.");
    }

    @Before
    public void setUp() throws Exception {
        printer = new MainWorkerPrintWriter();
        worker  = new MainWorker(printer, false);
    }

    @Test
    public void sendPlay() throws Exception {
        String msg1 = "Ah ac aS";
        String msg2 = "";
        String msg3 = "10h, 10S";
        String msg4 = "3c - 2D16h";
        String msg5 = "3c - 2D 5h6s7h";
        worker.sendPlay(msg1);
        assertEquals("[cplay|46,44,47,52]", printer.getLastMessage());
        worker.sendPlay(msg2);
        assertEquals("[cplay|52,52,52,52]", printer.getLastMessage());
        worker.sendPlay(msg3);
        assertEquals("[cplay|30,31,52,52]", printer.getLastMessage());
        worker.sendPlay(msg4);
        assertEquals("[cplay|00,49,52,52]", printer.getLastMessage());
        worker.sendPlay(msg5);
        assertEquals("[cplay|00,49,10,15]", printer.getLastMessage());
    }

    @Test
    public void sendSwap() throws Exception {
        String msg1 = "Ah";
        String msg2 = "";
        String msg3 = "10C";
        String msg4 = "3c";
        String msg5 = "js";
        worker.sendSwap(msg1);
        assertEquals("[cswap|46]", printer.getLastMessage());
        worker.sendSwap(msg2);
        assertEquals("[cswap|52]", printer.getLastMessage());
        worker.sendSwap(msg3);
        assertEquals("[cswap|28]", printer.getLastMessage());
        worker.sendSwap(msg4);
        assertEquals("[cswap|00]", printer.getLastMessage());
        worker.sendSwap(msg5);
        assertEquals("[cswap|35]", printer.getLastMessage());
    }

    @Test
    public void sendChat() throws Exception {
        String msg1 = "             hi there, I'm james    ";
        String msg2 = "what's up doc?";
        String msg3 = "this is actually going to be really fun... I can't wait to figure out how this is going to work.";
        String msg4 = "123456789012345678901234567890123456789012345678901234567890123412345678901234567890";
        String msg5 = "";
        worker.sendChat(msg1);
        assertEquals("[cchat|hi there, I'm james                                            ]",printer.getLastMessage());
        worker.sendChat(msg2);
        assertEquals("[cchat|what's up doc?                                                 ]",printer.getLastMessage());
        worker.sendChat(msg3);
        assertEquals("[cchat|figure out how this is going to work.                          ]",printer.getLastMessage());
        worker.sendChat(msg4);
        assertEquals("[cchat|412345678901234567890                                          ]",printer.getLastMessage());
        worker.sendChat(msg5);
        assertEquals("[cchat|                                                               ]",printer.getLastMessage());
    }

    private class MainWorkerPrintWriter implements ClientConnection {
        private String lastMessage;

        @Override
        public void println(String s) {
            lastMessage = s;
        }

        public String getLastMessage() {
            return lastMessage;
        }
    }

}