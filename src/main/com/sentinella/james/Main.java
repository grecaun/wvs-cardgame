package com.sentinella.james;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.regex.Matcher;

public class Main {
    private static boolean keepAlive = true;

    public static void main(String[] args) {
        Client              theClient;
        MainWorker          myWorker;
        Table               clientTable;
        Lobby               clientLobby;

        Thread              clientThread;
        String      name  = null;
        String      host  = null;
        int         port  = 0;
        boolean     auto  = true;
        boolean     debug = false;
        int         argc  = args.length;
        int         delay = 0;
        for (int i=0; i<argc; i++) {
            switch (args[i]) {
                case "-s":
                case "-S":
                    host = args[++i];
                    break;
                case "-p":
                case "-P":
                    try {
                        port = Integer.parseInt(args[++i]);
                    } catch (Exception e) {
                        System.out.println("Unknown value given for as a port number.");
                    }
                    break;
                case "-n":
                case "-N":
                    name = args[++i];
                    break;
                case "-m":
                case "-M":
                    auto = false;
                    break;
                case "-d":
                case "-D":
                    debug = true;
                    break;
                case "a":
                case "A":
                    try {
                        delay = Integer.parseInt(args[++i]);
                    } catch (Exception e) {
                        System.out.println("Unknown value given for a delay.");
                    }
                    break;
                default:
                    System.out.println("-s <servername> Sets the server IP address.");
                    System.out.println("-p <portnumber> Sets the server's port number.");
                    System.out.println("-n <name> Sets the name you wish to use while playing. 8 character limit.");
                    System.out.println("-a <delay> Sets the time (seconds) the AI will wait before sending a play/swap message.");
                    System.out.println("-m Tells the client to run in manual mode instead of auto.");
            }
        }
        try {
            theClient     = new Client(host,port,name,auto);
            myWorker      = new MainWorker(theClient.getOutConnection(),debug);
            clientLobby   = theClient.getLobby();
            clientTable   = theClient.getTable();
            theClient.setDelay(delay);
            myWorker.setHand(theClient.getHand());
            clientThread  = new Thread(theClient);
            ClientCallback lifeLine = new ClientCallback() {
                @Override
                public void finished() {
                    System.out.println("Client thread no longer running.");
                    keepAlive = false;
                }

                @Override public void unableToConnect() {}

                @Override public void setOutConnection(PrintWriter out) {
                    myWorker.setOutConnection(out);
                }
            };
            theClient.setUiThread(lifeLine);
            theClient.setDebug(debug);
            clientThread.start();
        } catch (UnknownHostException e) {
            System.out.println("Unable to connect to server.");
            return;
        }
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        Matcher matcher;
        String cmd;
        String msg;
        String inLine = "";
        while (keepAlive) {
            try {
                while (!userInput.ready() && keepAlive) Thread.sleep(1500);
                if (keepAlive) inLine = userInput.readLine();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            matcher = RegexPatterns.input.matcher(inLine);
            if (matcher.find()) {
                cmd = matcher.group(1);
                msg = matcher.group(2);
                if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("q")) {
                    System.out.println("Goodbye.");
                    theClient.quit();
                    keepAlive = false;
                    break;
                } else if (cmd.equalsIgnoreCase("table") || cmd.equalsIgnoreCase("t")) {
                    clientTable.printTable(theClient.getName());
                } else if (cmd.equalsIgnoreCase("help") || cmd.equalsIgnoreCase("h")) {
                    printHelp();
                } else if (cmd.equalsIgnoreCase("chat") || cmd.equalsIgnoreCase("c")) {
                    myWorker.sendChat(msg);
                } else if (cmd.equalsIgnoreCase("play") || cmd.equalsIgnoreCase("p")) {
                    myWorker.sendPlay(msg);
                } else if (cmd.equalsIgnoreCase("swap") || cmd.equalsIgnoreCase("s")) {
                    myWorker.sendSwap(msg);
                } else if (cmd.equalsIgnoreCase("lobby") || cmd.equalsIgnoreCase("l")) {
                    System.out.println(clientLobby.getLobbyString());
                } else {
                    System.out.println("I don't quite understand that command. If you need help type 'help' for information on terminal input commands.");
                }
            }
        }
        try {
            clientThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("Typing 'c' or 'chat' followed by a message will send the message to everyone connected to the server.");
        System.out.println("Typing 'q' or 'quit' will exit the game.");
        System.out.println("Typing 'h' or 'help' will bring up this message.");
        System.out.println("Typing 't' or 'table' will display the current table information.");
        System.out.println("Typing 'l' or 'lobby' will display the list of people in the lobby.");
        System.out.println("Typing 's' or 'swap' followed by a number from 2-10 or A, J, K, Q, then the suit C, S, D, H will indicate to the server what card you wish to give to the scumbag.");
        System.out.println("Typing 'p' or 'play' followed by a number from 2-10 or A, J, K, Q, then the suit C, S, D, H will indicate to the server the cards you wish to play.");
        System.out.println("Both the play and swap message do not require the suit, if you have a card of the value indicated it will be found and transmitted.  If you do not it assumes the suit is clubs.");
        System.out.println("Both of these messages will transmit a card that you do not have.");
    }
}
