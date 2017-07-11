'''
/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
'''

class Client:
    name = ""           # name the client is given
    strikes = 0         # number of strikes the client has
    socket = None       # socket used to communicate with the client

    def __init__(self,name,socket):
        self.name = name
        self.socket = socket
        self.strikes = 0

    # a client is equal to a socket if the client's socket equals the socket given
    def equals(self, inputsock):
        if inputsock == self.socket:
            return True
        else:
            return False