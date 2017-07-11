'''
/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
'''

class errcode:			# errorcode object
	value = 00
	message = ''

	def __init__(self,value,message):
		self.value = value
		self.message = message

class ST:				# ST = STrike errorlist - add new codes if necessary
	errlist = []
	errlist.append(errcode(00,"Unknown error."))
	errlist.append(errcode(10,"Illegal play."))
	errlist.append(errcode(11,"Cards sent do not have matching face values."))
	errlist.append(errcode(12,"Face values of cards sent is too low."))
	errlist.append(errcode(13,"Quantity of cards sent is too few."))
	errlist.append(errcode(14,"Card not in player's hand."))
	errlist.append(errcode(15,"Out of turn play."))
	errlist.append(errcode(16,"Initial play of first hand must have 3 of clubs."))
	errlist.append(errcode(17,"Played duplicates."))
	errlist.append(errcode(18,"Pass on start."))
	errlist.append(errcode(20,"Timeout."))
	errlist.append(errcode(30,"Bad Message."))
	errlist.append(errcode(31,"Lobby player sending play message."))
	errlist.append(errcode(32,"Length exceeded."))
	errlist.append(errcode(33,"Unknown message type."))
	errlist.append(errcode(34,"Malformed message with known type."))
	errlist.append(errcode(60,"Chat flood."))
	errlist.append(errcode(70,"Illegal swap value."))
	errlist.append(errcode(71,"Illegal swap message."))
	errlist.append(errcode(72,"Swap message sent out of turn."))
	errlist.append(errcode(80,"Can't connect."))
	errlist.append(errcode(81,"Too many people already connected.  Lobby full."))