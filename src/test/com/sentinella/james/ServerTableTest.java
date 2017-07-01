package com.sentinella.james;

import org.junit.*;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by James on 1/29/2017.
 */
public class ServerTableTest {
    ServerTable table;
    ServerLobby lobby;
    String p1Name = "JonSnow ";
    String p2Name = "Hodor   ";
    String p3Name = "James   ";
    String p4Name = "John    ";
    String p5Name = "Daenarys";
    String p6Name = "Bozo    ";
    String p7Name = "Tweedle ";
    TestServerPlayer player1;
    TestServerPlayer player2;
    TestServerPlayer player3;
    TestServerPlayer player4;
    TestServerPlayer player5;
    TestServerPlayer player6;
    TestServerPlayer player7;

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
        lobby = new ServerLobby();
        table = new ServerTable(3,lobby);
        player1 = new TestServerPlayer(p1Name);
        player2 = new TestServerPlayer(p2Name);
        player3 = new TestServerPlayer(p3Name);
        player4 = new TestServerPlayer(p4Name);
        player5 = new TestServerPlayer(p5Name);
        player6 = new TestServerPlayer(p6Name);
        player7 = new TestServerPlayer(p7Name);
        table.setPlayer(0,player1);
        table.setPlayer(1,player2);
        table.setPlayer(2,player3);
        table.setPlayer(3,player4);
        table.setPlayer(4,player5);
        table.setPlayer(5,player6);
        table.setPlayer(6,player7);
        player1.addCard(0);
        player2.addCard(1);
        player3.addCard(2);
        player4.addCard(3);
        player5.addCard(4);
        player6.addCard(5);
        player7.addCard(6);
    }

    @Test
    public void newHand() throws Exception {
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);
        table.setNotRanked(true);
        assertEquals(Server.SERVERSTATE.WAITFORPLAYMSG,table.newHand());
        assertEquals(51,player1.getNumCards()+player2.getNumCards()+player3.getNumCards());
        table.addToFinished(player1);
        table.addToFinished(player2);
        table.addToFinished(player3);
        table.setNotRanked(false);
        assertEquals(Server.SERVERSTATE.WAITFORSWAPMSG,table.newHand());
        assertEquals(52,player1.getNumCards()+player2.getNumCards()+player3.getNumCards());
        assertEquals(Server.SERVERSTATE.INSUFFPLAYERS,table.newHand());
    }

    @Test
    public void startGame() throws Exception {
        assertEquals(Server.SERVERSTATE.WAITFORPLAYMSG, table.startGame());
        assertEquals(player1, table.getCurrentPlayer());
        assertEquals(1,player1.getNumCards());
        table.setNotRanked(true);
        assertEquals(Server.SERVERSTATE.WAITFORPLAYMSG, table.startGame());
        assertEquals(player2, table.getCurrentPlayer());
        assertEquals(0,player1.getNumCards());
        table.setNotRanked(true);
        player4.addCard(0);
        assertEquals(Server.SERVERSTATE.WAITFORPLAYMSG, table.startGame());
        assertEquals(player5, table.getCurrentPlayer());
        assertEquals(1,player4.getNumCards());
    }

    @Test
    public void play() throws Exception {
        assertEquals(Server.SERVERSTATE.WAITFORPLAYMSG, table.play());
        player1.clearCards();
        player2.clearCards();
        player3.clearCards();
        player4.clearCards();
        player5.clearCards();
        player6.clearCards();
        assertEquals(Server.SERVERSTATE.NEWGAME, table.play());
    }

    @Test
    public void addStrike() throws Exception {
        table.addStrike(0,31);
        table.addStrike(0,12);
        table.addStrike(0,15);
        assertEquals(3,player1.getNumStrikes());
        assertEquals("[strik|31|1]",player1.getLastMessage());
        assertEquals("[strik|12|2]",player1.getLastMessage());
        assertEquals("[strik|15|3]",player1.getLastMessage());
        table.addStrike(2,31);
        assertEquals(1,player3.getNumStrikes());
        table.addStrike(5,31);
        table.addStrike(5,31);
        assertEquals(2,player6.getNumStrikes());
        table.addStrike(6,31);
        table.addStrike(6,31);
        table.addStrike(6,31);
        table.addStrike(6,31);
        assertEquals(4,player7.getNumStrikes());
        table.addStrike(1,31);
        table.addStrike(1,31);
        assertEquals(2,player2.getNumStrikes());
        table.addStrike(3,31);
        assertEquals(1,player4.getNumStrikes());
        table.addStrike(4,31);
        assertEquals(1,player5.getNumStrikes());
    }

    @Test
    public void strikeActiveAndPass() throws Exception {
        Player curPlay = table.getCurrentPlayer();
        table.strikeActiveAndPass(31);
        assertEquals(1,curPlay.getNumStrikes());
        assertEquals(pStatus.PASSED,curPlay.getStatus());
        assertFalse(curPlay.isPlayer(table.getCurrentPlayer()));
        curPlay = table.getCurrentPlayer();
        table.strikeActiveAndPass(42);
        assertEquals(1,curPlay.getNumStrikes());
    }

    @Test
    public void sendSwapScum() throws Exception {
        table.setWarlordCard(Card.CardCreator(13));
        table.sendSwapScum(Card.CardCreator(12));
        assertEquals("[swaps|12|13]",player7.getLastMessage());
    }

    @Test
    public void getCurrentPlayerStatus() throws Exception {
        player1.setStatus(pStatus.DISCONNECTED);
        assertEquals(pStatus.DISCONNECTED,table.getCurrentPlayerStatus());
        table.nextPlayer();
        assertEquals(pStatus.ACTIVE,table.getCurrentPlayerStatus());
    }

    @Test
    public void arrangeTable() throws Exception {
        table.removePlayer(2);
        table.removePlayer(3);
        table.removePlayer(4);
        table.removePlayer(5);
        table.addToFinished(player2);
        table.addToFinished(player1);
        table.addToFinished(player7);
        table.arrangeTable();
        assertFalse(table.isNotRanked());
        assertEquals(3,table.getActivePlayers());
        assertEquals(player2, table.getCurrentPlayer());
        assertEquals(0,player1.getNumCards());
        assertEquals(0,player2.getNumCards());
        assertEquals(0,player7.getNumCards());
        player1.addCard(0);
        player2.addCard(1);
        player7.addCard(2);
        table.nextPlayer();
        assertEquals(player1, table.getCurrentPlayer());
        table.nextPlayer();
        assertEquals(player7, table.getCurrentPlayer());
        table.addToFinished(player7);
        lobby.addPlayer(player1);
        lobby.addPlayer(player2);
        lobby.addPlayer(player3);
        lobby.addPlayer(player6);
        table.arrangeTable();
        assertTrue(table.isNotRanked());
        assertEquals(5,table.getActivePlayers());
        assertEquals(player7, table.getCurrentPlayer());
    }

    @Test
    public void getActivePlayers() throws Exception {
        assertEquals(7,table.getActivePlayers());
        player1.setStatus(pStatus.DISCONNECTED);
        player3.setStatus(pStatus.DISCONNECTED);
        player4.setStatus(pStatus.DISCONNECTED);
        player7.setStatus(pStatus.DISCONNECTED);
        assertEquals(3,table.getActivePlayers());
    }

    @Test
    public void nextPlayerCurrentPlayer() throws Exception {
        assertTrue(player1.isPlayer(table.getCurrentPlayer()));
        assertTrue(table.nextPlayer()); //2
        assertTrue(table.nextPlayer()); //3
        assertTrue(table.nextPlayer()); //4
        assertTrue(table.nextPlayer()); //5
        assertTrue(table.nextPlayer()); //6
        assertTrue(table.nextPlayer()); //7
        assertTrue(table.nextPlayer()); //1
        assertTrue(table.nextPlayer()); //2
        assertTrue(table.nextPlayer()); //3
        assertTrue(player3.isPlayer(table.getCurrentPlayer()));
        player4.removeCard(3); //4 not available
        assertTrue(table.nextPlayer()); //5
        assertTrue(player5.isPlayer(table.getCurrentPlayer()));
        player6.setStatus(pStatus.DISCONNECTED); //4+6 not avail
        assertTrue(table.nextPlayer()); //7
        assertTrue(player7.isPlayer(table.getCurrentPlayer()));
        player1.removeCard(0); //1+4+6 not avail
        player2.setStatus(pStatus.DISCONNECTED); //1+2+4+6 not avail
        assertTrue(table.nextPlayer()); //3
        assertTrue(player3.isPlayer(table.getCurrentPlayer()));
        player5.setStatus(pStatus.DISCONNECTED); //1+2+4+5+6 not avail
        player7.setStatus(pStatus.DISCONNECTED); //1+2+4+5+6+7 not avail
        assertFalse(table.nextPlayer()); //3
        assertTrue(player3.isPlayer(table.getCurrentPlayer()));
    }

    @Test
    public void newRound() throws Exception {
        table.setInPlay(0,1,2,3);
        table.newRound();
        assertEquals(0,table.numInPlay());
        assertEquals(-1,table.getInPlayValue());
        assertEquals(pStatus.ACTIVE,player1.getStatus());
        assertEquals(pStatus.WAITING,player2.getStatus());
        assertEquals(pStatus.WAITING,player3.getStatus());
        assertEquals(pStatus.WAITING,player4.getStatus());
        assertEquals(pStatus.WAITING,player5.getStatus());
        assertEquals(pStatus.WAITING,player6.getStatus());
        assertEquals(pStatus.WAITING,player7.getStatus());
    }

    @Test
    public void getTableMessage() throws Exception {
        player1.addCard(0);
        player1.addCard(2);
        player1.addCard(3);
        player1.addCard(4);
        player1.addCard(5);
        player1.addCard(6);
        player1.addCard(7);
        player1.addCard(8);
        player1.addCard(9);
        player1.addCard(10);
        player1.addCard(20);
        player1.addCard(30);
        player1.addCard(40);
        player2.addCard(31);
        player2.addCard(32);
        player2.addCard(33);
        player3.addCard(45);
        player1.sendStrike(12);
        player7.sendStrike(12);
        player7.setStatus(pStatus.DISCONNECTED);
        player6.setStatus(pStatus.PASSED);
        table.removePlayer(3);
        table.setNotRanked(true);
        String thisString = String.format("[stabl|a1:JonSnow :13,w0:Hodor   :04,w0:James   :02,e0:        :00,w0:Daenarys:01,p0:Bozo    :01,d1:Tweedle :01|52,52,52,52|0]");
        assertEquals(thisString,table.getTableMessage());
    }

    @Test
    public void checkPlay() throws Exception {
        player1.addCard(2);
        player1.addCard(3);
        int[] cards = {2,3,52,52};
        table.setInPlay(0,1,52,52);
        assertEquals(-2,table.checkPlay(cards));
        table.setInPlay(0,52,52,52);
        assertEquals(0,table.checkPlay(cards));
        cards = new int[]{50, 52, 52, 52};
        player1.addCard(50);
        assertEquals(-3,table.checkPlay(cards));
        cards = new int[]{1,6,52,52};
        assertEquals(14,table.checkPlay(cards));
        player1.addCard(1);
        player1.addCard(6);
        assertEquals(11,table.checkPlay(cards));
        player1.addCard(0);
        cards = new int[]{0,0,52,52};
        assertEquals(17,table.checkPlay(cards));
        table.setInPlay(5,6,52,52);
        assertEquals(12,table.checkPlay(cards));
        cards = new int[]{7,52,52,52};
        player1.addCard(7);
        assertEquals(13,table.checkPlay(cards));
        cards = new int[] {52,52,52,52};
        assertEquals(-1,table.checkPlay(cards));
        table.setInPlay(52,52,52,52);
        assertEquals(18,table.checkPlay(cards));
        table.setPlayer(0,null);
        assertEquals(99,table.checkPlay(cards));
    }

    @Test
    public void isAtTable() throws Exception {
        assertTrue(table.isAtTable(player1));
        assertTrue(table.isAtTable(player2));
        assertTrue(table.isAtTable(player3));
        assertTrue(table.isAtTable(player4));
        assertTrue(table.isAtTable(player5));
        assertTrue(table.isAtTable(player6));
        assertTrue(table.isAtTable(player7));
        table.setPlayer(0,null);
        assertFalse(table.isAtTable(player1));
        player2 = new TestServerPlayer("Hello");
        assertFalse(table.isAtTable(player2));
    }

    @Test
    public void isCurrentPlayer() throws Exception {
        assertTrue(table.isCurrentPlayer(player1));
        table.nextPlayer();
        assertFalse((table.isCurrentPlayer(player1)));
        assertTrue(table.isCurrentPlayer(player2));
        table.nextPlayer();
        table.nextPlayer();
        assertTrue(table.isCurrentPlayer(player4));
    }

    @Test
    public void isWarlord() throws Exception {
        assertTrue(table.isWarlord(player1));
        assertFalse(table.isWarlord(player2));
        assertFalse(table.isWarlord(player3));
        assertFalse(table.isWarlord(player4));
        assertFalse(table.isWarlord(player5));
        assertFalse(table.isWarlord(player6));
        assertFalse(table.isWarlord(player7));
    }

    @Test
    public void getSeatNos() {
        assertEquals(0,table.getWarlordSeat());
        assertEquals(6,table.getScumbagSeat());
    }

    private class TestServerPlayer extends ServerPlayer {
        ArrayList<String> isWritten;

        public TestServerPlayer(String iName) {
            super(iName, null, null);
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