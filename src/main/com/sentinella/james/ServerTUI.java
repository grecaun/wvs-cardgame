package com.sentinella.james;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ServerTUI {
    private static boolean keepAlive = true;
    private static final LogBook log = new LogBook(3,true,"SERVERTUI");

    public static void main(String[] args) {
        Thread          serverThread;
        Server          theServer;

        int             lobbyTimeOut        = 15;
        int             playTimeOut         = 30;
        int             minPlayers          = 3;
        int             strikesAllowed      = 3;
        int             maxClients          = 35;
        int             port                = 36789;
        boolean         debug               = false;

        PrintStream debugStream = null;

        int             argc                = args.length;
        for (int i=0; i<argc; i++) {
            switch (args[i]) {
                case "-p":
                case "-P":
                    port = Integer.parseInt(args[++i]);
                    break;
                case "-l":
                case "-L":
                    lobbyTimeOut = Integer.parseInt(args[++i]);
                    break;
                case "-t":
                case "-T":
                    playTimeOut = Integer.parseInt(args[++i]);
                    break;
                case "-m":
                case "-M":
                    minPlayers = Integer.parseInt(args[++i]);
                    minPlayers = minPlayers > 7 ? 7 : minPlayers < 3 ? 3 : minPlayers;
                    break;
                case "-s":
                case "-S":
                    strikesAllowed = Integer.parseInt(args[++i]);
                    break;
                case "-c":
                case "-C":
                    maxClients = Integer.parseInt(args[++i]);
                    maxClients = maxClients > 90 ? 90 : maxClients < 3 ? 3 : maxClients;
                    break;
                case "-d":
                case "-D":
                    debug = true;
                    try {
                        debugStream = new PrintStream(new FileOutputStream(new File("debug_log.txt")));
                    } catch (FileNotFoundException e) {
                        log.printErrMsg("Unable to establish debug log file.");
                        return;
                    }
                    break;
                default:
                    log.printOutMsg("-p <portnumber> Sets the server's port number. Default is 36789.");
                    log.printOutMsg("-l <seconds> Sets the time (in seconds) the server will wait before starting a game once the minimum number of players are in the lobby. Default of 15.");
                    log.printOutMsg("-t <seconds> Sets the time (in seconds) the server will wait for a player to send a play when it becomes their turn. Default of 30. Set to 0 for no timeout.");
                    log.printOutMsg("-m <number> Sets the minimum number of players for a game to run.");
                    log.printOutMsg("-s <number> Sets the number of strikes given out before disconnecting a client. Default is 3. Set to 0 to never disconnect people.");
                    log.printOutMsg("-c <number> Sets the maximum number of clients allowed in the lobby. Default of 35.");
            }
        }

        try {
            // Create server object then thread.
            log.printOutMsg("Setting up server thread.");
            log.printDebMsg(String.format("%s ServerTUI.Main - Creating server", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),1);
            theServer = new Server(lobbyTimeOut, playTimeOut, minPlayers, strikesAllowed, maxClients, port);
            log.printDebMsg(String.format("%s ServerTUI.Main - Creating Thread",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            serverThread = new Thread(theServer);
            log.printDebMsg(String.format("%s ServerTUI.Main - Starting Thread",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            serverThread.start();
        } catch (Exception e) {
            System.err.println("Unable to establish server.");
            return;
        }
        log.printDebMsg(String.format("%s ServerTUI.Main - Setting up TUI Input Stream",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        log.printDebMsg(String.format("%s ServerTUI.Main - Setting up Callback",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        ClientCallback lifeLine = new ClientCallback() {
            @Override
            public void finished() {
                System.err.println("server thread no longer running.");
                ServerTUI.keepAlive = false;
            }

            @Override public void unableToConnect() {}

            @Override public void setOutConnection(ClientSocket out) {}
        };
        log.printDebMsg(String.format("%s ServerTUI.Main - Setting Callback in server Thread.",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        theServer.setUiThread(lifeLine);
        log.printDebMsg(String.format("%s ServerTUI.Main - Creating Matcher and Matching input lines.",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        Matcher matcher;
        String cmd;
        String inLine = "";
        log.printDebMsg(String.format("%s ServerTUI.Main - Starting Main Loop",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        while (keepAlive) {
            log.printDebMsg(String.format("%s ServerTUI.Main - Waiting for input commands",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),1);
            try {
                while (!userInput.ready() && keepAlive) Thread.sleep(1500);
                if (keepAlive) inLine = userInput.readLine();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            matcher = RegexPatterns.input.matcher(inLine);
            log.printDebMsg(String.format("%s ServerTUI.Main - Line Read - %s",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),inLine),2);
            if (matcher.find()) {
                cmd = matcher.group(1).toLowerCase();
                log.printDebMsg(String.format("%s ServerTUI.Main - Found Match - %s",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)),cmd),2);
                switch (cmd) {
                    case "quit":
                    case "q":
                        log.printDebMsg(String.format("%s ServerTUI.Main - Quitting",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
                        log.printOutMsg("Goodbye.");
                        theServer.quit();
                        keepAlive = false;
                        break;
                    case "help":
                    case "h":
                        printHelp();
                        break;
                    default:
                        log.printOutMsg("I don't quite understand that command. If you need help type 'help' for information on terminal input commands.");
                }
            }
        }
        log.printDebMsg(String.format("%s ServerTUI.Main - Exited Main Loop",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),1);
        try {
            log.printDebMsg(String.format("%s ServerTUI.Main - Joining server Thread",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.printDebMsg(String.format("%s ServerTUI.Main - Closing Down.",LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH))),2);
        log.printOutMsg("server now terminating.");
    }

    private static void printHelp() {
        log.printOutMsg("Typing 'q' or 'quit' will terminate the server.");
        log.printOutMsg("Typing 'h' or 'help' will bring up this message.");
    }
}
