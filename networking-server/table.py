'''
WWU CSCI 367 Fall 2013

Server code.

@author: James Sentinella
'''

import collections
import random
import time
import socket
from globalvals import gvals, D_STATE

class Table:
    # Information about the table.
    curplayer  = 0              # number of the current player
    numcards   = 0              # number of cards on the table
    tablecards = [52,52,52,52]  # cards on the table, including 52's indicating nul

    numplayers = 0              # number of alive players at the table, includes those who've finished
    notranked  = 1              # indicator of whether or not the round is ranked
    wcard      = 0              # placeholder for the card the warlord receives during swap

    # Array of players at the table, their hands, and their pass value
    player = [None,None,None,None,None,None,None,None]
    handof = [[],[],[],[],[],[],[],[]]
    passof = [0,0,0,0,0,0,0,0]

    # Queue for the players who've finished their hand
    finished = collections.deque('',9)

    # method to check a play (array of integers) to see if its a valid play
    @staticmethod
    def checkPlay(Play):
        if Table.curplayer not in range(1,8):       # if the current player isn't in the proper range something went wrong...
            return 99
        curhand = Table.handof[Table.curplayer]     # lets use a local copy of the array for checking, less typing
        curcards = 0                                # keep count of proper cards in play array
        Table.tablecards.sort()                     # sort the tablecards to ensure we do things properly
        match = False                               # match is for indicating a perfect match (count and card values match)
        Play.sort()
        for card in range(0,4):                     # there can only be 4 cards in a play, so from 0 to 3
            if Play[card] != 52:                    # 52 is placeholder for nothing
                curcards += 1
                if Play[card] not in curhand:       # card not in hand, error code = 14
                    return 14
                elif card > 0:                      # check the card against all of the other cards before it
                    for verify in range (0, card):
                        if Play[card] == Play[verify]:  # duplicate card
                            return 17
                        if Play[card]/4 != Play[verify]/4: # values don't match
                            return 11
                if Play[card]/4 < Table.tablecards[0]/4 and Table.tablecards[0] != 52: # card value less than card on table
                    return 12
            if Play[card]/4 == Table.tablecards[0]/4 and Table.tablecards[0] != 52: # card values match
                match = True
        if curcards == 0:                                   # indicates a pass
            if Table.numcards == 0:                         # can't pass if you start the round
                return 18
            else:
                return -1
        elif match == True and curcards == Table.numcards:  # full match
            return -2
        elif curcards < Table.numcards:                     # too few cards provided
            return 13
        elif Play[0]/4 == 12:                               # 2 was played
            return -3
        return 0                                            # play is valid

    # Method that starts a new hand.
    @staticmethod
    def newhand():
        print "Arranging the table."
        print "----------------------------------"
        Table.arrangetable()
        if (Table.numplayers < gvals.minplay):  # if after arranging the table we have too few people to play
            print "Insufficient players. Returning players to lobby until more clients join."
            print "----------------------------------"
            Table.insuffplayers()
            return
        print "Setting up cards on table."
        print "----------------------------------"
        Table.numcards = 0
        Table.tablecards = [52,52,52,52]
        print "Shuffling deck."
        print "----------------------------------"
        if (Table.shuffle() != 0):
            print "Something went wrong with the table shuffle."
            print "----------------------------------"
            Table.insuffplayers()
            return
        if (Table.notranked == 0):              # swap cards if ranked round (i.e. not ranked is 0)
            print "Performing card swap."
            print "----------------------------------"
            Table.swap()
        else:                                   # otherwise start the game
            print "Starting unranked game. Player with three of clubs goes first."
            print "----------------------------------"
            Table.StartGame()

    # method for ensuring the Table knows the correct number of players at the table
    @staticmethod
    def checkPlayers():
        numplayers = 0
        for player in range(0,8):       # pass value of 2 indicates disconnected player
            if Table.player[player] and Table.passof[player] != 2:
                numplayers += 1
        Table.numplayers = numplayers

    # method for starting the game, i.e. after the table is set up, call this to start play
    @staticmethod
    def StartGame():
        from server import broadcastlobby
        gvals.state = D_STATE.WAITPLAY                  # set state to waiting for a play
        if Table.notranked == 1:                        # check for ranked, if ranked, find the zombie
            for zombie in range(1,8):                   # or rather, the person with a 3 of clubs who must play it first
                if 0 in Table.handof[zombie]:
                    Table.curplayer = zombie
                    Table.getNextPlayer()               # force him to play it, get next player, set table cards, number of cards,
                    Table.tablecards = [0,52,52,52]     # and remove the 3 of clubs from the zombie's hand
                    Table.numcards = 1
                    Table.handof[zombie].remove(0)
        for someone in range(1,8):                      # tell everyone what cards they have
            if Table.player[someone]:
                print Table.player[someone].name + " has these cards: " + str(Table.handof[someone])
                Table.sendhand(Table.player[someone].socket,Table.handof[someone])
        print "----------------------------------"
        Table.Play()                                    # indicate you're waiting for a play
        broadcastlobby(None,True)                       # broadcast the lobby

    # method for the server to indicate that it is waiting for a play
    @staticmethod
    def Play():
        from server import close
        if len(Table.handof[Table.curplayer]) == 0:     # check if the current player has cards
            Table.getNextPlayer()                       # get next player if not
        if Table.oneLeft():                             # check if there's only one player with cards/alive at the table
            Table.addLast()                             # add him to finished if so, set state to NEWGAME and let the server main loop handle it from there
            gvals.state = D_STATE.NEWGAME
            return
        Table.checkStatus()                             # otherwise, check the status of the table (if everyone else passed, i.e. new round is necessary)
        tablestring = Table.tableStr()                  # prepare the string for broadcasting the table message
        for c in gvals.clientlist:                      # broadcast it to everyone still connected
            try:
                c.socket.send(tablestring + '\n')
            except socket.error:
                close(c.socket)
        gvals.to_time = time.time()                     # update time for timeout value

    # method that sets Table.curplayer to the next player (i.e. they have cards, aren't dc'ed, and the spot isn't empty)
    @staticmethod
    def getNextPlayer():
        oldplayer = Table.curplayer                     # keep track of where we left from
        Table.curplayer += 1
        for i in range(0,11):                           # ensure that we check everyone
            if Table.curplayer > 7:
                Table.curplayer = Table.curplayer % 7   # if we're out of range, loop back to the start, 8->1, 9->2, etc
            elif Table.curplayer > 0 and Table.player[Table.curplayer] and len(Table.handof[Table.curplayer]) > 0 and Table.passof[Table.curplayer] != 2:
                return                                  # if between 1 and 7, the player exists, their hand has 1 or more cards, and they're not dc'ed, we've got something!
            elif oldplayer == Table.curplayer:
                gvals.state = D_STATE.NEWGAME           # set state to NEWGAME state, because they're the only one who can play
                return
            else:                                       # otherwise we need to look at next
                Table.curplayer += 1

    # method that sends the empty table string to a client
    @staticmethod
    def sendEmptyTable(client):
        from server import close
        stablstr = "[stabl|e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00,e0:        :00|52,52,52,52|0]"
        try:
            client.socket.send(stablstr)
        except socket.error:
            close(client.socket)

    # method that returns the table string to be sent to clients that reflects the current state of the table
    @staticmethod
    def tableStr():
        stablstr = "[stabl|"
        for pnum in range(1,7):                                             # loop through all the players except 7
            if Table.player[pnum]:                                          # check for a player existing, then set their status
                if Table.curplayer == pnum:
                    stablstr = stablstr + "a"
                elif Table.passof[pnum] == 1:
                    stablstr = stablstr + "p"
                elif Table.passof[pnum] == 2:
                    stablstr = stablstr + "d"
                else:
                    stablstr = stablstr + "w"
                stablstr = stablstr + "%01d:" %Table.player[pnum].strikes   # then their number of strikes, name, then cards in hand
                stablstr = stablstr + Table.player[pnum].name + ":%02d," %len(Table.handof[pnum])
            else:                                                           # if there was no one there set it to empty
                stablstr = stablstr + "e0:        :00,"
        if Table.player[7]:                                                 # do the same for 7 minus the last comma, its | here
            if Table.curplayer == 7:
                stablstr = stablstr + "a"
            elif Table.passof[7] == 1:
                stablstr = stablstr + "p"
            elif Table.passof[7] == 2:
                stablstr = stablstr + "d"
            else:
                stablstr = stablstr + "w"
            stablstr = stablstr + "%01d:" %Table.player[7].strikes
            stablstr = stablstr + Table.player[7].name + ":%02d|" %len(Table.handof[7])
        else:
            stablstr = stablstr + "e0:        :00|"                         # then set the cards on the table, then whether or not the table is ranked
        stablstr = stablstr + "%02d,%02d,%02d,%02d|" %(Table.tablecards[0],Table.tablecards[1],Table.tablecards[2],Table.tablecards[3])
        stablstr = stablstr + "%01d]\n" %Table.notranked
        return stablstr

    # method to add the last player to the finished deque
    @staticmethod
    def addLast():
        count = 0                                               # count the number of people we add
        for player in range(1,8):                               # go through all 7 players, if they're there, and they have cards
            if Table.passof[player] != 2 and Table.passof[player] != 3 and len(Table.handof[player]) != 0:
                Table.finished.appendleft(Table.player[player]) # add em to the deque
                print "%s added to finished list." %Table.player[player].name
                print "----------------------"
                count += 1                                      # keep count
        if count > 1:                                           # for error checking
            print "Something went wrong..."

    # method for checking if there's only one person left who can make plays at the table
    @staticmethod
    def oneLeft():
        count = 0                                               # count number of players who have cards
        for player in range(1,8):
            if Table.passof[player] != 2 and Table.passof[player] != 3 and len(Table.handof[player]) != 0:
                count += 1
        if count <= 1:                                          # if less than or equal to 1, we've found what we were looking for
            return True
        else:                                                   # otherwise nope
            return False

    # method to check if everyone other than the current player passed (forced or not) before this moment
    @staticmethod
    def checkStatus():
        allpassed = True                                        # set to true, if we debunk it thats okay
        for player in range(1,8):                               # check all of the players, if their pass is 0 (wait), they have cards, and they're not the current player
            if Table.passof[player] == 0 and len(Table.handof[player]) > 0 and Table.curplayer != player:
                allpassed = False                               # we've found someone who didn't pass
        if allpassed == True:
            Table.newround()                                    # if we found no one, then we start a new round

    # method for sending the swap card to the warlod
    @staticmethod
    def swap():
        Table.wcard = Table.handof[7].pop()                     # pull highest rank card from scumbad
        errval = Table.sendhand(Table.player[1].socket,Table.handof[1]) # attempt to send the hand to the warlord
        try:                                                    # attempt to send the swap message to the warlord
            Table.player[1].socket.send("[swapw|%02d]\n" %Table.wcard)
        except socket.error:
            close(Table.player[1].socket)
            errval = -1
        Table.handof[1].append(Table.wcard)                     # add to warlord's hand
        Table.handof[1].sort()                                  # keep it sorted
        Table.curplayer = 1                                     # we've got swap scenario, that means rank matters, so warlord goes first
        gvals.state = D_STATE.WTIMEOUT                          # warlord timeout state
        gvals.to_time = time.time()
        if errval == -1:                                        # unable to send message to warlord, so give the card back to the scumbag
            print "Unable to send swap message to warlord."
            print "----------------------------------"
            Table.handof[7].appendleft(Table.wcard)
            Table.getNextPlayer()                               # get the next player
            Table.StartGame()                                   # and start the game

    # method to send a hand to a specific connection, no guarantee that its the proper hand for a connection
    @staticmethod
    def sendhand(con,hand):
        from server import close
        handstr = "[shand|"                                     # build string, then try to send it off
        for i in hand:
            handstr = handstr + "%02d," %i
        while len(handstr) < 61:
            handstr += "52,"
        handstr = handstr[0:len(handstr)-1] + "]"
        try:
            con.send(handstr+"\n")
        except:
            close(con)
            return -1
        return 0

    # method to return any players at the table to the lobby in case we have too few clients to start a game
    @staticmethod
    def insuffplayers():
        if Table.player[7]:                     # if they exist, append them.  this puts them in the front of the queue
            gvals.lobby.append(Table.player[7])
            Table.player[7] = None
        if Table.player[6]:
            gvals.lobby.append(Table.player[6])
            Table.player[6] = None
        if Table.player[5]:
            gvals.lobby.append(Table.player[5])
            Table.player[5] = None
        if Table.player[4]:
            gvals.lobby.append(Table.player[4])
            Table.player[4] = None
        if Table.player[3]:
            gvals.lobby.append(Table.player[3])
            Table.player[3] = None
        if Table.player[2]:
            gvals.lobby.append(Table.player[2])
            Table.player[2] = None
        if Table.player[1]:
            gvals.lobby.append(Table.player[1])
            Table.player[1] = None
        Table.notranked = 1                     # next time we play it won't be ranked
        Table.numplayers = 0                    # there is no one there!
        Table.initvars()
        gvals.state = D_STATE.WAITMIN           # waiting for min players

    # method to initialize the table players, hands, and pass values to initial state,
    # also sends empty table to tell people that nothing is playing currently
    @staticmethod
    def initvars():
        # Array of players at the table.
        Table.player = [None,None,None,None,None,None,None,None]
        Table.handof = [[],[],[],[],[],[],[],[]]
        Table.passof = [0,0,0,0,0,0,0,0]
        for c in gvals.clientlist:
            Table.sendEmptyTable(c)

    # method to arrange the table, fill up slots if people to fill them, set up scumbag and whatnot
    @staticmethod
    def arrangetable():
        Table.initvars()                # initialize to empty state
        playernum = 0                   # start at 0 since we immediately increase by one at start of loop
        Table.numplayers = 0
        while len(Table.finished) != 0 and playernum < 7:
            playernum += 1              # go until we have 7 people, or there is no one in the finished deque
            Table.numplayers += 1
            Table.player[playernum] = Table.finished.pop()

        # setting up scumbag, switch 7 (scumbag) with the last person in the finished deque
        if (playernum != 1) and (playernum != 7):
            Table.player[7] = Table.player[playernum]
            Table.player[playernum] = None

        # fill up empty spots, go through all 7 spots
        for empty in range(1,8):
            if len(gvals.lobby) == 0:                   # if the lobby is empty we can exit this
                break
            elif Table.player[empty] == None:           # otherwise we check if someone is in the chair
                Table.player[empty] = gvals.lobby.pop() # if no one is, we pop the person from the lobby
                Table.numplayers += 1
        if not Table.player[7]:
            Table.notranked = 1
        if (Table.numplayers > 7):                      # error check
            print "Uh, things are getting out of hand here!"
            print "$$$$$$$$$$$$$$$$$$$$$$$$$$$"

    # method for starting a new round
    @staticmethod
    def newround():
        Table.numcards = 0                              # set cards to 0 on table
        Table.tablecards = [52,52,52,52]                # set cards to nul card values
        for player in range(1,8):                       # set pass values to 0 if the player exists and they're not dead
            if Table.passof[player] < 2 and Table.player[player]:
                Table.passof[player] = 0

    # method for shuffling the deck and assigning cards to players
    @staticmethod
    def shuffle():                                                      # initialize deck to be cards from 0 to 51
        deck = [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51]
        cardsleft = 51                                                  # set the number of cards left to be 51, this is because of indices and randint below
        while (cardsleft >= 0):                                         # loop until we've got no cards left (0 means one card left)
            errcheck = cardsleft
            for i in range (1,8):                                       # loop through every player
                if (Table.player[i] != None) and cardsleft >= 0:        # if the player is there, and we've got cards left
                    newcard = deck.pop(random.randint(0,cardsleft))     # give the player a random card
                    Table.handof[i].append(newcard)
                    cardsleft -= 1                                      # take note that we've got less cards
            if (errcheck == cardsleft):                                 # if we went through every player and didn't give a card out, we had a problem
                return 1
        for player in range(1,8):                                       # sort everyone's hands
            Table.handof[player].sort()
        return 0