'''
/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
'''

import collections

# class for storing default state values
class D_STATE:
    WAITMIN  = 0
    LTIMEOUT = 1
    WTIMEOUT = 2
    WAITPLAY = 3
    NEWGAME  = 4

# class for storing global values
class gvals:
    # Variables related to game functions.
    lobby       = collections.deque('',99)
    maxclients  = 35
    lbctime     = 0.0       # last lobby broadcast time
    sincebc     = []        # clients added since last lobby broadcast
    
    clientlist = []         # clients (objects) connected to server
    
    # Variables relating to command line arguments.
    ltimeout    = 15        # lobby timeout
    timeout     = 30        # timeout for play/swap
    minplay     = 3         # minimum players for game
    strikeout   = 3         # number of strikes before disconnected

    # 0 = waiting for min players, 1 = lobby timeout, 2 = warlord timeout, 3 = waiting for play, 4 = newgame
    state = D_STATE.WAITMIN
    to_time = 0.0           # time storage for timeouts