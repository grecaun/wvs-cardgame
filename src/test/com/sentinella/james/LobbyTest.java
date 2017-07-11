package com.sentinella.james;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class LobbyTest {
    Lobby  thisLobby;
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
        System.out.println("Running Lobby tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with Lobby tests.");
    }

    @Before
    public void setUp() throws Exception {
        thisLobby = new Lobby();
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
    public void hasPlayer() throws Exception {
        thisLobby.addPlayer(player3);
        thisLobby.addPlayer(player4);
        thisLobby.addPlayer(player5);
        thisLobby.addPlayer(new Player(player6));
        thisLobby.addPlayer(new Player(player7));
        thisLobby.addPlayer(new Player(player8));
        assertTrue(thisLobby.hasPlayer(player3));
        assertTrue(thisLobby.hasPlayer(player4));
        assertTrue(thisLobby.hasPlayer(player5));
        assertTrue(thisLobby.hasPlayer(player6));
        assertTrue(thisLobby.hasPlayer(player7));
        assertTrue(thisLobby.hasPlayer(player8));
        assertFalse(thisLobby.hasPlayer(player1));
        assertFalse(thisLobby.hasPlayer(player2));
        assertFalse(thisLobby.hasPlayer(player9));
        assertFalse(thisLobby.hasPlayer(player10));
    }

    @Test
    public void clear() throws Exception {
        assertEquals(0, thisLobby.numInLobby());
        thisLobby.addPlayer(player1);
        thisLobby.addPlayer(player2);
        thisLobby.addPlayer(player3);
        thisLobby.addPlayer(player4);
        thisLobby.addPlayer(player5);
        thisLobby.addPlayer(new Player(player6));
        thisLobby.addPlayer(new Player(player7));
        thisLobby.addPlayer(new Player(player8));
        thisLobby.addPlayer(new Player(player9));
        thisLobby.addPlayer(new Player(player10));
        assertEquals(10, thisLobby.numInLobby());
        thisLobby.clear();
        assertEquals(0, thisLobby.numInLobby());
    }

    @Test
    public void getLobbyString() throws Exception {
        thisLobby.addPlayer(player1);
        thisLobby.addPlayer(player2);
        thisLobby.addPlayer(player3);
        assertEquals("Players in lobby: Danny JonSnow Haggard",thisLobby.getLobbyString());
        thisLobby.addPlayer(player4);
        thisLobby.addPlayer(player5);
        thisLobby.addPlayer(new Player(player6));
        thisLobby.addPlayer(new Player(player7));
        assertEquals("Players in lobby: Danny JonSnow Haggard James Ultron IronMan Rhaegar",thisLobby.getLobbyString());
        thisLobby.addPlayer(new Player(player8));
        thisLobby.addPlayer(new Player(player9));
        thisLobby.addPlayer(new Player(player10));
        assertEquals("Players in lobby: Danny JonSnow Haggard James Ultron IronMan Rhaegar Snow Nikola Leonidas",thisLobby.getLobbyString());
    }

}