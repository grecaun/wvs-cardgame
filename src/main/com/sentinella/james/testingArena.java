package com.sentinella.james;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by James on 1/26/2017.
 */
public class testingArena {
    static int currentSeat = 0;
    static Player[] players;
    public static void main(String[] args) throws InterruptedException {
        /*players = new Player[7];
        players[0] = new Player("server");
        players[2] = new Player("Server2");
        players[3] = new Player("Server3");
        players[4] = new Player("Server4");
        players[6] = new Player("Server5");
        players[0].addCard(0);
        players[2].addCard(1);
        players[3].addCard(2);
        players[4].addCard(3);
        players[6].addCard(4);
        currentSeat = 0;
        nextPlayer(); //2
        nextPlayer(); //3
        nextPlayer(); //4
        nextPlayer(); //6
        nextPlayer(); //0
        nextPlayer(); //2
        nextPlayer(); //3
        players[4].removeCard(3);
        nextPlayer(); //6
        players[0].setStatus(pStatus.DISCONNECTED);
        nextPlayer(); //2
        players[3].removeCard(2);
        players[6].setStatus(pStatus.DISCONNECTED);
        nextPlayer(); //2, currentSeat*/
        int test1 = 0;
        try {
            test1 = Integer.parseInt("something");
        } catch (Exception e) {

        }
        int test2 = Integer.parseInt(" 12 ".trim());
        int test3 = Integer.parseInt("b");
        System.out.println(String.format("%d %d %d", test1, test2, test3));
    }

    private static void nextPlayer() {
        int oldPlayer = currentSeat;
        for (int i=1;i<8;i++) {
            setCurrentSeat((oldPlayer + i) % 7);
            System.err.println(String.format("OldPlayer: %d, index: %d, modval: %d",oldPlayer,i,(oldPlayer+i)%7));
            if (players[currentSeat] != null && players[currentSeat].getStatus() != pStatus.DISCONNECTED && players[currentSeat].getNumCards() > 0) break;
        }
        System.err.println(String.format("Oldplayer %d - CurrentSeat %d",oldPlayer,currentSeat));
        if (oldPlayer == currentSeat) System.err.println("Same seat found.");
    }


    private static void setCurrentSeat(int i) {
        currentSeat = i;
    }
}
