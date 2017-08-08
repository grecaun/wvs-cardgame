# Warlords vs Scumbags (President)
[Warlords vs Scumbags is a very simple card game, usually played as a drinking game.  It may be more commonly known as President.][1]

## Table of Contents
  * [How to Play](#how-to-play)
    * [General Rules](#general-rules)
    * [Playing](#playing)
    * [Unranked vs Ranked](#unranked-vs-ranked)
  * [Downloads](#downloads)
  * [Running the Game](#running-the-game)
    * [CLI Versions](#cli-versions)
    * [GUI Versions](#gui-versions)
  * [Building Your Own](#building-your-own)
  * [Known Issues](#known-issues)

## How To Play
Let's establish some terminology I'll use that may differ from the above linked wikipedia page.

  * Player
    * Any individual (or AI) playing Warlords vs Scumbags.
  * Hand
    * A hand starts when everyone is dealt their hand and ends when everyone (or all but one) has run out of cards.
  * Round
    * A round starts when the table is clear (or at the start of a hand) and ends when a 2 is played, or everyone but the last person to play a card has passed after having an opportunity to play a card.
  * Game
    * A set of rounds.
  * Unranked Hand
    * The very first Hand you'll play in a game.
  * Ranked Hand
    * Any Hand following another Hand.  Has a Warlord (President) and Scumbag.
  * Table
    * Cards on the table may be considered the cards in play.  That is, these are the cards that must be matched/beat when presenting your next play.
  * Play
    * A set of cards you 'play' when it is your turn.
  * Lobby
    * During play with this server/client there is a 'lobby' where people who've connected while a round is being played are put.  They are sent chat messages and updates about how the game is going.
  * Face-value
    * When talking about a card, the face value is the number shown on the card.  See below for the rankings of the cards.

### General Rules

  * Ace is high, but 2s outrank Aces.  So the ordering of cards goes 3, 4, 5, 6, 7, 8, 9, 10, Jack, Queen, King, Ace, 2.
  * The only time a card's suit matters is at the start of an unranked hand where the player with the 3 of clubs goes first.
  * The Warlord (President) is the first person to run out of cards in the previous hand.
  * The Scumbag is the last person to run out of cards in the previous hand.
  * All games must have 3 or more players.
  * A game may have no more than 7 players.

### Playing
In an unranked hand the person with the 3 of clubs is the first person allowed to play a card and must play the 3 of clubs.  In a ranked round the Warlord is the first person to play a card.

When it is a person's turn to play cards, they must play an equal or greater number of cards with face value greater than or equal to those on the table.  That is, if there are two 3s on the table, you must play two, three, or four cards whose face value is either 3 or greater.  If there are no cards on the table, any number of cards with matching face values may be played.

A new round occurs any time a set of cards with face value of 2 is played, or if all players after the last person to play a set of cards pass after having an opportunity to play cards.  In both situations the last person to play a set of cards is presented with an empty table and a new round commences.

If at any point the number of cards and face values of cards played match exactly, the person who would normally be given a chance to play next, is automatically skipped.

### Unranked vs Ranked
There are two differences between a ranked hand and an unranked hand

  * At the start of an unranked hand the person with the 3 of clubs automatically plays the 3 of clubs.  In a ranked hand the Warlord may play any card.
  * Before a ranked hand starts a swap occurs between the Scumbag and the Warlord.  The Scumbag automatically gives his best card to the Warlord.  The Warlord must give a card (including the card just given) to the Scumbag.

## Downloads
There are multiple ways to play this game with the code in this repository.

  * [Java CLI Client][2]
  * [Java GUI Client][3]
  * [Java CLI Server][4]
  * [Java GUI Server][5]
  * [Python 2.7 CLI Client][6]
  * [Python 2.7 CLI Server][7]

The easiest way to play this is going to be the Java version with a GUI.  This version can launch a server and multiple AI's so you can play essentially by yourself.

The java version requires Java 8 which you can find [here][8].

You can find Python 2.7 for download [here][9].

## Running The Game

All CLI (command line interface) versions must be launched from a shell.  Powershell on Windows, or Bash on a Unix based system are recommended.

### CLI Versions

  * Server - [Python 2.7][7] - [Java][4]
    ```
    java -jar 'WvS ServCLI 0.1.jar' [-l LOBBYTIMEOUT] [-t PLAYTIMEOUT] [-m MINPLAYERS] [-s NUMSTRIKES] [-c MAXCLIENTS] [-h]
    ```
    ```
    python server.py [-l LOBBYTIMEOUT] [-t PLAYTIMEOUT] [-m MINPLAYERS] [-s NUMSTRIKES] [-c MAXCLIENTS] [-h]
    ```
    * -l LOBBYTIMEOUT  Sets the amount of time (in seconds) the server waits before starting a game once enough players are connected.
    * -t PLAYTIMEOUT   Sets the amount of time (in seconds) the server will wait for a player to send a play message once it is their term. (0 for no timeout)
    * -m MINPLAYERS    Sets the minimum number of players required to be connected to the server before a game will start. (Default 3)
    * -s NUMSTRIKES    Sets the number of strikes the server will give a client before disconnecting them. (Default 3)
    * -c MAXCLIENTS    Sets the maximum number of clients the server will allow to connect to it.
    * -h               Displays a help message detailing these options then exits.

  * Client - [Python 2.7][6] - [Java][2]
    ```
    java -jar 'WvS ClientCLI 0.1.jar' [-s SERVERNAME] [-p PORTNUMBER] [-n NAME] [-a DELAY] [-m] [-h]
    ```
    ```
    python client.py [-s SERVERNAME] [-p PORTNUMBER] [-n NAME] [-d DELAY] [-m] [-h]
    ```
    * -s SERVERNAME  Server IP Address / Domain Name. (Default localhost)
    * -p PORTNUMBER  Server port number. (Default 36789)
    * -n NAME        Sets the name you wish to use while playing. May be changed by server. Randomly chosen if none given.
    * -d DELAY       Sets a delay (in seconds) that the AI will wait before sending a play.
    * -m             Runs the client in manual mode instead of auto.
    * -h             Displays a help message which details these options then exits.

### GUI Versions
#### [Client GUI][3]
Options for connecting to a server in the Client version are found under File->Settings.  Changing these settings while connected to a server will not disconnect you, but will affect any AI you try to spin up.

Server options are found under Server->Options.  Changing server settings while a server is running *should* change the settings the server is using.

You can disconnect and reconnect at any point in time.  You can start AI clients and close them all from the menu, you can even list them and close them individually.

#### [Server GUI][5]
Options for the server can be updated at any point in time.  On the left is a list of connected clients which can be closed individually. On the right is a log of people connecting, hands being dealt, when people run out of cards, etc.

## Building Your Own
I'll be updating this later with information on how to build upon or utilize this code for yourself.

## Known issues
  * Server list of clients doesn't properly update when the client disconnects themself.
  * Client list of AI's doesn't update if an AI is forcibly disconnected or disconnects without being told to do so via the client.  Reopening updates the list properly.



 [1]: https://en.wikipedia.org/wiki/President_(card_game)
 [2]: https://github.com/grecaun/wvs-cardgame/raw/master/jar/executable/WvS%20ClientCLI%200.1.jar
 [3]: https://github.com/grecaun/wvs-cardgame/raw/master/jar/executable/WvS%200.1.jar
 [4]: https://github.com/grecaun/wvs-cardgame/raw/master/jar/executable/WvS%20ServCLI%200.1.jar
 [5]: https://github.com/grecaun/wvs-cardgame/raw/master/jar/executable/WvS%20Server%200.1.jar
 [6]: https://github.com/grecaun/wvs-cardgame/raw/master/python/python-client.zip
 [7]: https://github.com/grecaun/wvs-cardgame/raw/master/python/python-server.zip
 [8]: https://java.com/en/download/manual.jsp
 [9]: https://www.python.org/download/releases/2.7/