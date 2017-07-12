package com.sentinella.james.gui.view;

import com.sentinella.james.*;
import com.sentinella.james.gui.WarlordVScumbagServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2017 James.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ServerRootLayoutController {
    private WarlordVScumbagServer serverGUI;
    private ServerOptionHolder    options;
    private boolean               serverRunning = false;

    private GUIServer theServer;
    private Thread    serverThread;
    private boolean   debug = false;

    @FXML private VBox      clients;
    @FXML private VBox      log;
    @FXML private TextField lobbyTO;
    @FXML private TextField playTO;
    @FXML private TextField minPlayers;
    @FXML private TextField maxClients;
    @FXML private TextField maxStrikes;
    @FXML private TextField portText;
    @FXML private Label     portLabel;
    @FXML private Label     connected;
    @FXML private Label     connectionInfo;
    @FXML private Button    start;
    @FXML private Button    disconnect;

    @FXML
    public void initialize() {
        options = new ServerOptionHolder();
        lobbyTO.setPromptText(String.valueOf(options.getLobbyTime()));
        playTO.setPromptText(String.valueOf(options.getPlayTime()));
        minPlayers.setPromptText(String.valueOf(options.getMinPlayers()));
        maxClients.setPromptText(String.valueOf(options.getMaxClients()));
        maxStrikes.setPromptText(String.valueOf(options.getMaxStrikes()));
        portText.setPromptText(String.valueOf(options.getPortNumber()));
        clients.setSpacing(5.0);
    }

    @FXML
    private void submit() {
        if (!updateSettings()) return;
        if (!serverRunning) {
            //
            if (theServer != null && !theServer.hasBeenClosed()) new Alert(Alert.AlertType.ERROR, "Server already running.", ButtonType.CLOSE).showAndWait();
            try {
                theServer = new GUIServer(options.getLobbyTime(),options.getPlayTime(),options.getMinPlayers(),options.getMaxStrikes(),options.getMaxClients(),options.getPortNumber(),debug);
                theServer.setUiThread(new ClientCallback() {
                    @Override
                    public void finished() {
                        disconnect();
                    }

                    @Override public void unableToConnect() {}

                    @Override public void setOutConnection(ClientSocket out) {}
                });
                theServer.setListener(() -> Platform.runLater(()->{
                    System.err.println("Updating log.");
                    ArrayList<String> messages  = new ArrayList<>();
                    List<String>      serverLog = theServer.getServerLog();
                    messages.addAll(serverLog);
                    serverLog.removeAll(messages);
                    System.err.println(String.format("Log messages to display: %d",messages.size()));
                    for (String message : messages) {
                        System.err.println(String.format("Message: '%s'",message));
                        log.getChildren().add(new Label(message));
                    }
                }));
                if (debug) theServer.setPrinter(new Printer() {
                    @Override
                    public void printString(String string) {
                        System.out.println(String.format("SERV: MSG: %s",string));
                        theServer.addToServerLog(string);
                    }

                    @Override
                    public void printErrorMessage(String string) { System.out.println(String.format("SERV: ERR: %s",string)); }

                    @Override
                    public void printDebugMessage(String string) { System.out.println(String.format("SERV: DBG: %s",string)); }

                    @Override
                    public void printLine() { printString("----------------------------------------"); }

                    @Override
                    public void setDebugStream(PrintStream stream) { }

                    @Override
                    public void setErrorStream(PrintStream stream) { }

                    @Override
                    public void setOutputStream(PrintStream stream) { }
                });
                else theServer.setPrinter(new Printer() {
                    @Override public void printString(String string) {
                        theServer.addToServerLog(string);
                    }

                    @Override public void printErrorMessage(String string) {}

                    @Override public void printDebugMessage(String string) {}

                    @Override public void printLine() { printString("----------------------------------------"); }

                    @Override public void setDebugStream(PrintStream stream) {}

                    @Override public void setErrorStream(PrintStream stream) {}

                    @Override public void setOutputStream(PrintStream stream) {}
                });
                serverThread = new Thread(theServer);
                serverThread.start();
                enableDisconnectButton();
            } catch (UnknownHostException e) {
                new Alert(Alert.AlertType.ERROR, "Unable to start server.", ButtonType.CLOSE).showAndWait();
            }
        }
    }

    private boolean updateSettings() {
        String current = lobbyTO.getText();
        if (current.length() > 0) {
            try {
                options.setLobbyTime(Integer.parseInt(current));
                lobbyTO.setPromptText(current);
                lobbyTO.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = playTO.getText();
        if (current.length() > 0) {
            try {
                options.setPlayTime(Integer.parseInt(current));
                playTO.setPromptText(current);
                playTO.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = minPlayers.getText();
        if (current.length() > 0) {
            try {
                int min = Integer.parseInt(current);
                if (min >= 3 && min <=7) {
                    options.setMinPlayers(Integer.parseInt(current));
                    minPlayers.setPromptText(current);
                }
                minPlayers.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = maxClients.getText();
        if (current.length() > 0) {
            try {
                int max = Integer.parseInt(current);
                if (max >= 3) {
                    options.setMaxClients(Integer.parseInt(current));
                    maxClients.setPromptText(current);
                }
                maxClients.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = maxStrikes.getText();
        if (current.length() > 0) {
            try {
                options.setMaxStrikes(Integer.parseInt(current));
                maxStrikes.setPromptText(current);
                maxStrikes.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = portText.getText();
        if (current.length() > 0) {
            try {
                int num = Integer.parseInt(current);
                if (num >= 1024) {
                    options.setPortNumber(Integer.parseInt(current));
                    portText.setPromptText(current);
                }
                portText.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        return true;
    }

    @FXML
    private void disconnect() {
        if (theServer == null) return;
        theServer.quit();
        new Thread(() -> {
            while (!theServer.hasBeenClosed()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(this::disableDisconnectButton);
        }).start();
    }

    private void disableDisconnectButton() {
        serverRunning = false;
        portLabel.setDisable(false);
        portText.setDisable(false);
        portLabel.setVisible(true);
        portText.setVisible(true);
        disconnect.setVisible(false);
        start.setText("Start");
        connected.setVisible(false);
        connectionInfo.setVisible(false);
    }

    private void enableDisconnectButton() {
        serverRunning = true;
        portLabel.setDisable(true);
        portText.setDisable(true);
        portLabel.setVisible(false);
        portText.setVisible(false);
        disconnect.setVisible(true);
        start.setText("Submit");
        try {
            connectionInfo.setText(String.format("%s:%s", InetAddress.getLocalHost().getHostAddress(),options.getPortNumber()));
            connectionInfo.setVisible(true);
            connected.setVisible(true);
        } catch (UnknownHostException e) {
            connected.setVisible(false);
            connectionInfo.setVisible(false);
        }
    }

    @FXML
    private void close() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void help() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagServer.class.getResource("view/ServerHelpLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((HelpController)loader.getController()).setStage(newStage);
        newStage.show();
    }

    @FXML
    private void about() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagServer.class.getResource("view/ServerAboutLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((HelpController)loader.getController()).setStage(newStage);
        newStage.show();
    }

    public void closeEverything() {
        if (theServer != null) theServer.quit();
        try {
            if (serverThread != null) serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class GUIServer extends Server {
        private ConcurrentHashMap<SocketChannel, Label> displayed = new ConcurrentHashMap<>();
        private List<String>                  serverLog = Collections.synchronizedList(new ArrayList<String>());
        private ServerListener                listener;
        private boolean                       hasBeenClosed = false;

        public GUIServer(int lobbyTimeOut, int playTimeOut, int minPlayers, int strikesAllowed, int maxClients, int port, boolean debug) throws UnknownHostException {
            super(lobbyTimeOut, playTimeOut, minPlayers, strikesAllowed, maxClients, port, debug);
        }

        public boolean hasBeenClosed() {
            return hasBeenClosed;
        }

        @Override
        public void done() {
            hasBeenClosed = true;
        }

        @Override
        protected void updateClients() {
            Platform.runLater(this::runLaterUpdateClients);
        }

        private void runLaterUpdateClients() {
            for (SocketChannel con : cons) {
                Player player = ((ServerLobby) sLobby).getPlayer(con);
                String name = "Unknown";
                if (player != null) {
                    name = player.getName();
                }
                Label nameLabel;
                if (displayed.containsKey(con)) {
                    nameLabel = displayed.get(con);
                } else {
                    HBox   container = new HBox();
                           nameLabel = new Label();
                    Button close     = new Button("Close");
                    container.getChildren().add(nameLabel);
                    container.getChildren().add(close);
                    nameLabel.setFont(Font.font(18.0));
                    container.setSpacing(5.0);
                    displayed.put(con,nameLabel);
                    clients.getChildren().add(container);
                    close.setOnAction(e-> new Thread(()->{
                        removeClient(con);
                        displayed.remove(con);
                        Platform.runLater(()->{
                            try {
                                clients.getChildren().remove(container);
                            } catch (Exception ex) {
                                System.err.println("Unable to remove client from list of displayed clients.");
                            }
                        });
                    }).start());
                }
                nameLabel.setText(name);
            }
        }

        public void addToServerLog(String string) {
            serverLog.add(string);
            if (listener != null) listener.updateServerLog();
        }

        public List<String> getServerLog() {
            return serverLog;
        }

        public void setListener(ServerListener listener) {
            this.listener = listener;
        }
    }
}
