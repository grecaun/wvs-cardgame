#!/usr/bin/env python
'''
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

Main python file for the Warlords vs Scumbag client.

@author: James Sentinella
'''

import socket
import select
import sys
import argparse
import random
import re
import errors
import threading
import Queue
from cTable import Table, Card, Player

'''
    Default values for sockets.
    List of default names for the client.
    Regular expressions
'''

D_HOST = 'localhost'
D_PORT = 36789
D_AUTO = True
SIZE   = 1024
PAD    = "                                                               "

D_NAMES = ["Raegar  ","Bran    ","Hodor   ","Jon     ","TheFuzz ","Ned     ","Bozo    ","Captain ","Spirit  ","Wolf    "]

RE_GENMSG = re.compile("\[(?P<cmd>\w{5})\|{0,1}(?P<msg>.*?)\]")
RE_CHKMSG = re.compile("(?P<chk>^.*?\])")
RE_CHKLEN = re.compile("(?P<len>[^\]]*)\[")

RE_INPUT = re.compile("(?P<cmd>\w{1,6})\s{0,1}(?P<msg>.*\s)")
RE_PLAYM = re.compile("([\dAJKQajkq]{1,2})([cCdDsShH]{0,1})\s")

RE_STABL = re.compile("(?P<p1s>[apwdeAPWDE])\d:(?P<p1n>[A-Za-z0-9_ ]{8}):(?P<p1c>\d{2}),(?P<p2s>[apwdeAPWDE])\d:(?P<p2n>[A-Za-z0-9_ ]{8}):(?P<p2c>\d{2}),(?P<p3s>[apwdeAPWDE])\d:(?P<p3n>[A-Za-z0-9_ ]{8}):(?P<p3c>\d{2}),(?P<p4s>[apwdeAPWDE])\d:(?P<p4n>[A-Za-z0-9_ ]{8}):(?P<p4c>\d{2}),(?P<p5s>[apwdeAPWDE])\d:(?P<p5n>[A-Za-z0-9_ ]{8}):(?P<p5c>\d{2}),(?P<p6s>[apwdeAPWDE])\d:(?P<p6n>[A-Za-z0-9_ ]{8}):(?P<p6c>\d{2}),(?P<p7s>[apwdeAPWDE])\d:(?P<p7n>[A-Za-z0-9_ ]{8}):(?P<p7c>\d{2})\|(?P<c1>\d{2}),(?P<c2>\d{2}),(?P<c3>\d{2}),(?P<c4>\d{2})\|(?P<r>\d)")
RE_SHAND = re.compile("(\d{2})")
RE_SWAPS = re.compile("(?P<ncard>\d{2})\|(?P<lcard>\d{2})")
RE_SLOBB = re.compile("([^,\|]{8})")
RE_SCHAT = re.compile("(?P<name>[A-Za-z0-9_ ]{8})\|(?P<msg>.{0,63})")
RE_STRIK = re.compile("(?P<code>\d{2})\|(?P<num>\d{1})")

'''
    Variables above this deal with default values and regular expressions.
    Variables below this deal with "global" values.
'''

state = 0  # 0 = nothing, 1 = waiting for play, 2 = waiting for play validation, 3 = waiting for swap card, 4 = waiting for swap validation
keepalive = 1
auto = True

# main program code
def main():
    global auto             # need access to auto values and state values
    global state
        # command line argument parser code
    parser = argparse.ArgumentParser()
    parser.add_argument("-m", "--manual", dest="m", action="store_true", help="Run the client in manual mode.")
    parser.add_argument("-s", "--server", dest="s", type=str, default=D_HOST, help="Server IP address.")
    parser.add_argument("-p", "--port", dest="p", type=int, default=D_PORT, help="Server port number.")
    parser.add_argument("-n", "--name", dest="n", type=str, help="The name you wish to use while playing.")
    args = parser.parse_args()

    # set up values based upon command line arguments
    auto = True if args.m == False else False
    host = args.s
    port = args.p
    if args.n:                  # check if a name was given
        if len(args.n) < 8:     # if it was less than 8 characters, pad it to 8 characters
            name = args.n[0:8] + PAD[0:8-len(args.n)]
        else:                   # otherwise take the first 8 characters
            name = args.n[0:8]
    else:                       # no name given, chose randomly from the array
        name = D_NAMES[random.randint(0,len(D_NAMES)-1)]

    # Create queue for reading system in.
    inputQueue = Queue.Queue()
    lock = threading.Lock()
    inThread = threading.Thread(target=input_thread, args=(inputQueue,lock,))
    inThread.daemon = True
    inThread.start()

    try:                        # attempt to establish connection to the server
        con = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        con.connect((host, port))
    except socket.error:
        print "Unable to establish connection to server. Program terminating."
        return

    try:                        # try to send the join message
        con.send("[cjoin|"+name+"]\n")
    except socket.error:
        print "Unable to send join message. Program terminating."
        return

    global keepalive            # ensure we can see and modify the keepalive variable which will terminate the loop if necessary

    while keepalive:
        try:                    # try to use the select statement
            inready, outready, exceptread = select.select([con],[],[],1.0)
        except select.error:
            print "Unable to use select statement."
            break
        lock.acquire()
        while (not inputQueue.empty()): # clear chat commands before doing anything else
            line = inputQueue.get()
            lineargs = RE_INPUT.match(line)
            if lineargs:
                parse_line(lineargs.group("cmd"),lineargs.group("msg"),con)
            else:
                print "I don't understand what you want me to do. Type 'help' for information on terminal input commands."
        lock.release()
        for s in inready:       # loop through all of the inputready items
            try:                                # peek at it, check if its only \r or \n
                data = con.recv(SIZE, socket.MSG_PEEK)
                newdata = data.replace('\n','').replace('\r','')
                if len(data) > 0 and newdata == '':
                    data = s.recv(len(data))    # pull them from the data stream if so
                elif newdata:                   # otherwise, use the regular expressions to check for proper messages
                    data = data.replace('\n',' ').replace('\r',' ')
                    peek = RE_CHKMSG.match(data)
                    throwaway = RE_CHKLEN.match(data)
                    if throwaway and throwaway.group("len"):        # if throwaway, we had some message before a [
                        data = s.recv(len(throwaway.group("len")))  # and throw that data away
                    elif peek and peek.group("chk"):                # if not, then we found a ]
                        data = s.recv(len(peek.group("chk")))       # so pull it out and use the RE to get proper data
                        newdata = data.replace('\n','').replace('\r','')
                        line = RE_GENMSG.match(data)
                        if line:                                    # and parse it if we had something proper
                            parse_server(line.group("cmd"), line.group("msg"))
                    elif len(newdata) > 1024:                       # if we've gotten 1024 characters, and no [], then throw it all away
                        data = s.recv(SIZE)
                elif not data:                                      # if nothing was sent, we were hung up on
                    keepalive = 0
                    print "Server closed connection."
            except socket.error:                                    # exception means we were disconnected
                print "Server closed connection."
                keepalive = 0
        if state == 1 and auto == True:                                 # state 1 implies it is this client's turn
            auto_Play(con)                                              # auto play if we're on auto, then set state to waiting for confirmation
            state = 2
        elif state == 3 and auto == True:                               # state 3 implies the server asked for a swap card to give to scumbag
            auto_Swap(con)                                              # auto implies we're in auto mode, so auto play, then set state to waiting for confirmation
            state = 4
    con.close()

# Function for new thread for reading input.
def input_thread(inQueue, lock):
    while True:
        line = sys.stdin.readline()
        lock.acquire()
        inQueue.put(line)
        lock.release()

# method that automatically generates a play that won't send a strike to the client
def auto_Play(con):
    global keepalive
    numcards = len(Table.myhand)                # number of cards the client has
    if numcards == 0:                           # if 0, we can't actually do anything, so we'll take a timeout strike
        return
    Table.myhand.sort()                         # sort hand for ease of dealing with is
    tomatch = len(Table.inplay)                 # number of cards we need to play
    Table.inplay.sort()                         # ensure the cards in play are sorted
    playmsg = ""                                # start building the playmsg
    if tomatch == 0:                            # start of a round, we can chose anything, go for the lowest card
        playmsg = "[cplay|%02d,52,52,52]" %Table.myhand[0].get_cardno()
        Table.myhand.remove(Table.myhand[0])
    else:                                       # otherwise build a play array
        play = []
        tomatch -= 1                            # take this down one so we can match cards easier
        for ix in range(0,numcards-tomatch):    # we want tomatch cards (from above), so if 2 cards, we match index, index + 1, i.e. 2 cards
            if Table.myhand[ix].value == Table.myhand[ix+tomatch].value and Table.myhand[ix].value >= Table.inplay[0].value:
                for newindex in range(0,tomatch + 1): # we found enough cards to match (works since they were sorted)
                    play.append(Table.myhand[ix].get_cardno()) # append their values to play, and remove them from my hand, then break the loop
                    Table.myhand.remove(Table.myhand[ix])
                break
        while len(play) < 4:                    # we need to transmit 4 cards, so pad it with 52's
            play.append(52)
        play.sort()                             # sort it so it looks pretty, don't think this is really necessary, but w/e.
        playmsg = "[cplay|%02d,%02d,%02d,%02d]" %(play[0],play[1],play[2],play[3]) # compose play msg
    try:
        con.send(playmsg+"\n")                  # try to send the message
    except socket.error:
        print "Unable to send play message. Terminating."
        keepalive = 0

# method for automatically swapping a card
def auto_Swap(con):
    global keepalive
    numcards = len(Table.myhand)    # ensure we have a card to give
    if numcards == 0:
        return
    Table.myhand.sort()             # sort so we can just give them the lowest card we have
    swapmsg = "[cswap|%02d]" %Table.myhand[0].get_cardno()
    Table.myhand.remove(Table.myhand[0])
    try:                            # attempt to send the message
        con.send(swapmsg + '\n')
    except:
        print "Unable to send swap message. Terminating."
        keepalive = 0

'''
Below are functions dealing with client input/gameplay.
'''

# method to parse the input line
def parse_line(cmd,msg,con):
    global keepalive
    if cmd == 'q' or cmd == 'quit':
        print "Goodbye."
        keepalive = 0
    elif cmd == 't' or cmd == 'table':
        Table.print_Table()
    elif cmd == 'h' or cmd == 'help':
        helpmsg()
    elif cmd == 'c' or cmd == 'chat':
        send_Chat(con,msg)
    elif cmd == 'p' or cmd == 'play':
        send_play(con,msg)
    elif cmd == 's' or cmd == 'swap':
        send_swap(con,msg)
    else:
        print "I don't understand what you want me to do.  Type 'help' for information on terminal input commands."

# method to send a chat msg to the server
def send_Chat(con, msg):
    global keepalive
    outmsg = msg.strip()        # strip the message of whitespace, it isn't necessary
    outmsg_overflow = ""        # have an extra string incase we have too much to send in one
    if len(outmsg) > 63:        # if greater than 63, split it up by spaces
        outarray = outmsg.split()
        outmsg = outarray[0]    # set outmsg to the first item in outarray
        ix = 1
        while ix < len(outarray): # loop through the rest of the items in the array
            if (len(outmsg) + 1 + len(outarray[ix])) <= 63:
                outmsg += ' ' + outarray[ix]
                ix += 1         # shove stuff into outmsg until the next item would push it past 63 chars
            else:
                break           # break if it would
        while ix < len(outarray): # then shove the rest into overflow
            outmsg_overflow += ' ' + outarray[ix]
            ix += 1
    outmsg = outmsg + PAD[0:63-len(outmsg)] # pad the message out to 63
    try:                        # send the msg
        con.send("[cchat|%s]\n" %outmsg)
    except socket.error:
        print "Unable to send chat message to server."
        keepalive = 0
    if outmsg_overflow != "":   # recursively call ourselves if we still have stuff to send
        send_Chat(con, outmsg_overflow)

# method to send a play message to the server
def send_play(con, msg):
    global keepalive
    global auto
    if auto == True:                # we don't want people screwing around when we're in auto mode
        print "Auto mode enabled. Unable to give manual commands in this mode."
        return
    cardstoplay = check_Cards(msg)  # check the message for cards
    playmsg = "[cplay|"             # start building the play msg
    for eachcard in cardstoplay:    # add all of the cards to the play msg
        playmsg += "%02d," %eachcard.get_cardno()
    while len(playmsg) < 19:        # if the length isn't long enough, pad it with 52's
        playmsg += "52,"
    playmsg = playmsg[0:len(playmsg)-1] +']'
    try:                            # finish it off by removing the last , and adding a ] then sending it out
        con.send("%s\n" %playmsg)
    except socket.error:
        print "Unable to send message to server. Terminating."
        keepalive = 0

# method for checking a msg string for cards and returning an array with the cards found
def check_Cards(msg):
    playarray = []                      # set an empty array for the cards
    playcards = RE_PLAYM.findall(msg)   # find all the matching substrings
    nosuit = []                         # create an array of those cards without suits
    for card in playcards:              # go through all of the cards, get their facevalue and suitvalue by calling card_Values
        faceval, suitval = card_Values(card[0],card[1])
        if faceval != 13:               # 13 indicates it wasn't a proper value
            if suitval != -1:           # -1 indicates the suit wasn't given
                cardval = (faceval * 4) + suitval   # calculate the card value
                for excard in Table.myhand:         # check all of the cards you have and remove the card if you have it
                    if faceval == excard.value and suitval == excard.suit:
                        Table.myhand.remove(excard)
                        break
                playarray.append(Card(cardval))     # add the card to the play
            else:
                nosuit.append(faceval)              # the suit wasn't specified, so we'll look at it after we've pulled all of the suit specific cards you said
    for suitlesscard in nosuit:         # check all of the non suit specific cards given
        found = False                   # keep track of whether or not we've found the card in our hand
        for card in Table.myhand:
            if suitlesscard == card.value:          # if we find a matching value, remove that card, and append the card, set found to true and break the inner loop
                Table.myhand.remove(card)
                playarray.append(card)
                found = True
                break
        if found == False:              # if we didn't find what we wanted, append a card with the clubs suit to the array
            playarray.append(Card(suitlesscard*4))
    if len(playarray) == 0:             # take note if they don't specity anything
        print "No 'cards' specified."
    return playarray                    # return what was built, or an empty array

# method that returns integers related to strings representing facevalues and suitvalues
def card_Values(face,suit):
    faceval = 13
    if face == '3' or face == '4' or face == '5' or face == '6' or face == '7' or face == '8' or face == '9' or face == '10':
        faceval = (int(face) - 3)
    elif face == 'J' or face == 'j':
        faceval = 8
    elif face == 'Q' or face == 'q':
        faceval = 9
    elif face == 'K' or face == 'k':
        faceval = 10
    elif face == 'A' or face == 'a':
        faceval = 11
    elif face == '2':
        faceval = 12
    suitval = -1
    if suit == 'c' or suit == 'C':
        suitval = 0
    elif suit == 'd' or suit == 'D':
        suitval = 1
    elif suit == 'h' or suit == 'H':
        suitval = 2
    elif suit == 's' or suit == 'S':
        suitval = 3
    return (faceval,suitval)

# sends the [chand] message to the server. might not be supported on all, so this code is useless atm
def send_hand(con, msg):
    global keepalive
    try:
        con.send("[chand]\n")
    except socket.error:
        print "Unable to send request for hand message to server."
        keepalive = 0

# prints out the help message detailing how to play the game via std in.
def helpmsg():
    print "Typing 'c' or 'chat' followed by a message will send the message to everyone connected to the server."
    print "Typing 'q' or 'quit' will exit the game."
    print "Typing 'h' or 'help' will bring up this message."
    print "Typing 's' or 'swap' followed by a number from 2-10 or A, J, K, Q, then the suit C, S, D, H will indicate to the server what card you wish to give to the scumbag."
    print "Typing 'p' or 'play' followed by a number from 2-10 or A, J, K, Q, then the suit C, S, D, H will indicate to the server the cards you wish to play."
    print "Both the play and swap message do not require the suit, if you have a card of the value indicated it will be found and transmitted.  If you do not it assumes the suit is clubs."
    print "Both of these messages will transmit a card that you do not have."

# method that sends the swap message when given a msg of cards to send
def send_swap(con, msg):
    global keepalive
    swapcard = check_Cards(msg)     # check the msg for cards
    swapmsg = "[cswap|"
    if len(swapcard) == 0:          # if no cards specified, add a 52
        swapmsg += "%02d]" %52
    else:                           # otherwise add every single card to a comma separated list... this is improper, but whatever, its a dumb client
        for card in swapcard:
            swapmsg += "%02d," %card.get_cardno()
        swapmsg = swapmsg[0:len(swapmsg)-1] + ']'
    try:
        con.send(swapmsg + "\n")
    except socket.error:
        print "Unable to send swap message to server."
        keepalive = 0

'''
Below are functions dealing with server messages.
'''

# parses the cmd and msg field from what the server sends the client
def parse_server(cmd,msg):
    if cmd == "sjoin":
        s_join(msg)
    elif cmd == "strik":
        s_trik(msg)
    elif cmd == "schat":
        s_chat(msg)
    elif cmd == "stabl":
        s_tabl(msg)
    elif cmd == "swapw":
        s_wapw(msg)
    elif cmd == "swaps":
        s_waps(msg)
    elif cmd == "slobb":
        s_lobb(msg)
    elif cmd == "shand":
        s_hand(msg)
    else:
        print "Server sent unknown message type."

# method to deal with the join message from a server
def s_join(msg):
    global keepalive
    if len(msg) > 8:    # if the name is longer than 8 throw hands in air and go "OMGIDON'TKNOWHWATSGOINGON" then exit the program
        print "Server sent you a name that's too long! Oh the horror."
        keepalive = 0   # but seriously, we need to know our name, and it MUST be 8 chars or everything is wonky, so we quit here
        return
    Table.myname = msg  # otherwise set the name and tell the client we joined successfully
    print "Joined server successfully. Your name is %s." %Table.myname.strip()

# method for dealing with strike messages from the server
def s_trik(msg):
    global state
    if len(msg) != 4:   # wrong length....
        print "Server sent some weird message masquerading as a strike message."
        return          # figure out what the message was
    strikemsg = RE_STRIK.match(msg)
    if not strikemsg:   # uhoh, something went wrong with the server, quick shock it!
        print "Server sent some weird message that was very close to a strike message."
        return
    else:               # oh hey, they're not as dumb as we thought
        scode = int(strikemsg.group("code"))    # check what the code is
        if (scode / 10) == 7 and state == 4:    # if its a 70-79 message it deals with swap, so we need to send a new one
            state = 3
        if (scode / 10) == 1 and state == 2:    # if its a 10-19 message it deals with play, so we need to send a new one
            state = 1
        Table.mystrike = int(strikemsg.group("num"))  # the number of strikes we've received
        errmsg = ''                             # get the error string
        for error in errors.ST.errlist:
            if error.value == scode:
                errmsg = error.message
                break                           # then output it to the user
        print "You received strike number %d because: %s" %(Table.mystrike, errmsg)

# method for dealing with when the server sends you a chat message
def s_chat(msg):
    chat = RE_SCHAT.match(msg)                  # use RE to get the name and msg
    outname = "Unknown"                         # we use Unknown as default name for outputting
    if not chat:                                # RE couldn't match it
        print "Server decided to send an improperly formatted chat message."
        return
    if chat.group("name"):                      # if it found a name, update the name, then strip it of whitespace for nice looks
        outname = chat.group("name")
        outname = outname.strip()
    if chat.group("msg"):                       # then get the msg if it exists and print it to the screen in a nice format
        print "%s: %s" %(outname,chat.group("msg").strip())
    else:                                       # otherwise tell the user we couldn't find the message
        print "Something went wrong, the server send a chat message without a message..."

# method for dealing with the stabl message from the server
def s_tabl(msg):
    global state
    if len(msg) != 118:
        print "Server sent some random crap for a table message that isn't the right length."
        return
    tablemsg = RE_STABL.match(msg)              # use a RE to match the message, this RE is the big ugly one above...
    if not tablemsg:
        print "Server sent some random crap for a table message that happened to be the right length."
        return
    else:
        state = 0                               # we default to state 0 (waiting for the server to tell me I need to do something)
        Table.player[1] = Player(tablemsg.group("p1n"),convstat(tablemsg.group("p1s")),int(tablemsg.group("p1c")))
        Table.player[2] = Player(tablemsg.group("p2n"),convstat(tablemsg.group("p2s")),int(tablemsg.group("p2c")))
        Table.player[3] = Player(tablemsg.group("p3n"),convstat(tablemsg.group("p3s")),int(tablemsg.group("p3c")))
        Table.player[4] = Player(tablemsg.group("p4n"),convstat(tablemsg.group("p4s")),int(tablemsg.group("p4c")))
        Table.player[5] = Player(tablemsg.group("p5n"),convstat(tablemsg.group("p5s")),int(tablemsg.group("p5c")))
        Table.player[6] = Player(tablemsg.group("p6n"),convstat(tablemsg.group("p6s")),int(tablemsg.group("p6c")))
        Table.player[7] = Player(tablemsg.group("p7n"),convstat(tablemsg.group("p7s")),int(tablemsg.group("p7c")))
        if Table.player[1].name == Table.myname:    # we set up each player based upon what we get from the RE
            Table.mychair = 1
        elif Table.player[2].name == Table.myname:  # then check the name of the players to determine if this client
            Table.mychair = 2
        elif Table.player[3].name == Table.myname:  # is in a specific chair, set it to the correct number if so
            Table.mychair = 3
        elif Table.player[4].name == Table.myname:
            Table.mychair = 4
        elif Table.player[5].name == Table.myname:
            Table.mychair = 5
        elif Table.player[6].name == Table.myname:
            Table.mychair = 6
        elif Table.player[7].name == Table.myname:
            Table.mychair = 7
        else:                                       # otherwise we're not at the table (0)
            Table.mychair = 0

        if Table.mychair != 0 and Table.player[Table.mychair].status == 0:
            state = 1                               # if my spot is set to active, we need to change our state to waiting for play
        else:                                       # otherwise we're twiddling our thumbs
            state = 0

        Table.inplay = []                           # initialize the card array on the table dealing with cards played is empty
        card1 = int(tablemsg.group("c1"))           # then set any card not = 52 into the array
        if card1 != 52:
            newcard = Card(card1)
            Table.inplay.append(newcard)
        card2 = int(tablemsg.group("c2"))
        if card2 != 52:
            newcard = Card(card2)
            Table.inplay.append(newcard)
        card3 = int(tablemsg.group("c3"))
        if card3 != 52:
            newcard = Card(card3)
            Table.inplay.append(newcard)
        card4 = int(tablemsg.group("c4"))
        if card4 != 52:
            newcard = Card(card4)
            Table.inplay.append(newcard)
        Table.inplay.sort()                         # sort it and then update whether or not the table is ranked
        Table.notranked = 1 if tablemsg.group("r") == '1' else 0
        Table.print_Table()                         # print out the table so the user can do what needs to be done

# method that returns a value depending on the character input, used for getting the status of a player
# when the server sends an stabl message
def convstat(stat):
    if stat == 'a' or stat == 'A':
        return 0
    elif stat == 'p' or stat == 'P':
        return 1
    elif stat == 'w' or stat == 'W':
        return 2
    elif stat == 'd' or stat == 'D':
        return 3
    elif stat == 'e' or stat == 'E':
        return 4
    else:
        return 5

# method for dealing with the warlord swap message from the server
def s_wapw(msg):
    global state
    if len(msg) != 2:
        print "Something went wrong with the server's warlord swap message."
        return
    try:
        newcard = int(msg)                          # figure out what card we were sent
    except:
        print "Server didn't send a real card for the warlord swap message."
        return                                      # we didn't get something we can work with so exit and take the timeout strike
    state = 3                                       # set state to waiting for swap msg
    ncard = Card(newcard)
    Table.myhand.append(ncard)                      # append the card to the hand we're working with then sort it
    Table.myhand.sort()
    print "Scumbag gave you (the warlord) the %s. It is demanding you give it a card to give the scumbag." %ncard.get_cardno()
    Table.print_Hand()                              # print the hand so the user knows what cards they can chose from

# method for dealing with the scumbad swap message from the server
def s_waps(msg):
    if len(msg) != 5:
        print "Something went wrong with the server's scumbag swap message."
        return
    swapmsg = RE_SWAPS.match(msg)               # use the RE to figure out if what we got was good
    if not swapmsg:
        print "The message was the right size but wrong format for a scumbag swap message."
        return
    ncard = Card(int(swapmsg.group("ncard")))   # new card
    lcard = Card(int(swapmsg.group("lcard")))   # card lost
    if ncard.value < 13:
        Table.myhand.append(ncard)
    print "You received the %s from the warlord.  You lost the %s." %(ncard.print_card(),lcard.print_card())

# method for dealing with the server's slobb message
def s_lobb(msg):
    try:                            # try to figure out how many people are in the lobby
        num_lobby = int(msg[0:2])
    except:
        print "Server sent an improper lobby message."
        return
    lobby = RE_SLOBB.findall(msg)   # find every name in the message
    if len(lobby) != num_lobby:     # if the numbers don't match report this
        #print num_lobby
        #print lobby
        print "Server sent too few/many names in the lobby message."
    else:                           # otherwise our lobby is the array of names
        Table.lobby = lobby

# method for dealing with the server's shand message
def s_hand(msg):
    handstr = RE_SHAND.findall(msg) # find every card in the string
    Table.myhand = []               # initialize the hand array to empty
    for string in handstr:          # then add every card to the array
        newcard = Card(int(string))
        if newcard.value < 13:
            Table.myhand.append(newcard)
    Table.myhand.sort()             # and sort it, because sorting is the best

if __name__ == '__main__':
    try:
        main()
    finally:
        pass

