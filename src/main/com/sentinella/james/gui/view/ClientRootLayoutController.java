package com.sentinella.james.gui.view;

import com.sentinella.james.*;
import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientRootLayoutController implements ClientCallback {
    private double[]                     prevScreen = {816.00, 639.00};
    private Stage                        primaryStage;
    private ClientPlayLayoutController   playController;
    private HostServices                 hostServices;

    private WarlordVScumbagClient        client;
    private MainWorker                   worker;
    private ClientOptionHolder           clientOptions;

    private GUIServer                    theServer;
    private ServerOptionHolder           serverOptions;

    private ArrayList<AIClient>          AIClients = new ArrayList<>();
    private ClientAIListLayoutController aiListController;

    private LogBook log = new GUILogBook();

    @FXML private MenuBar           menu;
    @FXML private Menu              file;
    @FXML private MenuItem          closeAIMenuItem;
    @FXML private MenuItem          listAIMenuItem;
    @FXML private MenuItem          startServer;
    @FXML private MenuItem          closeServer;
    @FXML private MenuItem          serverLog;
          private MenuItem          disconnect;
          private Menu              lobby;
          private SeparatorMenuItem menuSep;

    public void setLogBookInfo(LogBook l, String debugStr) {
        this.log = LogBookFactory.getLogBook(l,debugStr);
    }

    /*
     * Getters and setters.
     */
    public void         setPrimaryStage(Stage pStage) {this.primaryStage = pStage;}
    public void         setPlayController(ClientPlayLayoutController cont) {this.playController = cont;}
    public void         setClient(WarlordVScumbagClient client) {this.client = client;}
    public void         setWorker(MainWorker worker) {this.worker = worker;}
    public MainWorker   getWorker() {return worker;}
    public String       getHostName() {return clientOptions.getHostname();}
    public int          getHostPort() {return clientOptions.getHostport();}
    public void         setHostServices(HostServices hs) {this.hostServices=hs;}

    /*
     * File
     */
    @FXML
    private void settings() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientSettingsLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((ClientSettingsLayoutController)loader.getController()).setStage(newStage);
        ((ClientSettingsLayoutController)loader.getController()).setOptions(clientOptions);
        newStage.show();
    }

    @FXML
    private void startAI() {
        try {
            AIClient newClient = new AIClient(clientOptions.getHostname(),clientOptions.getHostport(),null,true,log,"AICLIENT");
            AIClients.add(newClient);
            newClient.setDelay(clientOptions.getDelay());
            newClient.setUiThread(newClient);
            new Thread(newClient).start();
            closeAIMenuItem.setDisable(false);
            listAIMenuItem.setDisable(false);
            if (aiListController != null) aiListController.setClients(AIClients);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listAI() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientAIListLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        aiListController = loader.getController();
        aiListController.setStage(newStage);
        aiListController.setClients(AIClients);
        newStage.show();
    }

    @FXML
    private void closeAI() {
        ArrayList<AIClient> closing = new ArrayList<>();
        for (AIClient client : AIClients) {
            client.quit();
            closing.add(client);
        }
        new Thread(()-> {
            while (closing.size() > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Iterator<AIClient> clientIterator = closing.iterator();
                while (clientIterator.hasNext()) {
                    AIClient curClient = clientIterator.next();
                    if (curClient.isFinished()) {
                        AIClients.remove(curClient);
                        clientIterator.remove();
                    }
                    else curClient.quit();
                }
            }
            Platform.runLater(this::updateCloseAIMenu);
        }).start();
    }

    private void updateCloseAIMenu() {
        Iterator<AIClient> iterator = AIClients.iterator();
        while (iterator.hasNext()) if (iterator.next().isFinished()) iterator.remove();
        if (aiListController != null) aiListController.setClients(AIClients);
        if (AIClients.size() < 1) {
            if (aiListController != null) aiListController.setClients(AIClients);
            closeAIMenuItem.setDisable(true);
            listAIMenuItem.setDisable(true);
        }
    }

    @FXML
    private void close() {
        Platform.exit();
        System.exit(0);
    }

    /*
     * Window Size
     */
    @FXML
    private void previous() {
        setScreenSize(prevScreen[0],prevScreen[1]);
    }

    @FXML
    private void screenOptions() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientScreenOptionsLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((ClientScreenOptionsLayoutController)loader.getController()).setStage(newStage);
        ((ClientScreenOptionsLayoutController)loader.getController()).setRootController(this);
        newStage.show();
    }

    void setScreenSize(double width, double height) {
        prevScreen[0] = primaryStage.getWidth();
        prevScreen[1] = primaryStage.getHeight();
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        if (playController != null) playController.updateView();
        if (width < 1200) {
            if (playController != null) playController.updateLeftPane(200.00);
        } else {
            if (playController != null) playController.updateLeftPane(300.00);
        }
    }

    /*
     * Server
     */
    @FXML
    private void startServer() {
        if (theServer != null && !theServer.hasBeenClosed()) new Alert(Alert.AlertType.ERROR, "Server already running.", ButtonType.CLOSE).showAndWait();
        try {
            theServer = new GUIServer(serverOptions.getLobbyTime(),serverOptions.getPlayTime(),serverOptions.getMinPlayers(),serverOptions.getMaxStrikes(),serverOptions.getMaxClients(),serverOptions.getPortNumber(),log,String.format("%s:%s",log.getDebugStr(),"SERVER"));
            theServer.setUiThread(new ClientCallback() {
                @Override
                public void finished() {
                    closeServer();
                }

                @Override public void unableToConnect() {}

                @Override public void setOutConnection(ClientSocket out) {}
            });
            Thread serverThread = new Thread(theServer);
            serverThread.start();
            enableCloseServer();
        } catch (UnknownHostException e) {
            new Alert(Alert.AlertType.ERROR, "Unable to start server.", ButtonType.CLOSE).showAndWait();
        }
    }

    @FXML
    private void closeServer() {
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
            Platform.runLater(this::disableCloseServer);
        }).start();
    }

    private void disableCloseServer() {
        serverLog.setDisable(true);
        closeServer.setDisable(true);
        startServer.setDisable(false);
    }

    private void enableCloseServer() {
        serverLog.setDisable(false);
        closeServer.setDisable(false);
        startServer.setDisable(true);
    }

    @FXML
    private void serverLog() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientServerLogLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClientServerLogLayoutController cont = loader.getController();
        cont.setStage(newStage);
        cont.setLog(theServer.getServerLog());
        theServer.setListener(()->{
            Platform.runLater(cont::updateLog);
        });
        cont.updateLog();
        cont.setServer(theServer);
        newStage.show();
    }

    @FXML
    private void optionsServer() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientServerOptionsLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ClientServerOptionsLayoutController cont = loader.getController();
        cont.setOptions(serverOptions);
        cont.setStage(newStage);
        cont.setRootLayoutController(this);
        newStage.show();
    }

    public void updateServerSettings() {
        if (theServer == null) return;
        theServer.setMaxClients(serverOptions.getMaxClients());
        theServer.setMinPlayers(serverOptions.getMinPlayers());
        theServer.setStrikesAllowed(serverOptions.getMaxStrikes());
        theServer.setLobbyTimeOut(serverOptions.getLobbyTime());
        theServer.setPlayTimeOut(serverOptions.getPlayTime());
    }

    /*
     * Help
     */
    @FXML
    private void howtoplay() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientHowToPlayLayout.fxml"));
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
        loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientAboutLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((HelpController)loader.getController()).setStage(newStage);
        ((HelpController)loader.getController()).setHostServices(hostServices);
        newStage.show();
    }

    @FXML
    private void initialize() {
        prevScreen[1] = Objects.equals(System.getProperty("os.name"), "Mac OS X") ? 661.00 : 639.00;
        disconnect      = new MenuItem("Disconnect");
        disconnect.setOnAction(e -> disconnect());
        disconnect.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        disconnect.setDisable(true);
        menuSep         = new SeparatorMenuItem();
        lobby           = new Menu("Lobby");
        serverOptions   = new ServerOptionHolder();
        clientOptions   = new ClientOptionHolder();
    }

    /*
     * Disconnect menu options.
     */
    public void addMenuDisconnect() {
        menu.getMenus().add(3, lobby);
        file.getItems().add(2, disconnect);
        file.getItems().add(3, menuSep);
        disconnect.setDisable(false);
    }

    public void removeMenuDisconnect() {
        menu.getMenus().remove(lobby);
        file.getItems().remove(disconnect);
        file.getItems().remove(menuSep);
        disconnect.setDisable(true);
    }

    public void updateLobby(ArrayList<String> members) {
        lobby.getItems().clear();
        for (String person : members) {
            lobby.getItems().add(new MenuItem(person));
        }
    }

    private void disconnect() {
        client.disconnect();
    }

    public void closeEverything() {
        closeAI();
        closeServer();
    }

    /*
     * ClientCallback methods
     */
    @Override
    public void setOutConnection(ClientSocket out) {
        worker.setOutConnection(out);
    }

    @Override
    public void unableToConnect() {
        Platform.runLater( () -> {
            new Alert(Alert.AlertType.ERROR, "Unable to connect.", ButtonType.CLOSE).showAndWait();
            client.returnToLogin();
        });
    }

    @Override
    public void finished() {
        Platform.runLater(() -> client.returnToLogin());
    }

    /*
     * Classes
     */
    class ClientOptionHolder {
        private String hostname = "localhost";
        private int    hostport = 36789;
        private int    delay    = 5;

        public int getHostport() {
            return hostport;
        }

        public void setHostport(int hostport) { if (hostport > 1024) this.hostport = hostport; }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }

    class AIClient extends Client implements ClientCallback {
        private boolean finished = false;

        AIClient(String iHostName, int iHostPort, String iName, boolean iIsAuto, LogBook iLog, String iDebStr) throws UnknownHostException {
            super(iHostName,iHostPort,iName,iIsAuto,iLog,iDebStr);
        }

        @Override
        public void finished() {
            log.printDebMsg("Setting finished to true.",1);
            finished = true;
            Platform.runLater(ClientRootLayoutController.this::updateCloseAIMenu);
        }

        @Override public void unableToConnect() {}

        @Override public void setOutConnection(ClientSocket out) { }

        public boolean isFinished() {
            log.printDebMsg("Someone is checking if I'm finished.",1);
            return finished; }
    }

    class GUIServer extends Server implements LogBookCallback {
       private List<String>      serverLog = Collections.synchronizedList(new ArrayList<>());
       private ServerListener    listener;
       private boolean           hasBeenClosed = false;

       public GUIServer(int lobbyTimeOut, int playTimeOut, int minPlayers, int strikesAllowed, int maxClients, int port, LogBook l, String debStr) throws UnknownHostException {
           super(lobbyTimeOut, playTimeOut, minPlayers, strikesAllowed, maxClients, port);
           this.log = LogBookFactory.getLogBook(l,debStr);
           ((GUILogBook)log).setCallback(this);
           sLobby.setLogBookInfo(log,String.format("%s:%s",debStr,"SERVERLOBBY"));
           sTable.setLogBookInfo(log,String.format("%s:%s",debStr,"SERVERTABLE"));
       }

       public boolean hasBeenClosed() {
            return hasBeenClosed;
        }

       @Override
       public void done() {
           hasBeenClosed = true;
       }

       public void addToMessages(String string) {
           serverLog.add(string);
           if (listener != null) listener.updateServerLog();
       }

       public List<String> getServerLog() {
           return serverLog;
       }

       public void closeListener() {
           listener = null;
       }

       public void setListener(ServerListener listener) {
           this.listener = listener;
       }
    }
}
