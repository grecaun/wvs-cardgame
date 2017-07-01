'''
WWU CSCI 367 Fall 2013

Client code.

@author: James Sentinella
'''

# class for dealing with cards and objects, each contains a value and a suit
class Card:
	value = 0
	suit = 0

	def __init__(self,integer):
		self.value = integer / 4
		self.suit = integer % 4

	# method for dealing with whether or not a card is less than another
	def __lt__(self,other):
		return self.value < other.value

	# method for 'printing' a card, i.e. writing it to a string and returning said string
	def print_card(self):
		suit = ""
		if self.suit == 0:
			suit = "clubs"
		elif self.suit == 1:
			suit = "diamonds"
		elif self.suit == 2:
			suit = "hearts"
		else:
			suit = "spades"
		if self.value == 0:
			value = "3"
		elif self.value == 1:
			value = "4"
		elif self.value == 2:
			value = "5"
		elif self.value == 3:
			value = "6"
		elif self.value == 4:
			value = "7"
		elif self.value == 5:
			value = "8"
		elif self.value == 6:
			value = "9"
		elif self.value == 7:
			value = "10"
		elif self.value == 8:
			value = "J"
		elif self.value == 9:
			value = "Q"
		elif self.value == 10:
			value = "K"
		elif self.value == 11:
			value = "A"
		elif self.value == 12:
			value = "2"
		else:
			value = "0"
		return "%s of %s" %(value, suit)

	# method for returning the cardvalue (or number) of a card for purposes of transmission
	def get_cardno(self):
		return (self.value * 4) + self.suit

# class for dealing with each player as an object, each has a name, a status, and somenumber of cards
class Player:
	name = ''
	status = 0
	numcards = 0

	def __init__(self,name,stat,cards):
		self.name = name
		self.status = stat
		self.numcards = cards

# class for dealing with the table as an object, it has players, cards that are in play, etc
class Table:
	myname   = ""		# name the server assigned the client
	mychair  = 0		# chair number, 0 indicates in lobby
	myhand   = []		# hand of cards
	mystrike = 0		# number of strikes the client has received

	notranked = 0		# whether or not the hand is ranked

	inplay   = []		# the cards currently in play
	player   = [None,None,None,None,None,None,None,None]
	lobby    = []		# the lobby

	# method for outputting the table status to the player
	@staticmethod
	def print_Table():
		for index in range(1,8):		# outputs every player but the client
			if Table.player[index] and Table.player[index].status < 4 and index != Table.mychair:
				newstr = ""
				if Table.player[index].status == 0:
				 	newstr = " It is currently their turn."
				elif Table.player[index].status == 1:
					newstr = " They passed or were skipped last time around."
				elif Table.player[index].status == 2:
					newstr = " They are waiting for their turn."
				elif Table.player[index].status == 3:
					newstr = " They are no longer connected."
				print "Player %d is %s, who has %d cards left.%s" %(index, Table.player[index].name.strip(), Table.player[index].numcards, newstr)

		if Table.mychair != 0 and Table.player[Table.mychair]:
			newstr = ""
			if Table.player[Table.mychair].status == 0:
			 	newstr = " It is your turn."
			elif Table.player[Table.mychair].status == 1:
				newstr = " You passed or were skipped."
			elif Table.player[Table.mychair].status == 2:
				newstr = " You are waiting for your turn."
			elif Table.player[Table.mychair].status == 3:
				newstr = " You are apparently no longer connected."
			print "You are player %d.%s" %(Table.mychair, newstr)
		outstr = "These cards are on the table: "
		for card in Table.inplay:
			outstr += "%s, " %card.print_card()
		print outstr[0:len(outstr)-2]
		Table.print_Hand()
		
	# method for outputting the player's hand so that they know what cards they have
	@staticmethod
	def print_Hand():
		outstr = ""
		if len(Table.myhand) == 0:
			return
		for card in Table.myhand:
			if card.value < 13:
				outstr += "%s, " %card.print_card()
		print "Your cards are: %s" %outstr[0:len(outstr)-2]