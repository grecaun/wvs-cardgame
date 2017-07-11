package com.sentinella.james;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by James on 1/18/2017.
 */
public class ServerPlayer extends Player {
    private SocketChannel con;
    private SelectionKey  key;
    private StringBuffer  buffer;
    private ByteBuffer    outputBuffer;

    public ServerPlayer(String iName, pStatus iStatus, int iStrikes, int iNumCards, SocketChannel iCon, SelectionKey iKey) {
        super(iName, iStatus, iStrikes, iNumCards);
        con          = iCon;
        key          = iKey;
        buffer       = new StringBuffer(256);
        outputBuffer = ByteBuffer.allocate(512);
    }

    public ServerPlayer(String iName, SocketChannel iCon, SelectionKey iKey) {
        super(iName);
        con     = iCon;
        key     = iKey;
        buffer  = new StringBuffer(256);
        outputBuffer = ByteBuffer.allocate(512);
    }

    public CONERROR sendMessage(String message) {
        outputBuffer.clear();
        outputBuffer.put(message.getBytes());
        outputBuffer.flip();
        while (outputBuffer.hasRemaining()) {
            try {
                con.write(outputBuffer);
            } catch (IOException e) {
                return CONERROR.UNABLETOSEND;
            }
        }
        return CONERROR.NOERROR;
    }

    public boolean isPlayer(SocketChannel sock) {
        if (con.equals(sock)) return true;
        return false;
    }

    public Card getFirstCard() {
        return getHand().getHand().get(0);
    }

    public CONERROR sendHand() {
        StringBuilder output = new StringBuilder("[shand|");
        sortHand();
        for (Card c : getHand().getHand()) output.append(String.format("%02d,", c.getCardIndexNumber()));
        while (output.length() < 61) output.append("52,");
        output.replace(output.length()-1, output.length(), "]");
        return sendMessage(output.toString());
    }

    public void sortHand() {
        cards.sort();
    }

    public CONERROR sendSwapW(Card warlordCard) {
        addCard(warlordCard);
        sortHand();
        if (sendHand() == CONERROR.NOERROR) {
            return sendMessage(String.format("[swapw|%02d]",warlordCard == null? 52 :warlordCard.getCardIndexNumber()));
        }
        return CONERROR.UNABLETOSEND;
    }

    public CONERROR sendStrike(int strikeNo) {
        return sendMessage(String.format("[strik|%02d|%1d]",strikeNo,++strikes));
    }

    public CONERROR sendSwapS(Card returnCard, Card warlordCard) {
        if (returnCard == null || warlordCard == null) return sendMessage("[swaps|52|52]");
        addCard(returnCard);
        sortHand();
        return sendMessage(String.format("[swaps|%02d|%02d]",returnCard.getCardIndexNumber(),warlordCard.getCardIndexNumber()));
    }

    public void closeSocket() {
        try {
            con.close();
            key.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        return buffer.toString();
    }

    public void addItem(String item) {
        buffer.append(item);
    }

    public void setMessageBuffer(String leftOvers) {
        buffer.setLength(0);
        buffer.append(leftOvers);
    }

    public SelectionKey getKey() {return key;}
    public SocketChannel getCon() {return con;}

    public void clearCards() {
        numCards = 0;
        cards.clear();
    }

    @Override
    public int getNumCards() {
        return cards.count();
    }

    public void setName(String name) {
        this.name = name;
    }

    public enum CONERROR {
        NOERROR, UNABLETOSEND
    }
}
