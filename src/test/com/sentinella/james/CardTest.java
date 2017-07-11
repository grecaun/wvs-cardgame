package com.sentinella.james;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class CardTest {
    Card card1;
    Card card2;
    Card card3;
    Card card4;
    Card card5;
    Card card6;
    Card card7;
    Card card8;
    Card card9;
    Card card10;

    @BeforeClass
    public static void oneTimeSetup() {
        System.out.println("Running Card tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with Card tests.");
    }

    @Before
    public void setUp() throws Exception {
        card1  = Card.CardCreator(0);
        card2  = Card.CardCreator(52);
        card3  = Card.CardCreator(46);
        card4  = Card.CardCreator(12);
        card5  = Card.CardCreator(5);
        card6  = Card.CardCreator(19);
        card7  = Card.CardCreator(51);
        card8  = Card.CardCreator(33);
        card9  = Card.CardCreator(42);
        card10 = Card.CardCreator(37);
    }

    @Test
    public void testCreator() throws Exception {
        assertEquals(null, card2);
    }

    @Test
    public void isLessThan() throws Exception {
        // check by card numbers
        assertTrue(card1.isLessThan(4, true));
        assertTrue(card3.isLessThan(48, true));
        assertTrue(card4.isLessThan(17, true));
        assertTrue(card5.isLessThan(9, true));
        assertTrue(card6.isLessThan(21, true));
        // check for negatives
        assertFalse(card1.isLessThan(3, true));
        assertFalse(card3.isLessThan(44, true));
        assertFalse(card4.isLessThan(5, true));
        assertFalse(card5.isLessThan(6, true));
        assertFalse(card6.isLessThan(14, true));
        // by value
        assertTrue(card1.isLessThan(1, false));
        assertTrue(card3.isLessThan(12, false));
        assertTrue(card4.isLessThan(5, false));
        assertTrue(card5.isLessThan(8, false));
        assertTrue(card6.isLessThan(5, false));
        // check for false
        assertFalse(card1.isLessThan(0, false));
        assertFalse(card3.isLessThan(1, false));
        assertFalse(card4.isLessThan(3, false));
        assertFalse(card5.isLessThan(1, false));
        assertFalse(card6.isLessThan(2, false));
    }

    @Test
    public void isSameCardByCard() throws Exception {
        Card lCard1 = Card.CardCreator(0);
        Card lCard2 = Card.CardCreator(46);
        Card lCard3 = Card.CardCreator(12);
        Card lCard4 = Card.CardCreator(5);
        Card lCard5 = Card.CardCreator(19);
        Card lCard6 = Card.CardCreator(36);

        // Check by com.sentinella.james.com.sentinella.james.Card
        assertTrue(card1.isSameCard(lCard1));
        assertTrue(card3.isSameCard(lCard2));
        assertTrue(card4.isSameCard(lCard3));
        assertTrue(card5.isSameCard(lCard4));
        assertTrue(card6.isSameCard(lCard5));
        // Falses
        assertFalse(card1.isSameCard(lCard6));
        assertFalse(card1.isSameCard(lCard2));
        assertFalse(card3.isSameCard(lCard3));
        assertFalse(card3.isSameCard(lCard4));
        assertFalse(card4.isSameCard(lCard4));
        assertFalse(card5.isSameCard(lCard5));
        assertFalse(card6.isSameCard(lCard1));
        assertFalse(card6.isSameCard(lCard6));
    }

    @Test
    public void isSameCardByValue() throws Exception {
        // Check by com.sentinella.james.com.sentinella.james.Card
        assertTrue(card1.isSameCard(0));
        assertTrue(card3.isSameCard(46));
        assertTrue(card4.isSameCard(12));
        assertTrue(card5.isSameCard(5));
        assertTrue(card6.isSameCard(19));
        // Falses
        assertFalse(card1.isSameCard(36));
        assertFalse(card1.isSameCard(44));
        assertFalse(card3.isSameCard(15));
        assertFalse(card3.isSameCard(7));
        assertFalse(card4.isSameCard(5));
        assertFalse(card5.isSameCard(18));
        assertFalse(card6.isSameCard(1));
        assertFalse(card6.isSameCard(36));
    }

    @Test
    public void getNumValue() throws Exception {
        assertEquals(card1.getCardNumericFaceValue(), 0);
        assertEquals(card3.getCardNumericFaceValue(), 11);
        assertEquals(card4.getCardNumericFaceValue(), 3);
        assertEquals(card5.getCardNumericFaceValue(), 1);
        assertEquals(card6.getCardNumericFaceValue(), 4);
    }

    @Test
    public void getCardValue() throws Exception {
        assertEquals(0,  card1.getCardIndexNumber());
        assertEquals(46, card3.getCardIndexNumber());
        assertEquals(12, card4.getCardIndexNumber());
        assertEquals(5,  card5.getCardIndexNumber());
        assertEquals(19, card6.getCardIndexNumber());

    }

    @Test
    public void getStringRep() throws Exception {
        assertEquals("3 of clubs",        card1.getStringRep());
        assertEquals("Ace of hearts",     card3.getStringRep());
        assertEquals("6 of clubs",        card4.getStringRep());
        assertEquals("4 of diamonds",     card5.getStringRep());
        assertEquals("7 of spades",       card6.getStringRep());
        assertEquals("2 of spades",       card7.getStringRep());
        assertEquals("Jack of diamonds",  card8.getStringRep());
        assertEquals("King of hearts",    card9.getStringRep());
        assertEquals("Queen of diamonds", card10.getStringRep());
    }

    @Test
    public void compareTo() throws Exception {
        assertEquals(-1, card1.compareTo(card10));
        assertEquals(1,  card3.compareTo(card9));
        assertEquals(-1, card4.compareTo(card8));
        assertEquals(-1, card5.compareTo(card7));
        assertEquals(1,  card10.compareTo(card1));
        assertEquals(0,  card1.compareTo(card1));
        assertEquals(0,  card6.compareTo(card6));
    }

}