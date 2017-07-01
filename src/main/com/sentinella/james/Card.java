package com.sentinella.james;

/**
 * Created by James on 4/6/2016.
 */
public class Card implements Comparable<Card> {
    private int value = 0;
    private int suit = 0;

    private Card(int number) {
        value = number / 4;
        suit  = number % 4;
    }

    public static Card CardCreator(int number) {
        if (number < 52 && number >= 0) return new Card(number);
        return null;
    }

    public boolean isLessThan(int iValue, boolean cardNumber) {
        return cardNumber? this.value < (iValue/4) : this.value < iValue;
    }

    public boolean isSameCard(Card other) {
        return (this.value == other.value && this.suit == other.suit);
    }

    public boolean isSameCard(int other) {
        return ((value*4)+suit) == other;
    }

    public boolean isEqualValue(int iValue, boolean cardNumber) {
        return cardNumber? this.value == (iValue/4) : this.value == iValue;
    }

    public int getNumValue() {
        return value;
    }

    public int getCardValue() {
        return (value * 4) + suit;
    }

    public String getStringRep() {
        StringBuilder val = new StringBuilder();
        switch (value) {
            case 8:
                val.append("Jack");
                break;
            case 9:
                val.append("Queen");
                break;
            case 10:
                val.append("King");
                break;
            case 11:
                val.append("Ace");
                break;
            case 12:
                val.append("2");
                break;
            default:
                val.append(String.format("%d",value+3));
        }
        switch (suit) {
            case 0:
                val.append(" of clubs");
                break;
            case 1:
                val.append(" of diamonds");
                break;
            case 2:
                val.append(" of hearts");
                break;
            default:
                val.append(" of spades");
        }
        return val.toString();
    }

    @Override
    public int compareTo(Card o) {
        return this.value < o.value ? -1 : this.value > o.value ? 1 : this.suit < o.suit ? -1 : this.suit > o.suit ? 1 : 0;
    }
}
