package com.sentinella.james;

import org.junit.*;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by James on 1/29/2017.
 */
public class ServerPlayerTest {
    String              p1Name = "John";
    String              p2Name = "James";
    String              p3Name = "Hodor";
    TestServerPlayer    player1;
    TestServerPlayer    player2;
    TestServerPlayer    player3;
    SocketChannel       p1Con;
    SocketChannel       p2Con;
    SocketChannel       p3Con;

    @BeforeClass
    public static void oneTimeSetup() {
        System.out.println("Running ServerPlayer tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with ServerPlayer tests.");
    }

    @Before
    public void setUp() throws Exception {
        p1Con = SocketChannel.open();
        p2Con = SocketChannel.open();
        p3Con = SocketChannel.open();
        player1 = new TestServerPlayer(p1Name,p1Con,null);
        player2 = new TestServerPlayer(p2Name,p2Con,null);
        player3 = new TestServerPlayer(p3Name,p3Con,null);
    }

    @Test
    public void sendMessage() throws Exception {

    }

    @Test
    public void isPlayer() throws Exception {
        assertTrue(player1.isPlayer(p1Con));
        assertFalse(player1.isPlayer(p3Con));
        assertFalse(player1.isPlayer(p2Con));
        assertFalse(player2.isPlayer(p1Con));
        assertFalse(player2.isPlayer(p3Con));
        assertTrue(player2.isPlayer(p2Con));
        assertFalse(player3.isPlayer(p1Con));
        assertTrue(player3.isPlayer(p3Con));
        assertFalse(player3.isPlayer(p2Con));
    }

    @Test
    public void sendHand() throws Exception {
        player1.addCard(51);
        player1.addCard(0);
        player1.addCard(4);
        player1.sendHand();
        assertEquals("[shand|00,04,51,52,52,52,52,52,52,52,52,52,52,52,52,52,52,52]",player1.getLastMessage());
        player1.addCard(16);
        player1.addCard(21);
        player1.sendHand();
        assertEquals("[shand|00,04,16,21,51,52,52,52,52,52,52,52,52,52,52,52,52,52]",player1.getLastMessage());
        player1.addCard(34);
        player1.addCard(35);
        player1.sendHand();
        assertEquals("[shand|00,04,16,21,34,35,51,52,52,52,52,52,52,52,52,52,52,52]",player1.getLastMessage());
        player1.removeCard(0);
        player1.sendHand();
        assertEquals("[shand|04,16,21,34,35,51,52,52,52,52,52,52,52,52,52,52,52,52]",player1.getLastMessage());
    }

    @Test
    public void sortHand() throws Exception {
        player1.addCard(51);
        player1.addCard(21);
        player1.addCard(34);
        assertEquals(51,player1.getFirstCard().getCardIndexNumber());
        player1.sortHand();
        assertEquals(21,player1.getFirstCard().getCardIndexNumber());
        player1.addCard(0);
        player1.addCard(4);
        player1.sortHand();
        assertEquals(0,player1.getFirstCard().getCardIndexNumber());
        player1.addCard(16);
        player1.addCard(35);
        player1.removeCard(0);
        assertEquals(4,player1.getFirstCard().getCardIndexNumber());

    }

    @Test
    public void sendSwapW() throws Exception {
        player1.sendSwapW(Card.CardCreator(31));
        player1.sendSwapW(Card.CardCreator(4));
        player1.sendSwapW(Card.CardCreator(51));
        player1.sendSwapW(Card.CardCreator(0));
        player1.sendSwapW(null);
        assertEquals("[swapw|31]",player1.getLastMessage());
        assertEquals("[swapw|04]",player1.getLastMessage());
        assertEquals("[swapw|51]",player1.getLastMessage());
        assertEquals("[swapw|00]",player1.getLastMessage());
        assertEquals("[swapw|52]",player1.getLastMessage());
    }

    @Test
    public void sendStrike() throws Exception {
        player1.sendStrike(31);
        player1.sendStrike(12);
        player1.sendStrike(66);
        player1.sendStrike(6);
        assertEquals("[strik|31|1]",player1.getLastMessage());
        assertEquals("[strik|12|2]",player1.getLastMessage());
        assertEquals("[strik|66|3]",player1.getLastMessage());
        assertEquals("[strik|06|4]",player1.getLastMessage());
    }

    @Test
    public void sendSwapS() throws Exception {
        player1.sendSwapS(Card.CardCreator(31),Card.CardCreator(51));
        player1.sendSwapS(Card.CardCreator(12),Card.CardCreator(7));
        player1.sendSwapS(Card.CardCreator(5),Card.CardCreator(16));
        player1.sendSwapS(Card.CardCreator(21),Card.CardCreator(23));
        player1.sendSwapS(null,Card.CardCreator(34));
        assertEquals("[swaps|31|51]",player1.getLastMessage());
        assertEquals("[swaps|12|07]",player1.getLastMessage());
        assertEquals("[swaps|05|16]",player1.getLastMessage());
        assertEquals("[swaps|21|23]",player1.getLastMessage());
        assertEquals("[swaps|52|52]",player1.getLastMessage());
    }

    @Test
    public void addItemGetMessage() throws Exception {
        String string1 = "[message numero uno]";
        String string2 = "[two]";
        String string3 = "[san]";
        player1.addItem(string1);
        assertEquals(string1,player1.getMessage());
        player1.addItem(string2);
        assertEquals(string1+string2,player1.getMessage());
        player1.addItem(string3);
        assertEquals(string1+string2+string3,player1.getMessage());
        player1.addItem(string1);
        assertEquals(string1+string2+string3+string1,player1.getMessage());
    }

    @Test
    public void setMessageBuffer() throws Exception {
        String string1 = "[message numero uno]";
        String string2 = "[two]";
        String string3 = "[san]";
        player1.setMessageBuffer(string1);
        assertEquals(string1,player1.getMessage());
        player1.setMessageBuffer(string2+string3);
        assertEquals(string2+string3,player1.getMessage());
    }

    @Test
    public void setName() throws Exception {
        assertEquals(p1Name,player1.getName());
        player1.setName(p2Name);
        assertEquals(p2Name,player1.getName());
    }

    @Test
    public void clearCards() throws Exception {
        player1.addCard(0);
        player1.addCard(1);
        player1.addCard(2);
        player1.addCard(3);
        player1.addCard(4);
        player1.addCard(5);
        assertEquals(6,player1.getNumCards());
        player1.clearCards();
        assertEquals(0,player1.getNumCards());
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