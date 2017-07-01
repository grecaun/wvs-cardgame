#!/usr/bin/env python
'''
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

Warlords vs Scumbags server.

@author: James Sentinella
'''

import socket
import select
import sys
import random
import argparse
import re
import time
import collections
import threading
import Queue
from table import Table
from Client import Client
from globalvals import gvals, D_STATE

inlist = []

D_LTIME = 15   # default lobby timeout value
D_TIMEO = 15   # default timeout value (swap,play)
D_MINPL = 3    # default minimum players for game start
D_STRIK = 3    # default number of strikes before disconnect
D_CONNS = 35   # default maximum number of clients in lobby

# Regular expressions for recognizing input. First one deals with correctly formatted messages from the client.
RE_GENMSG = re.compile("\[(?P<cmd>\w{5})\|{0,1}(?P<msg>.*?)\]")
RE_CHKMSG = re.compile("(?P<chk>^.*?\])") # this one checks for ending bracket,
RE_CHKLEN = re.compile("(?P<len>[^\]]*)\[") # checks for messages before starting bracket
RE_GTPLAY = re.compile("(?P<c1>\d{2}),(?P<c2>\d{2}),(?P<c3>\d{2}),(?P<c4>\d{2})") # checks the cards in a play msg

def main():
    keepalive = 1       # Variable for keeping the server running.
    HOST = ''           # Localhost
    PORT = 36789        # Port number
    BLOG = 5            # Number of concurrent listens
    SIZE = 4096         # Size of data for receiving data

    # Command line argument parser.
    parser = argparse.ArgumentParser()
    parser.add_argument("-l",dest="l",type=int,default=D_LTIME,help="Sets the lobby timeout value (going from no game -> new game).")
    parser.add_argument("-t",dest="t",type=int,default=D_TIMEO,help="Sets the timeout value (for plays/warlord swap).")
    parser.add_argument("-m",dest="m",type=int,default=D_MINPL,help="Sets the minimum number of players for a game to run.")
    parser.add_argument("-s",dest="s",type=int,default=D_STRIK,help="Sets the number of strikes given out before disconnecting a client.")
    parser.add_argument("-c",dest="c",type=int,default=D_CONNS,help="Sets the maximum number of clients allowed in the lobby.")
    args = parser.parse_args()

    # Set values based on command line (or default values if none specified.)
    gvals.ltimeout = args.l
    gvals.timeout = args.t
    if args.m >= 3 and args.m <= 7:
        gvals.minplay = args.m
    elif args.m < 3:
        print "Minimum number of players to start a game must be 3 or greater. Setting value to 3."
        gvals.minplay = 3
    else:
        print "Maximum number of players for a game is 7. Useless it is to wait for more than 7. Setting to 7."
        gvals.minplay = 7
    if args.l < 35:
        gvals.maxclients = 35
    elif args.l > 90:
        gvals.maxclients = 90
    else:
        gvals.maxclients = args.l
    gvals.strikeout = args.s if args.s < 10 else 9 # strikes must be a single digit, i.e. max of 9

    try: # attempt to open the socket we need
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # the next line allows us to re-establish the socket immediately after closing
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind((HOST,PORT))
        server.listen(BLOG)
        inlist.append(server) # ensure that we're listening to the socket we opened
    except socket.error: # unable to establish the socket
        print "Unable to establish socket. Terminating."
        server.close() # ensure that we closed the server
        return

    print "Server live at " + str(socket.gethostbyname(socket.gethostname()))
    print "Clients connected: 0"
    print "----------------------------------"

    gvals.state = D_STATE.WAITMIN # set default state to waiting for min players

    # Create queue for reading system in.
    inputQueue = Queue.Queue()
    lock = threading.Lock()
    inThread = threading.Thread(target=input_thread, args=(inputQueue,lock,))
    inThread.daemon = True
    inThread.start()

    while keepalive:
        try:
            # use select statement, don't want it blocking for more than a second at any time
            inready, outready, exceptready = select.select(inlist, [], [], 1.0)
        except select.error:
            print "Unable to use select statement."
            break
        lock.acquire()
        while (not inputQueue.empty()): #check for quit command from terminal
            line = inputQueue.get().strip()
            if line == 'quit' or line == 'q':
                keepalive = 0
        lock.release()
        for s in inready: # loop through all of the sockets with information ready to be read
            if s == server:  # if the socket is our first socket, we're accepting the connection for a new client
                try: # attempt to accept the connection, then add it input list
                    client, address = server.accept()
                    inlist.append(client)
                    print "Clients connected: " + str(len(inlist)-1)
                    print "----------------------------------"
                except socket.error:
                    print "Unable to accept client connection."
            else: # otherwise we had someone sending us a message!
                try:
                    data = s.recv(SIZE, socket.MSG_PEEK)                # it is christmas! peek!
                    newdata = data.replace('\n','').replace('\r','')    # check to see if its just \n or \r
                    if len(data) > 0 and newdata == '':
                        data = s.recv(len(data))                        # pull out if so
                    elif newdata:                                       # check newline for correct messages
                        data = data.replace('\n',' ').replace('\r',' ') # replace \n and \r in data
                        peek = RE_CHKMSG.match(data)                    # peek = proper message
                        throwaway = RE_CHKLEN.match(data)               # throwaway = some string before [
                        if throwaway and throwaway.group("len"):        # if we have something to throwaway
                            data = s.recv(len(throwaway.group("len")))  # pull it from the buffer (and only it)
                            tdata = data.replace('\n','').replace('\r','')
                            if tdata:                                   # check to see if it was just \n and \r
                                send_strike(s,30)                       # if not, send strike to whomever this was
                        elif peek and peek.group("chk"):                # otherwise, if we had a properly formatted message
                            data = s.recv(len(peek.group("chk")))       # pull it out
                            newdata = data.replace('\n','').replace('\r','')
                            parserecv(s,newdata)                        # throw \n and \r away, then parse it
                        elif len(newdata) > 512:                        # if we've gotten 512 characters, and no [], then throw it all away
                            data = s.recv(SIZE)
                            send_strike(s,30)                           # and send a strike
                    elif not data:
                        close(s)                                        # if they send us nothing, we close them
                except socket.error:                                    # on socket error, we close them
                    close(s)
                    continue
        # Check the state, and do the appropriate action.
        if gvals.state == D_STATE.WAITMIN:                      # check if we're waiting for the minimum number of players before starting
            if len(gvals.lobby) >= gvals.minplay:               # check number of players in lobby
                gvals.state = D_STATE.LTIMEOUT                  # set state to lobby timeout state (waiting for set time before starting)
                gvals.to_time = time.time()                     # recording current time
                print "Play starting in " + str(gvals.ltimeout) + " seconds."
                print "----------------------------------"
        elif gvals.state == D_STATE.LTIMEOUT:                   # if we're waiting for a specified time
            curtime = time.time()                               # get current time
            if curtime > gvals.to_time + gvals.ltimeout:        # if we're less than the recorded time + timeout value, do nothing
                print "Let the game begin!"
                print "----------------------------------"
                Table.notranked = 1                             # make sure when going from lobby timeout to new hand we're doing unranked
                Table.newhand()                                 # else start a new hand
        elif gvals.state == D_STATE.WTIMEOUT:                   # waiting for warlord message
            Table.checkPlayers()                                # update the number of players at the table
            if Table.numplayers == 0:                           # if everyone left we can go to the waiting for players state
                gvals.state = D_STATE.WAITMIN
                print "No players at table. Waiting for players."
                print "----------------------------------"
            else:                                               # otherwise check the time
                rightnow = time.time()
                if rightnow > gvals.to_time + gvals.timeout or Table.player[7] not in gvals.clientlist:
                    strike(Table.player[1],20)                  # we haven't received a message yet, strike the player (warlord is default player 1)
                    if Table.passof[1] == 2:                    # if they're dead, we need to get the next player, because they were set as current player
                        Table.getNextPlayer()
                    if Table.wcard in Table.handof[1]:          # remove wcard if we had it in their hand
                        Table.handof[1].remove(Table.wcard)
                    Table.handof[7].append(Table.wcard)         # put the 'wcard' back in the scumbag's hands
                    Table.handof[7].sort()
                    Table.player[7].socket.send("[swaps|52|52]")
                    Table.StartGame()                           # start the game
        elif gvals.state == D_STATE.WAITPLAY:                   # waiting for current player to send their play message
            Table.checkPlayers()                                # check how many players we have
            if Table.numplayers == 0:                           # if none, wait for new players
                print "No players at table. Waiting for players."
                print "----------------------------------"
                gvals.state = D_STATE.WAITMIN
            elif Table.passof[Table.curplayer] == 2:            # if the player died, get the next player and play
                Table.getNextPlayer()
                Table.Play()
            else:                                               # otherwise, check the time and strike if necessary
                rightnow = time.time()
                if rightnow > gvals.to_time + gvals.timeout:
                    strike(Table.player[Table.curplayer],20)
                    Table.passof[Table.curplayer] = 1 if Table.passof[Table.curplayer] != 2 else 2 # set pass value to pass or dead, whichever applies
                    Table.getNextPlayer()                       # get next player
                    if gvals.state == D_STATE.WAITPLAY:         # if we're still in the waiting for play state, we can play
                        Table.Play()
        elif gvals.state == D_STATE.NEWGAME:                    # we need to start a new hand
            print "A new round is now starting."
            print "----------------------------------"
            Table.notranked = 0                                 # this state is only entered into if we were in a hand already, so by default it is ranked
            Table.newhand()

    server.close()                                              # we've exited our run loop, so close the socket

# Function for new thread for reading input.
def input_thread(inQueue, lock):
    while True:
        line = sys.stdin.readline()
        lock.acquire()
        inQueue.put(line)
        lock.release()

# function for dealing with join message from client
def join(con,msg):
    cname = msg             # the name the client wants to use
    errno = 0               # for telling ourselves if we need to send a strike
    if len(msg) > 8:        # name too long
        errno = 32
    elif len(msg) < 8:      # name not long enough
        errno = 34
    if errno != 0:          # if we need to, send strike
        send_strike(con,errno)
        return
    # Checking for invalid names.  Digits not allowed before any other characters
    if cname[0].isdigit():
        cname = 'A' + cname[1:]
    # ensuring that whitespace isn't between characters while checking for invalid characters
    for ci in range(0,8):
        # only alpha, digits, underscores and spaces allowed here
        if not (cname[ci].isalpha()) and not (cname[ci].isdigit()) and not (cname[ci] == '_') and not (cname[ci] == ' '):
            # replace characters not allowed with a random integer
            cname = cname.replace(cname[ci], str(random.randint(0,9)))
        # find the first space (if any, and ensure the chars after are spaces)
        if cname[ci] == ' ':
            cname = cname[0:ci]
            nci = ci
            while (nci<8):
                cname = cname + " "
                nci += 1
            break
    checkagain = 1  # we may need to check name multiple times if we change it
    while checkagain:
        checkagain = 0 # no conflict yet, so we set it to 0, so we'll exit the loop unless we find a conflict
        for c in gvals.clientlist:
            if cname == c.name:
                # we'll need to check again to ensure we didn't get a repeat
                checkagain = 1
                # Mangling a name, first find out how many spaces at end
                s_ix = 8    # Space index
                for index in range(0,8):
                    if cname[index] == ' ':
                        s_ix = index
                        break
                # to ensure unique name, ensure three spots at end of name for random number
                s_ix = 5 if s_ix > 5 else s_ix
                # place three ditits in there
                cname = cname[0:s_ix] + "%03d" %(random.randint(0,999))
                while (len(cname) < 8): # pad name out to 8 chars
                    cname = cname + " "
                break
            # if they've sent another join message after being assigned a name, give 'em a strike
            if c.equals(con):
                strike(c,30)
                return
    # make a new client object
    newclient = Client(cname,con)
    # add it to the clientlist
    gvals.clientlist.append(newclient)
    # add the client to the lobby
    addtolobby(newclient,cname)
    # send table message if necessary
    if gvals.state == 3:
        newclient.socket.send(Table.tableStr()+'\n')

# function for handling with chat messages from the client
def chat(con,msg):
    errno = 0                           # check for problems
    if len(msg) > 63:                   # if the length is not 63, we need to strike the user
        errno = 32
    elif len(msg) < 63:
        errno = 34
    if errno != 0:
        send_strike(con, errno)
        return
    for c in gvals.clientlist:          # find the name of the person who sent it
        if c.socket == con:
            name = c.name
            if c in gvals.sincebc:      # see if their lobby status has been broadcast to everyone
                broadcastlobby(None, True)
                gvals.sincebc = []      # if not, force a broadcast to everyone
            break
    for c in gvals.clientlist:          # send the chat message to everyone
        try:
            c.socket.send("[schat|"+name+"|"+msg+"]\n")
        except socket.error:            # close the socket of anyone who can't have the message sent to them
            close(c.socket)

# function to send a hand (if applicable) given a specific connection
def server_hand(con):
    for client in range(1,8):   # check all of the players
        if Table.player[client] and Table.player[client].equals(con):
            Table.sendhand(con,Table.handof[client])    #if this is the person, send the hand to them
            return

# function to check a play message
def play(con,msg):
    att_play = RE_GTPLAY.match(msg)     # use the RE to check the message
    client = None
    for c in gvals.clientlist:          # see if we can find the client
        if c.equals(con):
            client = c
            break
    if client == None:                  # if we did not, close the connection, cannot play if we don't know who you are
        close(con)
        return
    if client in gvals.lobby:           # if you're in the lobby you can't send a play message, strike
        strike(client,31)
        return
    if not Table.player[Table.curplayer].equals(con):
        strike(client,15)               # you're not the current player, strike
        server_hand(con)
        return
    if len(msg) > 11:
        strike(client,33)               # your message was too long, strike
        server_hand(con)
        return

    if att_play:                        # if the RE found something
                                        # build the play (an array of integers)
        play = [int(att_play.group("c1")),int(att_play.group("c2")),int(att_play.group("c3")),int(att_play.group("c4"))]
        errcode = Table.checkPlay(play) # check the play, errorcode being what it returns
        thisplayer = Table.curplayer    # set thisplayer to current player int so we can change curplayer later
        cards = 0                       # number of cards in play
        for acard in play:              # count the non 52 cards
            if acard != 52:
                cards += 1
        if errcode > 0:                 # errocdes > 0 == strikable offenses
            strike(client,errcode)      # send strikes, then hand, then tell them they can play again
            Table.sendhand(Table.player[Table.curplayer].socket,Table.handof[Table.curplayer])
            Table.Play()
            return
        elif errcode == -1: #pass situation, they passed, get the next player
            Table.passof[Table.curplayer] = 1
            Table.getNextPlayer()
        elif errcode == -2: #match situation, they force the next player to pass, then the next player after that gets to play
            Table.getNextPlayer()
            Table.passof[Table.curplayer] = 1
            Table.getNextPlayer()
            Table.numcards = cards
            Table.tablecards = play
            Table.tablecards.sort()
        elif errcode == -3: #two played, if a two was played, we start a new round
            Table.newround()
        else: # otherwise the play was a normal play, set status to waiting, get next player, set the table cards to what was played
            Table.passof[Table.curplayer] = 0
            Table.getNextPlayer()
            Table.numcards = cards
            Table.tablecards = play
            Table.tablecards.sort()

        for card in play: #for each of the cards in the play, remove them from the hand if they were in there
            if card in Table.handof[thisplayer]:
                Table.handof[thisplayer].remove(card)
            elif card != 52: # if the card wasn't in the hand and wasn't a 52, the card wasn't removed, this should never happen, this means checkPlay failed
                print "Card was not removed..."

        if len(Table.handof[thisplayer]) == 0: # if the player is now out of cards, we add them to the finished list
            Table.finished.appendleft(Table.player[thisplayer])
            print "%s added to finished list." %Table.player[thisplayer].name
            print "----------------------------"

        if gvals.state == 3: # if we're still in the play state, play again
            Table.Play()
    else: # this means the RE couldn't match the message
        strike(client,34)

# function that seals with swap message from clients
def swap(con,msg):
    if len(msg) > 2: #if length is greater than 2 or less than 2, it wasn't a proper message
        send_strike(con,32)
        return
    elif len(msg) < 2:
        send_strike(con,34)
        return
    else:
        try:    # attempt to convert it to an integer
            returncard = int(msg)
        except: # if unable to, send a strike
            send_strike(con,30)
            return      # if there is no player 1, or this isn't player 1, send strike then the hand if necessary
        if not Table.player[1] or not Table.player[1].equals(con):
            send_strike(con,71)
            server_hand(con)
            return      # if we don't want to receive a swap message right now, send a strike
        if gvals.state != 2:
            send_strike(con,72)
            server_hand(con)
            return      # if player 1 doesn't have the card, send strike and update their timeout timer
        if not returncard in Table.handof[1]:
            strike(Table.player[1],70)
            Table.sendhand(con,Table.handof[1])
            gvals.to_time = time.time()
            return
        else:           # otherwise, remove it from player 1's hand, append it to player 7's hand, sort 7
            Table.handof[1].remove(returncard)
            Table.handof[7].append(returncard)
            Table.handof[7].sort()
            try:        # send the swap message to the scumbag so they know what they lost and what they received
                Table.player[7].socket.send("[swaps|%02d|%02d]" %(returncard, Table.wcard))
            except socket.error:
                Table.passof[7] = 2
                Table.numplayers -= 1
                Table.getNextPlayer()
            gvals.state = D_STATE.WAITPLAY  # state is then put into the waiting for play state
            Table.StartGame()               # and we start the game

# function to deal with receiving a request for hand
def hand(con):
    for player in range(0,8):   # check if the player is a valid player and send hand it so
        if Table.player[player] and Table.player[player].equals(con):
            Table.sendhand(con,Table.handof[player])
            return
    send_strike(con,90)         # otherwise strike them

# function to parse received data from a connection
def parserecv(con, data):
    linedata = RE_GENMSG.search(data)       # use the regular expression to check for proper format
    if linedata == None:                    # improper format, send strike
        send_strike(con,30)
        return
    elif len(linedata.group("msg")) > 63:   # message too long, send strike
        send_strike(con,32)
        return
    command = linedata.group("cmd")         # pull command for comparison, call function specific to message
    if command == "cquit":
        close(con)
    elif command == "cjoin":
        join(con,linedata.group("msg"))
    elif command == "cchat":
        chat(con,linedata.group("msg"))
    elif command == "cplay":
        play(con,linedata.group("msg"))
    elif command == "chand":
        hand(con)
    elif command == "cswap":
        swap(con,linedata.group("msg"))
    else:                                   # if none recognized, send strike
        send_strike(con,33)
    return

# function to remove clients given a socket, i.e. remove them from the clientlist, the finished list, lobby, and record them as dead if part of the table
def remclient(con):
    for player in range(1,8): #check to see if the connection is a player, check if its in the client list
        if Table.player[player] and Table.player[player].equals(con) and Table.player[player] in gvals.clientlist:
            Table.passof[player] = 2    # set them as dead if so
            Table.numplayers -= 1       # record that there is one less player at the table
    for s in gvals.clientlist:          # check all the people in the clientlist
        if s.equals(con):               # if equal, remove from the list and lobby/finished if there
            gvals.clientlist.remove(s)
            if s in gvals.lobby:
                gvals.lobby.remove(s)
                broadcastlobby(None,False)
            if s in Table.finished:
                Table.finished.remove(s)

# function that closes the socket given a connection (socket)
def close(con):
    con.close()
    if con in inlist:
        inlist.remove(con)
        remclient(con)
        print "Clients connected: " + str(len(inlist)-1)
        print "----------------------------------"

# function to send a strike to a client given an error number
def strike(client, errorno):
    client.strikes += 1     # update the number of strikes the client has
    unabletosend = False    # check if unable to send message
    try:                    # send the strike message
        client.socket.send("[strik|%02d|%01d]\n" % (errorno,client.strikes))
    except socket.error:    # if error, we were unable to send
        unabletosend = True
                            # close the socket if they have too many strikes, or we weren't able to send the message
    if client.strikes >= gvals.strikeout or unabletosend == True:
        close(client.socket)

# fucntion to send a strike given a CONNECTION and error number
def send_strike(con,no):
    for c in gvals.clientlist:
        if c.equals(con):
            strike(c,no)
            return
    close(con)

# function to add a client to the lobby given the newclient
def addtolobby(newclient, cname):
    if len(gvals.lobby) >= gvals.maxclients:   # if lobby has more than # of allowed people in it, we can't add more
        newclient.socket.send("[strik|81|0]")
        close(newclient.socket)
    else:                       # otherwise add them to the lobby
        gvals.lobby.appendleft(newclient)
        try:                    # try to send their name to them
            newclient.socket.send("[sjoin|"+cname+"]\n")
        except socket.error:
            print "Unable to send sjoin message."
            close(newlcient.socket)
            return              # broadcast to lobby if everything went okay, don't force the broadcast
        broadcastlobby(newclient, False)

# function to broadcast the lobby message, checks last time we broadcast, sends to newclient if given, and if force is True will send regardless of time
def broadcastlobby(newclient, force):
    if len(gvals.lobby) == 0:   # if no one in the lobby, need to broadcast that there is no one there
        lstring = "[slobb|00]"
    else:
        newlobby = collections.deque('',99) # need to store the lobby somewhere while we build string
        lstring = "[slobb|%02d|" %len(gvals.lobby)
        while len(gvals.lobby) > 0:         # set up the string above, then build it person by person, pop then append into temp storage
            curperson = gvals.lobby.pop()
            newlobby.appendleft(curperson)
            lstring = lstring + curperson.name + ","
        lstring = lstring[0:len(lstring)-1] + ']'     # cap the string
        gvals.lobby = newlobby              # change lobby over to the temp one
    newtime = time.time()                   # check the time
    if (newtime > (gvals.lbctime + 10)) or (force == True):
        gvals.lbctime = newtime             # update the time if we're broadcastime (time or force told us to)
        for c in gvals.clientlist:          # send to everyone!
            try:
                c.socket.send(lstring+'\n')
            except socket.error:
                close(c.socket)
    elif newclient != None:                 # otherwise broadcast to the newclient if it exists and we didn't already broadcast it
        try:
            newclient.socket.send(lstring+'\n')
            gvals.sincebc.append(newclient)
        except socket.error:
            close(newclient.socket)

if __name__ == '__main__':
    try:
        main()
    finally:
        pass
