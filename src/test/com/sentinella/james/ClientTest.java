package com.sentinella.james;

import org.junit.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientTest {
    Client          theClient;

    Table           theTable;
    Lobby           theLobby;
    PlayerHand      theHand;

    @BeforeClass
    public static void oneTimeSetup() {
        System.out.println("Running Client tests.");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        System.out.println("All finished with Client tests.");
    }

    @Before
    public void setUp() throws Exception {
        theClient    = new Client(null, 0, "James", true);
        theTable     = theClient.getTable();
        theLobby     = theClient.getLobby();
        theHand      = theClient.getHand();
    }

    @Test
    public void testSLobb() throws Exception {
        String lobbyStr1 = "[slobb|01|John]";
        String lobbyStr2 = "[slobb|02|John    ]";
        String lobbyStr3 = "[slobb|15|John01  ,John02  ,John03  ,John04  ,John05  ,John06  ,John07  ,John08  ,John09  ,John10  ,John11  ,John12  ,John13  ,John14  ,John15  ]";
        String lobbyStr4 = "[slobb|04|John    ,Rhaegar ,Dinkle  ,James   ]";

        Client.errVal returnVal;

        returnVal = theClient.parseMessage(lobbyStr1);
        assertEquals(0,theLobby.numInLobby());
        assertEquals("Players in lobby:", theLobby.getLobbyString());
        assertEquals(Client.errVal.LOBBYERR, returnVal);

        returnVal = theClient.parseMessage(lobbyStr2);
        assertEquals(0,theLobby.numInLobby());
        assertEquals("Players in lobby:", theLobby.getLobbyString());
        assertEquals(Client.errVal.LOBBYERR, returnVal);

        returnVal = theClient.parseMessage(lobbyStr3);
        assertEquals(15,theLobby.numInLobby());
        assertEquals("Players in lobby: John01 John02 John03 John04 John05 John06 John07 John08 John09 John10 John11 John12 John13 John14 John15", theLobby.getLobbyString());
        assertEquals(Client.errVal.NOERR, returnVal);

        returnVal = theClient.parseMessage(lobbyStr4);
        assertEquals(4,theLobby.numInLobby());
        assertEquals("Players in lobby: John Rhaegar Dinkle James", theLobby.getLobbyString());
        assertEquals(Client.errVal.NOERR, returnVal);
    }

    @Test
    public void testSTabl() throws Exception {
        String tableStr1 = "[stabl|p1:Buddy   :04,w2:Bozo    :10,e0:        :00,a1:Lon     :05,e0:        :00,e0:        :00,e0:        :00|00,52,52,52|1]";
        String tableStr2 = "[stabl|p1:Buddy   :04,a2:Bozo    :10,w1:Lon     :15,e0:        :00,d3:John    :13,e0:        :00,e0:     :00|00,52,52,52|1]";
        String tableStr3 = "[stabl|w0:Buddy   :03,a2:Bozo    :10,d1:Lon     :15,e0:        :00,e0:        :00,d3:John    :13,e0:        :00|12,52,52,52|0]";
        String tableStr4 = "[stabl|p2:Buddy   :04,d2:Bozo    :10,e0:        :00,a1:Lon     :05,e0:        :00,e0:        :00,d2:John    :13|43,52,52,52|1]";

        Client.errVal returnVal;

        returnVal = theClient.parseMessage(tableStr1);
        assertEquals(Client.errVal.NOERR, returnVal);
        assertEquals(0,theTable.getInPlayValue());
        assertEquals(pStatus.PASSED, theTable.getPlayerStatus("Buddy"));
        assertEquals(pStatus.WAITING, theTable.getPlayerStatus("Bozo"));
        assertEquals(pStatus.ACTIVE, theTable.getPlayerStatus("Lon"));
        assertEquals(pStatus.NOTFOUND, theTable.getPlayerStatus("John"));
        assertEquals(4, theTable.getCardsLeft("Buddy"));
        assertEquals(5, theTable.getCardsLeft("Lon"));
        assertEquals(1, theTable.getStrikes("Buddy"));
        assertEquals(2, theTable.getStrikesBySeat(1));
        assertTrue(theTable.isNotRanked());

        returnVal = theClient.parseMessage(tableStr2);
        assertEquals(Client.errVal.TABLERR, returnVal);
        assertEquals(0,theTable.getInPlayValue());
        assertEquals(pStatus.PASSED, theTable.getPlayerStatus("Buddy"));
        assertEquals(pStatus.WAITING, theTable.getPlayerStatus("Bozo"));
        assertEquals(pStatus.ACTIVE, theTable.getPlayerStatus("Lon"));
        assertEquals(pStatus.NOTFOUND, theTable.getPlayerStatus("John"));
        assertEquals(4, theTable.getCardsLeft("Buddy"));
        assertEquals(5, theTable.getCardsLeft("Lon"));
        assertEquals(1, theTable.getStrikes("Buddy"));
        assertEquals(2, theTable.getStrikesBySeat(1));
        assertTrue(theTable.isNotRanked());

        returnVal = theClient.parseMessage(tableStr3);
        assertEquals(Client.errVal.NOERR, returnVal);
        assertEquals(3,theTable.getInPlayValue());
        assertEquals(pStatus.WAITING, theTable.getPlayerStatus("Buddy"));
        assertEquals(pStatus.ACTIVE, theTable.getPlayerStatus("Bozo"));
        assertEquals(pStatus.DISCONNECTED, theTable.getPlayerStatus("Lon"));
        assertEquals(pStatus.DISCONNECTED, theTable.getPlayerStatus("John"));
        assertEquals(3, theTable.getCardsLeft("Buddy"));
        assertEquals(15, theTable.getCardsLeft("Lon"));
        assertEquals(0, theTable.getStrikes("Buddy"));
        assertEquals(0, theTable.getStrikesBySeat(6));
        assertFalse(theTable.isNotRanked());

        returnVal = theClient.parseMessage(tableStr4);
        assertEquals(Client.errVal.NOERR, returnVal);
        assertEquals(10,theTable.getInPlayValue());
        assertEquals(pStatus.PASSED, theTable.getPlayerStatus("Buddy"));
        assertEquals(pStatus.DISCONNECTED, theTable.getPlayerStatus("Bozo"));
        assertEquals(pStatus.ACTIVE, theTable.getPlayerStatus("Lon"));
        assertEquals(pStatus.DISCONNECTED, theTable.getPlayerStatus("John"));
        assertEquals(10, theTable.getCardsLeft("Bozo"));
        assertEquals(13, theTable.getCardsLeft("John"));
        assertEquals(2, theTable.getStrikes("Buddy"));
        assertEquals(2, theTable.getStrikesBySeat(6));
        assertTrue(theTable.isNotRanked());
    }

    @Test
    public void testSJoin() throws Exception {
        String joinMsg1 = "[sjoin|James123]";
        String joinMsg2 = "[sjoin|Rhaegar ]";
        String joinMsg3 = "[sjoin|JonSnow ]";

        assertTrue(theClient.getName().equalsIgnoreCase("James"));

        theClient.parseMessage(joinMsg1);
        assertTrue(theClient.getName().equalsIgnoreCase("James123"));
        assertFalse(theClient.getName().equalsIgnoreCase("James"));

        theClient.parseMessage(joinMsg2);
        assertTrue(theClient.getName().equalsIgnoreCase("Rhaegar"));
        assertFalse(theClient.getName().equalsIgnoreCase("James"));

        theClient.parseMessage(joinMsg3);
        assertTrue(theClient.getName().equalsIgnoreCase("JonSnow"));
        assertFalse(theClient.getName().equalsIgnoreCase("James"));
    }

    @Test
    public void testSHand() throws Exception {
        String handMsg1 = "[shand|02,05,11,12,24,26,34,36,45,52,52,52,52,52,52,52,52,52]";
        String handMsg2 = "[shand|01,06,11,12,24,26,34,36,52,52,52,52,52,52,52,52,52,52]";
        String handMsg3 = "[shand|00,03,10,16,19,20,52,52,52,52,52,52,52,52,52,52,52,52]";

        theClient.parseMessage(handMsg1);
        assertEquals(9, theHand.count());
        assertTrue(theHand.hasCard(2));
        assertTrue(theHand.hasCard(5));
        assertTrue(theHand.hasCard(11));
        assertTrue(theHand.hasCard(12));
        assertTrue(theHand.hasCard(24));
        assertTrue(theHand.hasCard(26));
        assertTrue(theHand.hasCard(45));

        theClient.parseMessage(handMsg2);
        assertEquals(8, theHand.count());
        assertTrue(theHand.hasCard(1));
        assertTrue(theHand.hasCard(6));
        assertTrue(theHand.hasCard(11));
        assertTrue(theHand.hasCard(34));
        assertTrue(theHand.hasCard(36));
        assertFalse(theHand.hasCard(2));
        assertFalse(theHand.hasCard(5));
        assertFalse(theHand.hasCard(45));

        theClient.parseMessage(handMsg3);
        assertEquals(6, theHand.count());
        assertTrue(theHand.hasCard(0));
        assertTrue(theHand.hasCard(3));
        assertTrue(theHand.hasCard(19));
        assertTrue(theHand.hasCard(20));
        assertTrue(theHand.hasCard(16));
        assertFalse(theHand.hasCard(2));
        assertFalse(theHand.hasCard(5));
        assertFalse(theHand.hasCard(45));
        assertFalse(theHand.hasCard(12));
        assertFalse(theHand.hasCard(11));
        assertFalse(theHand.hasCard(1));

    }

    @Test
    public void testSTrik() throws Exception {
        String strikMsg1 = "[strik|00|1]";
        String strikMsg2 = "[strik|34|3]";
        String strikMsg3 = "[strik|71|2]";

        assertEquals(0, theClient.getStrikes());

        theClient.parseMessage(strikMsg1);
        assertEquals(1, theClient.getStrikes());

        theClient.parseMessage(strikMsg2);
        assertEquals(3, theClient.getStrikes());

        theClient.parseMessage(strikMsg3);
        assertEquals(2, theClient.getStrikes());
    }

    @Test
    public void testSwapW() throws Exception {
        String swapwMsg1 = "[swapw|00]";
        String swapwMsg2 = "[swapw|24]";
        String swapwMsg3 = "[swapw|12]";

        assertEquals(0, theHand.count());

        theClient.parseMessage(swapwMsg1);
        assertEquals(1, theHand.count());
        assertTrue(theHand.hasCard(0));

        theClient.parseMessage(swapwMsg2);
        assertEquals(2, theHand.count());
        assertTrue(theHand.hasCard(24));

        theClient.parseMessage(swapwMsg3);
        assertEquals(3, theHand.count());
        assertTrue(theHand.hasCard(12));
    }


    @Test
    public void testSwapS() throws Exception {
        String swapsMsg1 = "[swaps|00|24]";
        String swapsMsg2 = "[swaps|24|00]";
        String swapsMsg3 = "[swaps|12|24]";

        assertEquals(0, theHand.count());

        theClient.parseMessage(swapsMsg1);
        assertEquals(1, theHand.count());
        assertTrue(theHand.hasCard(0));

        theClient.parseMessage(swapsMsg2);
        assertEquals(1, theHand.count());
        assertTrue(theHand.hasCard(24));

        theClient.parseMessage(swapsMsg3);
        assertEquals(1, theHand.count());
        assertTrue(theHand.hasCard(12));
    }
}