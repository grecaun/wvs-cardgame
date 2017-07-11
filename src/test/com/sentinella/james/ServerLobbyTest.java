package com.sentinella.james;

import org.junit.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ServerLobbyTest {
    ServerLobby  thisLobby;
    String player1  = "Danny   ";
    String player2  = "JonSnow ";
    String player3  = "Haggard ";
    String player4  = "James   ";
    String player5  = "Ultron  ";
    String player6  = "IronMan ";
    String player7  = "Rhaegar ";
    String player8  = "Snow    ";
    String player9  = "Nikola  ";
    String player10 = "Leonidas";

    @BeforeClass
    public static void oneTimeSetup() {
        System.out.println("Running ServerLobby tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with ServerLobby tests.");
    }

    @Before
    public void setUp() throws Exception {
        this.thisLobby = new ServerLobby();
    }

    @Test
    public void broadcastMessage() throws Exception {
        String message1 = "[stabl|e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00|52,52,52,52|0]";
        String message2 = "[slobb|01|Hodor   ]";
        String message3 = "[slobb|04|James   ,Hodor   ,JonSnow ]";
        String message4 = "[slobb|00]";
        String message5 = "[schat|James   |Hi!]";
        String message6 = "[WRONG]";
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        TestServerPlayer p1 = new TestServerPlayer(player1,p1Con,null);
        TestServerPlayer p2 = new TestServerPlayer(player2,p2Con,null);
        TestServerPlayer p3 = new TestServerPlayer(player3,p3Con,null);
        TestServerPlayer p4 = new TestServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p4);
        thisLobby.getNextPlayer();
        thisLobby.getNextPlayer();
        thisLobby.broadcastMessage(message1);
        thisLobby.broadcastMessage(message4);
        thisLobby.broadcastMessage(message6);
        thisLobby.broadcastMessage(message3);
        thisLobby.broadcastMessage(message2);
        thisLobby.broadcastMessage(message5);
        assertEquals(message1,p1.getLastMessage());
        assertEquals(message1,p2.getLastMessage());
        assertEquals(message1,p3.getLastMessage());
        assertEquals(message1,p4.getLastMessage());
        assertEquals(message4,p1.getLastMessage());
        assertEquals(message4,p2.getLastMessage());
        assertEquals(message4,p3.getLastMessage());
        assertEquals(message4,p4.getLastMessage());
        assertEquals(message6,p1.getLastMessage());
        assertEquals(message6,p2.getLastMessage());
        assertEquals(message6,p3.getLastMessage());
        assertEquals(message6,p4.getLastMessage());
        assertEquals(message3,p1.getLastMessage());
        assertEquals(message3,p2.getLastMessage());
        assertEquals(message3,p3.getLastMessage());
        assertEquals(message3,p4.getLastMessage());
        assertEquals(message2,p1.getLastMessage());
        assertEquals(message2,p2.getLastMessage());
        assertEquals(message2,p3.getLastMessage());
        assertEquals(message2,p4.getLastMessage());
        assertEquals(message5,p1.getLastMessage());
        assertEquals(message5,p2.getLastMessage());
        assertEquals(message5,p3.getLastMessage());
        assertEquals(message5,p4.getLastMessage());
    }

    @Test
    public void getPlayer() throws Exception {
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        ServerPlayer p1 = new ServerPlayer(player1,p1Con,null);
        ServerPlayer p2 = new ServerPlayer(player2,p2Con,null);
        ServerPlayer p3 = new ServerPlayer(player3,p3Con,null);
        ServerPlayer p4 = new ServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p4);
        assertTrue(thisLobby.getPlayer(p1Con).isPlayer(player1));
        assertFalse(thisLobby.getPlayer(p1Con).isPlayer(player2));
        assertFalse(thisLobby.getPlayer(p1Con).isPlayer(player3));
        assertFalse(thisLobby.getPlayer(p1Con).isPlayer(player4));
        assertTrue(thisLobby.getPlayer(p2.getCon()).isPlayer(player2));
        assertFalse(thisLobby.getPlayer(p2.getCon()).isPlayer(player1));
        assertFalse(thisLobby.getPlayer(p2.getCon()).isPlayer(player3));
        assertFalse(thisLobby.getPlayer(p2.getCon()).isPlayer(player4));
        assertTrue(thisLobby.getPlayer(p3Con).isPlayer(player3));
        assertFalse(thisLobby.getPlayer(p3Con).isPlayer(player1));
        assertFalse(thisLobby.getPlayer(p3Con).isPlayer(player2));
        assertFalse(thisLobby.getPlayer(p3Con).isPlayer(player4));
        assertTrue(thisLobby.getPlayer(p4.getCon()).isPlayer(player4));
        assertFalse(thisLobby.getPlayer(p4.getCon()).isPlayer(player1));
        assertFalse(thisLobby.getPlayer(p4.getCon()).isPlayer(player2));
        assertFalse(thisLobby.getPlayer(p4.getCon()).isPlayer(player3));
        thisLobby.removePlayer(p2Con);
        assertTrue(thisLobby.getPlayer(p2Con) == null);
        assertTrue(thisLobby.getPlayer(null) == null);
    }

    @Test
    public void sendEmptyTable() throws Exception {
        String emptyTable = "[stabl|e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00|52,52,52,52|0]";
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        TestServerPlayer p1 = new TestServerPlayer(player1,p1Con,null);
        TestServerPlayer p2 = new TestServerPlayer(player2,p2Con,null);
        TestServerPlayer p3 = new TestServerPlayer(player3,p3Con,null);
        TestServerPlayer p4 = new TestServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p4);
        thisLobby.getNextPlayer();
        thisLobby.getNextPlayer();
        thisLobby.sendEmptyTable();
        assertEquals(emptyTable,p1.getLastMessage());
        assertEquals(emptyTable,p2.getLastMessage());
        assertEquals(emptyTable,p3.getLastMessage());
        assertEquals(emptyTable,p4.getLastMessage());
    }

    @Test
    public void getNextPlayer() throws Exception {
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        ServerPlayer p1 = new ServerPlayer(player1,p1Con,null);
        ServerPlayer p2 = new ServerPlayer(player2,p2Con,null);
        ServerPlayer p3 = new ServerPlayer(player3,p3Con,null);
        ServerPlayer p4 = new ServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p4);
        assertEquals(p1,thisLobby.getNextPlayer());
        assertEquals(p3,thisLobby.getNextPlayer());
        assertEquals(p2,thisLobby.getNextPlayer());
        assertEquals(1,thisLobby.numInLobby());
        thisLobby.addPlayer(p1);
        assertEquals(1,thisLobby.numInLobby());
        assertEquals(p4,thisLobby.getNextPlayer());
        assertEquals(null,thisLobby.getNextPlayer());
        assertEquals(0,thisLobby.numInLobby());
    }

    @Test
    public void addToLobby() throws Exception {
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        ServerPlayer p1 = new ServerPlayer(player1,p1Con,null);
        ServerPlayer p2 = new ServerPlayer(player2,p2Con,null);
        ServerPlayer p3 = new ServerPlayer(player3,p3Con,null);
        ServerPlayer p4 = new ServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p4);
        assertEquals(4,thisLobby.numInLobby());
        thisLobby.getNextPlayer();
        thisLobby.getNextPlayer();
        assertEquals(2,thisLobby.numInLobby());
        thisLobby.getNextPlayer();
        thisLobby.getNextPlayer();
        assertEquals(0,thisLobby.numInLobby());
        thisLobby.addToLobby(p2);
        thisLobby.addToLobby(p2);
        assertEquals(1,thisLobby.numInLobby());
        thisLobby.addToLobby(p4);
        thisLobby.addToLobby(p1);
        thisLobby.addToLobby(p3);
        assertEquals(4,thisLobby.numInLobby());
    }

    @Test
    public void addRemovePlayer() throws Exception {
        assertEquals(0, thisLobby.numInLobby());
        thisLobby.addPlayer(player1);
        thisLobby.addPlayer(player2);
        thisLobby.addPlayer(player3);
        thisLobby.addPlayer(player4);
        thisLobby.addPlayer(player5);
        thisLobby.addPlayer(player5);
        thisLobby.addPlayer(new Player(player6));
        thisLobby.addPlayer(new Player(player7));
        thisLobby.addPlayer(new Player(player8));
        thisLobby.addPlayer(new Player(player8));
        thisLobby.addPlayer(new Player(player9));
        thisLobby.addPlayer(new Player(player10));
        assertEquals(10, thisLobby.numInLobby());
        thisLobby.removePlayer(player6);
        thisLobby.removePlayer(player8);
        thisLobby.removePlayer(new Player(player1));
        thisLobby.removePlayer(player6);
        assertEquals(7, thisLobby.numInLobby());
        thisLobby.addPlayer(player2);
        assertEquals(7, thisLobby.numInLobby());
        thisLobby.removePlayer(player2);
        thisLobby.removePlayer(player3);
        thisLobby.removePlayer(player4);
        thisLobby.removePlayer(player5);
        thisLobby.removePlayer(player7);
        thisLobby.removePlayer(player9);
        thisLobby.removePlayer(player10);
        assertEquals(0, thisLobby.numInLobby());
    }

    @Test
    public void removePlayer() throws Exception {
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        ServerPlayer p1 = new ServerPlayer(player1,p1Con,null);
        ServerPlayer p2 = new ServerPlayer(player2,p2Con,null);
        ServerPlayer p3 = new ServerPlayer(player3,p3Con,null);
        ServerPlayer p4 = new ServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p4);
        assertEquals(4,thisLobby.numInLobby());
        thisLobby.removePlayer(p1.getCon());
        thisLobby.removePlayer(p1Con);
        assertEquals(3,thisLobby.numInLobby());
        thisLobby.removePlayer(p2.getCon());
        thisLobby.removePlayer(p3Con);
        thisLobby.removePlayer(p4Con);
        assertEquals(0,thisLobby.numInLobby());
    }

    @Test
    public void playerExists() throws Exception {
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        ServerPlayer p1 = new ServerPlayer(player1,p1Con,null);
        ServerPlayer p2 = new ServerPlayer(player2,p2Con,null);
        ServerPlayer p3 = new ServerPlayer(player3,p3Con,null);
        ServerPlayer p4 = new ServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p4);
        assertTrue(thisLobby.playerExists(player1));
        assertTrue(thisLobby.playerExists(player2));
        assertTrue(thisLobby.playerExists(player3));
        assertTrue(thisLobby.playerExists(player4));
        assertFalse(thisLobby.playerExists(player10));
        assertFalse(thisLobby.playerExists(player9));
        assertFalse(thisLobby.playerExists(player8));
        thisLobby.removePlayer(player2);
        assertFalse(thisLobby.playerExists(player2));
    }

    @Test
    public void getLobbyMessage() throws Exception {
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        ServerPlayer p1 = new ServerPlayer(player1,p1Con,null);
        ServerPlayer p2 = new ServerPlayer(player2,p2Con,null);
        ServerPlayer p3 = new ServerPlayer(player3,p3Con,null);
        ServerPlayer p4 = new ServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p4);
        assertEquals("[slobb|04|Danny   ,JonSnow ,Haggard ,James   ]",thisLobby.getLobbyMessage());
        thisLobby.removePlayer("James");
        assertEquals("[slobb|03|Danny   ,JonSnow ,Haggard ]",thisLobby.getLobbyMessage());
        thisLobby.removePlayer(p2);
        assertEquals("[slobb|02|Danny   ,Haggard ]",thisLobby.getLobbyMessage());
        thisLobby.getNextPlayer();
        assertEquals("[slobb|01|Haggard ]",thisLobby.getLobbyMessage());
    }

    @Test
    public void isInLobby() throws Exception {
        SocketChannel p1Con = SocketChannel.open();
        SocketChannel p2Con = SocketChannel.open();
        SocketChannel p3Con = SocketChannel.open();
        SocketChannel p4Con = SocketChannel.open();
        ServerPlayer p1 = new ServerPlayer(player1,p1Con,null);
        ServerPlayer p2 = new ServerPlayer(player2,p2Con,null);
        ServerPlayer p3 = new ServerPlayer(player3,p3Con,null);
        ServerPlayer p4 = new ServerPlayer(player4,p4Con,null);
        thisLobby.addPlayer(p1);
        thisLobby.addPlayer(p2);
        thisLobby.addPlayer(p3);
        thisLobby.addPlayer(p4);
        assertTrue(thisLobby.isInLobby(p1));
        assertTrue(thisLobby.isInLobby(p2));
        assertTrue(thisLobby.isInLobby(p3));
        assertTrue(thisLobby.isInLobby(p4));
        thisLobby.removePlayer(p1);
        thisLobby.getNextPlayer();
        thisLobby.removePlayer(p4);
        assertFalse(thisLobby.isInLobby(p1));
        assertFalse(thisLobby.isInLobby(p2));
        assertTrue(thisLobby.isInLobby(p3));
        assertFalse(thisLobby.isInLobby(p4));
    }

    private class TestServerPlayer extends ServerPlayer {
        ArrayList<String> isWritten;

        public TestServerPlayer(String iName, SocketChannel iCon, SelectionKey iKey) {
            super(iName, iCon, iKey);
            isWritten = new ArrayList<>();
        }

        @Override
        public CONERROR sendMessage(String string) {
            isWritten.add(string);
            return CONERROR.NOERROR;
        }

        public String getLastMessage() {
            return isWritten.remove(0);
        }
    }
}