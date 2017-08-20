package com.sentinella.james;

/**
 * Copyright (c) 2017 James Sentinella.
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class LogBookFactory {
    public static LogBook getLogBook(LogBook l, String debugStr){
        if (l instanceof GUILogBook) {
            return new GUILogBook((GUILogBook) l,debugStr);
        }
        return new LogBook(l,debugStr);
    }
}
