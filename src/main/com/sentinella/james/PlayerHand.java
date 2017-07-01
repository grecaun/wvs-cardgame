package com.sentinella.james;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by James on 4/7/2016.
 */
public class PlayerHand {
    private ArrayList<Card> tHand;
    private boolean         debug = false;
    private Printer         printer;

    public PlayerHand() {
        tHand   = new ArrayList<>();
        printer = new BasicPrinter();
    }

    public synchronized void clear() { tHand.clear(); }

    public synchronized void add(Card iCard) {
        if (iCard == null || hasCard(iCard.getCardValue())) return;
        tHand.add(iCard);
    }

    public synchronized void add(int cardNo) {
        if (hasCard(cardNo)) return;
        Card newCard = Card.CardCreator(cardNo);
        if (newCard != null) tHand.add(newCard);
    }

    public synchronized void remove(Card iCard) {
        if (iCard == null) return;
        Card c;
        for (Iterator<Card> iterator = tHand.iterator(); iterator.hasNext();) {
            c = iterator.next();
            if (iCard.isSameCard(c)) {
                iterator.remove();
            }
        }
    }

    public synchronized void remove(int cardNo) {
        Card c;
        for (Iterator<Card> iterator = tHand.iterator(); iterator.hasNext();) {
            c = iterator.next();
            if (c.isSameCard(cardNo)) {
                iterator.remove();
            }
        }
    }

    public synchronized List<Card> getLowest(int valToMatch, int numCards) {
        ArrayList<Card> output = new ArrayList<>();
        int cardsInHand = tHand.size(), maxOffset = numCards > 0 ? numCards-1 : 0;
        if (cardsInHand <= 0) return output;
        sort();
        if (debug) printHand();
        for (int i=0;i<cardsInHand-maxOffset;i++) {
            int curCardNumVal = tHand.get(i).getNumValue();
            if (!tHand.get(i).isLessThan(valToMatch, false) && i+maxOffset < tHand.size() && tHand.get(i+maxOffset).getNumValue()==curCardNumVal) {
                for (int inside=i;inside<i+maxOffset;inside++) {
                   output.add(tHand.get(inside));
                }
                for (int extra=i+maxOffset;extra<i+4;extra++){
                    if (extra < tHand.size() && curCardNumVal == tHand.get(extra).getNumValue()) {
                       output.add(tHand.get(extra));
                    }
                }
                break;
            }
        }
        tHand.removeAll(output);
        return output;
    }

    public void printHand() {
        StringBuilder output = new StringBuilder();
        output.append("Your cards are:");
        for (Iterator<Card> iterator = tHand.iterator(); iterator.hasNext();) {
            output.append(" ");
            output.append(iterator.next().getStringRep());
        }
        printer.printString(output.toString());
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Card c : tHand) {
            output.append(c.getCardValue());
            output.append(" ");
        }
        return output.toString().trim();
    }

    public synchronized Card getLowest() {
        if (tHand.size() == 0) return null;
        sort();
        return tHand.remove(0);
    }

    public synchronized Card getHighest() {
        int handSize = tHand.size();
        if (handSize == 0) return null;
        sort();
        return tHand.remove(handSize-1);
    }

    public synchronized boolean hasCard(int cardNo) {
        for (Card c : tHand) {
            if (c.isSameCard(cardNo)) return true;
        }
        return false;
    }

    public synchronized int count() {
        return tHand.size();
    }

    public synchronized void sort() { Collections.sort(tHand); }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setPrinter(Printer printer) {
        this.printer = printer;
    }

    public ArrayList<Card> getHand() { return tHand; }
}
