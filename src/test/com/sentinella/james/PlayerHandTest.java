package com.sentinella.james;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class PlayerHandTest {
    PlayerHand theHand;

    @BeforeClass
    public static void oneTimeSetup() {
        System.out.println("Running PlayerHand tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with PlayerHand tests.");
    }

    @Before
    public void setUp() throws Exception {
        theHand = new PlayerHand();
    }

    @Test
    public void addRemoveClear() throws Exception {
        theHand.add(Card.CardCreator(0));
        theHand.add(Card.CardCreator(5));
        assertEquals(2, theHand.count());

        theHand.add(Card.CardCreator(52));
        theHand.add(Card.CardCreator(-1));
        assertEquals(2, theHand.count());

        theHand.add(0);
        theHand.add(52);
        assertEquals(2, theHand.count());

        theHand.add(-1);
        theHand.add(12);
        theHand.add(13);
        theHand.add(34);
        assertEquals(5, theHand.count());

        theHand.remove(Card.CardCreator(0));
        theHand.remove(Card.CardCreator(10));
        theHand.remove(Card.CardCreator(-1));
        assertEquals(4, theHand.count());

        theHand.remove(12);
        theHand.remove(13);
        theHand.remove(13);
        assertEquals(2, theHand.count());

        theHand.clear();
        assertEquals(0, theHand.count());
        assertFalse(theHand.hasCard(5));
    }

    @Test
    public void getLowest() throws Exception {
        theHand.add(11);
        theHand.add(23);
        theHand.add(5);
        theHand.add(26);
        theHand.add(28);
        theHand.add(34);
        theHand.add(45);
        theHand.add(47);
        theHand.add(10);
        theHand.add(8);
        theHand.add(16);

        Card lowest = theHand.getLowest();
        assertTrue(lowest.isSameCard(5));
        assertFalse(theHand.hasCard(5));

        assertTrue(theHand.hasCard(8));
        lowest = theHand.getLowest();
        assertTrue(lowest.isSameCard(8));

        List<Card> lowestCards = theHand.getLowest(0,2);
        assertEquals(2, lowestCards.get(0).getCardNumericFaceValue());
        assertEquals(2, lowestCards.size());

        theHand.add(4);
        theHand.add(5);
        theHand.add(6);
        theHand.add(7);
        lowestCards = theHand.getLowest(0,2);
        assertEquals(1, lowestCards.get(0).getCardNumericFaceValue());
        assertEquals(4, lowestCards.size());

        lowestCards = theHand.getLowest(6,1);
        assertEquals(6, lowestCards.get(0).getCardNumericFaceValue());
        assertEquals(1, lowestCards.size());

        theHand.clear();
        lowestCards = theHand.getLowest(0,1);
        lowest      = theHand.getLowest();
        assertEquals(0, lowestCards.size());
        assertEquals(null, lowest);
    }

    @Test
    public void hasCard() throws Exception {
        theHand.add(0);
        theHand.add(12);
        theHand.add(15);
        theHand.add(32);
        theHand.add(45);

        assertFalse(theHand.hasCard(13));
        assertFalse(theHand.hasCard(1));
        assertFalse(theHand.hasCard(44));
        assertFalse(theHand.hasCard(36));
        assertTrue(theHand.hasCard(0));
        assertTrue(theHand.hasCard(12));
        assertTrue(theHand.hasCard(15));
        assertTrue(theHand.hasCard(32));
        assertTrue(theHand.hasCard(45));
    }

    @Test
    public void count() throws Exception {
        theHand.add(0);
        theHand.add(0);
        theHand.add(10);
        assertEquals(2, theHand.count());

        theHand.add(17);
        theHand.add(22);
        theHand.add(27);
        theHand.add(30);
        theHand.add(43);
        assertEquals(7, theHand.count());

        theHand.remove(18);
        theHand.remove(0);
        theHand.remove(10);
        theHand.remove(10);
        theHand.remove(17);
        assertEquals(4, theHand.count());

        theHand.clear();
        assertEquals(0, theHand.count());
        theHand.add(0);
        assertEquals(1, theHand.count());
        theHand.remove(0);
        assertEquals(0, theHand.count());
    }
}