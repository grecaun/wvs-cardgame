package com.sentinella.james;

import java.util.HashMap;

/**
 * Created by James on 4/7/2016.
 */
public class StrikeErrors {
    private static HashMap<Integer, String> errors = new HashMap<Integer, String>();

    static {
        errors.put(0,"Unknown error.");
        errors.put(10,"Illegal play.");
        errors.put(11,"Cards sent do not have matching face values.");
        errors.put(12,"Face values of cards sent is too low.");
        errors.put(13,"Quantity of cards sent is too few.");
        errors.put(14,"Card not in player's hand.");
        errors.put(15,"Out of turn play.");
        errors.put(16,"Initial play of first hand must have 3 of clubs.");
        errors.put(17,"Played duplicates.");
        errors.put(18,"Pass on start.");
        errors.put(20,"Timeout.");
        errors.put(30,"Bad Message.");
        errors.put(31,"Lobby player sending play message.");
        errors.put(32,"Length exceeded.");
        errors.put(33,"Unknown message type.");
        errors.put(34,"Malformed message with known type.");
        errors.put(60,"Chat flood.");
        errors.put(70,"Illegal swap value.");
        errors.put(71,"Illegal swap message.");
        errors.put(72,"Swap message sent out of turn.");
        errors.put(80,"Can't connect.");
        errors.put(81,"Too many people already connected. Lobby full.");
    }

    public static String getErrorMessage(int number) {
        return errors.get(number);
    }
}
