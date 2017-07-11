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
public class PlayerTest {
    Player thisPlayer;

    @BeforeClass
    public static void oneTimeSetup() {
        System.out.println("Running Player tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with Player tests.");
    }

    @Before
    public void setUp() throws Exception {
        thisPlayer = new Player("James");
    }

    @Test
    public void updatePlayer() throws Exception {
        assertEquals(0, thisPlayer.getNumCards());
        assertEquals(0, thisPlayer.getNumStrikes());
        assertTrue(thisPlayer.isPlayer(new Player("James    ")));
        assertTrue(thisPlayer.isPlayer("James"));
        assertEquals(pStatus.LOBBY, thisPlayer.getStatus());
        thisPlayer.updatePlayer("JonSnow   ", pStatus.DISCONNECTED, 3, 24);
        assertEquals(24, thisPlayer.getNumCards());
        assertEquals(3, thisPlayer.getNumStrikes());
        assertTrue(thisPlayer.isPlayer(new Player("JonSnow")));
        assertFalse(thisPlayer.isPlayer("James   "));
        assertEquals(pStatus.DISCONNECTED, thisPlayer.getStatus());
    }

    @Test
    public void addRemoveHasCard() throws Exception {
        thisPlayer.addCard(24);
        thisPlayer.addCard(Card.CardCreator(13));
        thisPlayer.addCard(Card.CardCreator(56));
        thisPlayer.addCard(null);
        thisPlayer.addCard(51);
        thisPlayer.addCard(26);
        thisPlayer.addCard(0);
        thisPlayer.addCard(4);
        assertEquals(6, thisPlayer.getNumCards());
        thisPlayer.removeCard(Card.CardCreator(15));
        thisPlayer.removeCard(Card.CardCreator(0));
        thisPlayer.removeCard(24);
        assertEquals(4, thisPlayer.getNumCards());
        assertTrue(thisPlayer.hasCard(Card.CardCreator(26)));
        assertTrue(thisPlayer.hasCard(51));
        assertTrue(thisPlayer.hasCard(4));
        assertTrue(thisPlayer.hasCard(13));
        assertFalse(thisPlayer.hasCard(0));
        assertFalse(thisPlayer.hasCard(24));
        assertFalse(thisPlayer.hasCard(45));
    }

    @Test
    public void setNumCards() throws Exception {
        thisPlayer.setNumCards(56);
        assertEquals(26, thisPlayer.getNumCards());
        thisPlayer.setNumCards(12);
        assertEquals(12, thisPlayer.getNumCards());
        thisPlayer.setNumCards(15);
        assertEquals(15, thisPlayer.getNumCards());
        thisPlayer.setNumCards(78);
        assertEquals(26, thisPlayer.getNumCards());
        thisPlayer.setNumCards(-15);
        assertEquals(0, thisPlayer.getNumCards());
        thisPlayer.setNumCards(0);
        assertEquals(0, thisPlayer.getNumCards());
    }

    @Test
    public void getStatus() throws Exception {
        thisPlayer.setStatus(pStatus.ACTIVE);
        assertEquals(pStatus.ACTIVE, thisPlayer.getStatus());
        thisPlayer.setStatus(pStatus.DISCONNECTED);
        assertEquals(pStatus.DISCONNECTED, thisPlayer.getStatus());
        thisPlayer.setStatus(pStatus.LOBBY);
        assertEquals(pStatus.LOBBY, thisPlayer.getStatus());
        thisPlayer.setStatus(pStatus.EMPTY);
        assertEquals(pStatus.EMPTY, thisPlayer.getStatus());
        thisPlayer.setStatus(pStatus.PASSED);
        assertEquals(pStatus.PASSED, thisPlayer.getStatus());
        thisPlayer.setStatus(pStatus.WAITING);
        assertEquals(pStatus.WAITING, thisPlayer.getStatus());
    }

    @Test
    public void getName() throws Exception {
        assertEquals("James", thisPlayer.getName());
    }

}