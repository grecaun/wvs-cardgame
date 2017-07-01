package com.sentinella.james;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Created by James on 4/10/2016.
 */
public class TableTest {
    TestTable thisTable;

    @BeforeClass
    public static void oneTimeSetup() {
        System.out.println("Running Table tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with Table tests.");
    }

    @Before
    public void setUp() throws Exception {
        thisTable = new TestTable();
    }

    @Test
    public void isNotRanked() throws Exception {
        assertFalse(thisTable.isNotRanked());
        thisTable.setNotRanked(true);
        assertTrue(thisTable.isNotRanked());
        thisTable.setNotRanked(false);
        assertFalse(thisTable.isNotRanked());
    }


    @Test
    public void setInPlay() throws Exception {
        Card[] cards = thisTable.getCardArray();
        thisTable.setInPlay(52,52,15,14);
        assertTrue(cards[0].isSameCard(14));
        assertTrue(cards[1].isSameCard(15));
        assertEquals(null, cards[2]);
        assertEquals(null, cards[3]);
        assertEquals(2, thisTable.numInPlay());
        thisTable.setInPlay(52,52,52,-1);
        assertEquals(null, cards[0]);
        assertEquals(null, cards[1]);
        assertEquals(null, cards[2]);
        assertEquals(null, cards[3]);
        assertEquals(0, thisTable.numInPlay());
        thisTable.setInPlay(1,2,3,0);
        assertTrue(cards[0].isSameCard(0));
        assertTrue(cards[1].isSameCard(1));
        assertTrue(cards[2].isSameCard(2));
        assertTrue(cards[3].isSameCard(3));
        assertEquals(4, thisTable.numInPlay());
    }

    @Test
    public void getInPlayValue() throws Exception {
        thisTable.setInPlay(1,2,3,0);
        assertEquals(0, thisTable.getInPlayValue());
        thisTable.setInPlay(52,52,52,12);
        assertEquals(12/4, thisTable.getInPlayValue());
        thisTable.setInPlay(18,-1,-1,52);
        assertEquals(18/4, thisTable.getInPlayValue());
        thisTable.setInPlay(52,52,52,52);
        assertEquals(-1, thisTable.getInPlayValue());
    }

    @Test
    public void setPlayer() throws Exception {
        Player[] players = thisTable.getPlayerArray();
        Player   jon     = new Player("Jon");

        thisTable.setPlayer(0, jon);
        assertEquals(jon, players[0]);
        thisTable.setPlayer(2, pStatus.ACTIVE, 2, "James  ", 13);
        assertEquals(pStatus.ACTIVE, players[2].getStatus());
        assertEquals(2, players[2].getNumStrikes());
        assertEquals(13, players[2].getNumCards());
        assertEquals("James", players[2].getName());
        assertNotEquals(jon, players[2]);
        thisTable.setPlayer(0, pStatus.PASSED, 1, "Danny", 6);
        assertEquals(pStatus.PASSED, players[0].getStatus());
        assertEquals(1, players[0].getNumStrikes());
        assertEquals(6, players[0].getNumCards());
        assertEquals("Danny", players[0].getName());
    }

    @Test
    public void removePlayer() throws Exception {
        Player[] players = thisTable.getPlayerArray();
        Player   jon     = new Player("Jon");

        thisTable.setPlayer(0, jon);
        assertEquals(jon, players[0]);
        thisTable.setPlayer(2, pStatus.ACTIVE, 2, "James  ", 13);
        assertEquals(pStatus.ACTIVE, players[2].getStatus());
        assertEquals(2, players[2].getNumStrikes());
        assertEquals(13, players[2].getNumCards());
        assertEquals("James", players[2].getName());
        assertNotEquals(jon, players[2]);
        thisTable.removePlayer(0);
        assertEquals(null, players[0]);
        thisTable.removePlayer(2);
        assertEquals(null, players[2]);
    }

    @Test
    public void getPlayerStatus() throws Exception {
        Player   jon     = new Player("Jon");

        thisTable.setPlayer(0, jon);
        thisTable.setPlayer(2, pStatus.ACTIVE, 2, "James  ", 13);
        assertEquals(pStatus.ACTIVE, thisTable.getPlayerStatus("James"));
        assertEquals(pStatus.LOBBY, thisTable.getPlayerStatus("Jon"));
        assertNotEquals(pStatus.DISCONNECTED, thisTable.getPlayerStatus("Danny"));
        assertEquals(pStatus.NOTFOUND, thisTable.getPlayerStatus("Danny"));
        assertEquals(pStatus.LOBBY, thisTable.getSeatStatus(0));
        assertEquals(pStatus.EMPTY, thisTable.getSeatStatus(1));
        assertNotEquals(pStatus.EMPTY, thisTable.getSeatStatus(2));
        assertEquals(pStatus.ACTIVE, thisTable.getSeatStatus(2));
        assertEquals(pStatus.EMPTY, thisTable.getSeatStatus(3));
        assertEquals(pStatus.EMPTY, thisTable.getSeatStatus(4));
        assertEquals(pStatus.EMPTY, thisTable.getSeatStatus(5));
        assertEquals(pStatus.EMPTY, thisTable.getSeatStatus(6));
    }

    @Test
    public void getCardsLeft() throws Exception {
        thisTable.setPlayer(0, pStatus.WAITING, 2, "John    ", 5);
        thisTable.setPlayer(1, pStatus.ACTIVE, 0,  "James   ", 13);
        thisTable.setPlayer(2, pStatus.WAITING, 0, "Billy   ", 10);
        thisTable.setPlayer(6, pStatus.PASSED, 0,  "Rhaegar ", 8);

        assertEquals(5, thisTable.getCardsLeft("John"));
        assertEquals(5, thisTable.getCardsLeftBySeat(0));
        assertEquals(13, thisTable.getCardsLeft("James"));
        assertEquals(13, thisTable.getCardsLeftBySeat(1));
        assertEquals(10, thisTable.getCardsLeft("Billy"));
        assertEquals(10, thisTable.getCardsLeftBySeat(2));
        assertEquals(0, thisTable.getCardsLeft("Dan"));
        assertEquals(0, thisTable.getCardsLeftBySeat(3));
        assertEquals(0, thisTable.getCardsLeftBySeat(4));
        assertEquals(0, thisTable.getCardsLeftBySeat(5));
        assertEquals(8, thisTable.getCardsLeft("Rhaegar"));
        assertEquals(8, thisTable.getCardsLeftBySeat(6));
    }

    @Test
    public void getStrikes() throws Exception {
        thisTable.setPlayer(0, pStatus.WAITING, 2, "John    ", 5);
        thisTable.setPlayer(1, pStatus.ACTIVE, 0,  "James   ", 13);
        thisTable.setPlayer(2, pStatus.WAITING, 3, "Billy   ", 10);
        thisTable.setPlayer(6, pStatus.PASSED, 0,  "Rhaegar ", 8);

        assertEquals(2, thisTable.getStrikes("John"));
        assertEquals(2, thisTable.getStrikesBySeat(0));
        assertEquals(0, thisTable.getStrikes("James"));
        assertEquals(0, thisTable.getStrikesBySeat(1));
        assertEquals(3, thisTable.getStrikes("Billy"));
        assertEquals(3, thisTable.getStrikesBySeat(2));
        assertEquals(0, thisTable.getStrikes("Bob"));
        assertEquals(0, thisTable.getStrikesBySeat(3));
        assertEquals(0, thisTable.getStrikesBySeat(4));
        assertEquals(0, thisTable.getStrikesBySeat(5));
        assertEquals(0, thisTable.getStrikes("Rhaegar"));
        assertEquals(0, thisTable.getStrikesBySeat(6));
    }

    private class TestTable extends Table {
        public TestTable()               { super(); }
        public Card[]   getCardArray()   { return inPlay; }
        public Player[] getPlayerArray() { return players; }
    }
}