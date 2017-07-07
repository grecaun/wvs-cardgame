package com.sentinella.james.gui.view;

import com.sentinella.james.*;
import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by James on 7/1/2017.
 */
public class ClientRootLayoutController implements ClientCallback {
    private double[]                     prevScreen = {816.0,639.0};
    private Stage                        primaryStage;
    private ClientPlayLayoutController   playController;

    private WarlordVScumbagClient        client;
    private MainWorker                   worker;
    private ClientOptionHolder           clientOptions;

    private GUIServer                    theServer;
    private ServerOptionHolder           serverOptions;

    private ArrayList<AIClient>          AIClients = new ArrayList<>();
    private ClientAIListLayoutController aiListController;

    private boolean                      debug = false;

    @FXML private MenuBar           menu;
    @FXML private Menu              file;
    @FXML private MenuItem          closeAIMenuItem;
    @FXML private MenuItem          listAIMenuItem;
          private MenuItem          disconnect;
          private Menu              lobby;
          private SeparatorMenuItem menuSep;

    public void setPrimaryStage(Stage pStage) {
        this.primaryStage = pStage;
    }

    public void setPlayController(ClientPlayLayoutController cont) {
        this.playController = cont;
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

    @FXML
    private void initialize() {
        disconnect      = new MenuItem("Disconnect");
        disconnect.setOnAction(e -> disconnect());
        disconnect.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        disconnect.setDisable(true);
        menuSep         = new SeparatorMenuItem();
        lobby           = new Menu("Lobby");
        serverOptions   = new ServerOptionHolder();
        clientOptions   = new ClientOptionHolder();
    }

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
            AIClient newClient = new AIClient(clientOptions.getHostname(),clientOptions.getHostport(),null,true);
            AIClients.add(newClient);
            newClient.setDelay(clientOptions.getDelay());
            newClient.setUiThread(newClient);
            if (debug) newClient.setPrinter(new Printer() {
                private final int AINum = AIClients.size();

                @Override
                public void printString(String string) {
                    System.out.println(String.format("AI%d: MSG: %s",AINum,string));
                }

                @Override
                public void printErrorMessage(String string) {
                    System.err.println(String.format("AI%d: ERR: %s",AINum,string));
                }

                @Override
                public void printDebugMessage(String string) {
                    System.out.println(String.format("AI%d: DBG: %s",AINum,string));
                }

                @Override public void printLine() {}

                @Override public void setDebugStream(PrintStream stream) {}

                @Override public void setErrorStream(PrintStream stream) {}

                @Override public void setOutputStream(PrintStream stream) {}
            });
            newClient.setDebug(debug);
            new Thread(newClient).start();
            closeAIMenuItem.setDisable(false);
            listAIMenuItem.setDisable(false);
            if (aiListController != null) aiListController.setClients(AIClients);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void closeAI() {
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
            Platform.runLater(() -> updateCloseAIMenu());
        }).start();
    }

    @FXML
    public void listAI() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientAIListLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        aiListController = (ClientAIListLayoutController) loader.getController();
        aiListController.setStage(newStage);
        aiListController.setClients(AIClients);
        newStage.show();
    }

    private void updateCloseAIMenu() {
        Iterator<AIClient> iterator = AIClients.iterator();
        while (iterator.hasNext()) if (iterator.next().isFinished()) iterator.remove();
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

    @FXML
    private void disconnect() {
        client.disconnect();
    }

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

    @FXML
    private void startServer() {
        if (theServer != null && !theServer.hasBeenClosed()) new Alert(Alert.AlertType.ERROR, "Server already running.", ButtonType.CLOSE).showAndWait();
        try {
            theServer = new GUIServer(serverOptions.getLobbyTime(),serverOptions.getPlayTime(),serverOptions.getMinPlayers(),serverOptions.getMaxStrikes(),serverOptions.getMaxClients(),serverOptions.getPortNumber(),debug);
            theServer.setUiThread(new ClientCallback() {
                @Override
                public void finished() {
                    closeServer();
                }

                @Override public void unableToConnect() {}

                @Override public void setOutConnection(PrintWriter out) {}
            });
            if (debug) theServer.setPrinter(new Printer() {
                @Override
                public void printString(String string) { System.out.println(String.format("SERV: MSG: %s",string)); }

                @Override
                public void printErrorMessage(String string) { System.out.println(String.format("SERV: ERR: %s",string)); }

                @Override
                public void printDebugMessage(String string) { System.out.println(String.format("SERV: DBG: %s",string)); }

                @Override
                public void printLine() { System.out.println("SERV: ----------------------------------------"); }

                @Override
                public void setDebugStream(PrintStream stream) { }

                @Override
                public void setErrorStream(PrintStream stream) { }

                @Override
                public void setOutputStream(PrintStream stream) { }
            });
            else theServer.setPrinter(new Printer() {
                @Override public void printString(String string) {}

                @Override public void printErrorMessage(String string) {}

                @Override public void printDebugMessage(String string) {}

                @Override public void printLine() {}

                @Override public void setDebugStream(PrintStream stream) {}

                @Override public void setErrorStream(PrintStream stream) {}

                @Override public void setOutputStream(PrintStream stream) {}
            });
            Thread serverThread = new Thread(theServer);
            serverThread.start();
        } catch (UnknownHostException e) {
            new Alert(Alert.AlertType.ERROR, "Unable to start server.", ButtonType.CLOSE).showAndWait();
        }
    }

    @FXML
    private void closeServer() {
        if (theServer == null || theServer.hasBeenClosed()) new Alert(Alert.AlertType.ERROR, "No server running.", ButtonType.CLOSE).showAndWait();
        else theServer.quit();
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
        ((ClientServerOptionsLayoutController)loader.getController()).setOptions(serverOptions);
        ((ClientServerOptionsLayoutController)loader.getController()).setStage(newStage);
        newStage.show();
    }

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
        ((ClientHelpController)loader.getController()).setStage(newStage);
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
        ((ClientHelpController)loader.getController()).setStage(newStage);
        newStage.show();
    }

    @Override
    public void finished() {
        Platform.runLater(() -> client.returnToLogin());
    }

    @Override
    public void unableToConnect() {
        Platform.runLater( () -> {
            new Alert(Alert.AlertType.ERROR, "Unable to connect.", ButtonType.CLOSE).showAndWait();
            client.returnToLogin();
        });
    }

    @Override
    public void setOutConnection(PrintWriter out) {
        worker.setOutConnection(out);
    }

    public void setClient(WarlordVScumbagClient client) {
        this.client = client;
    }

    public void setWorker(MainWorker worker) {
        this.worker = worker;
    }

    public MainWorker getWorker() {
        return worker;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getHostName() {
        return clientOptions.getHostname();
    }

    public int getHostPort() {
        return clientOptions.getHostport();
    }

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

    class ServerOptionHolder {
        private int portNumber = 36789;
        private int lobbyTime  = 15;
        private int playTime   = 90;
        private int minPlayers = 3;
        private int maxClients = 90;
        private int maxStrikes = 5;

        int getPortNumber() {
            return portNumber;
        }

        void setPortNumber(int portNumber) {
            this.portNumber = portNumber;
        }

        int getLobbyTime() {
            return lobbyTime;
        }

        void setLobbyTime(int lobbyTime) {
            this.lobbyTime = lobbyTime;
        }

        int getPlayTime() {
            return playTime;
        }

        void setPlayTime(int playTime) {
            this.playTime = playTime;
        }

        int getMinPlayers() {
            return minPlayers;
        }

        void setMinPlayers(int minPlayers) {
            this.minPlayers = minPlayers;
        }

        int getMaxClients() {
            return maxClients;
        }

        void setMaxClients(int maxClients) {
            this.maxClients = maxClients;
        }

        int getMaxStrikes() {
            return maxStrikes;
        }

        void setMaxStrikes(int maxStrikes) {
            this.maxStrikes = maxStrikes;
        }
    }

    class AIClient extends Client implements ClientCallback {
        private boolean finished = false;

        public AIClient(String iHostName, int iHostPort, String iName, boolean iIsAuto) throws UnknownHostException {
            super(iHostName, iHostPort, iName, iIsAuto);
        }

        @Override
        public void finished() {
            printer.printString("Setting finished to true.");
            finished = true;
            Platform.runLater(ClientRootLayoutController.this::updateCloseAIMenu);
        }

        @Override public void unableToConnect() {}

        @Override public void setOutConnection(PrintWriter out) { }

        public boolean isFinished() {
            printer.printString("Someone is checking if I'm finished.");
            return finished; }
    }

    class GUIServer extends Server {
       private boolean hasBeenClosed = false;

       public GUIServer(int lobbyTimeOut, int playTimeOut, int minPlayers, int strikesAllowed, int maxClients, int port, boolean debug) throws UnknownHostException {
           super(lobbyTimeOut, playTimeOut, minPlayers, strikesAllowed, maxClients, port, debug);
       }

       public boolean hasBeenClosed() {
            return hasBeenClosed;
        }

       @Override
       public void done() {
           System.out.println("SERVER TERMINATED");
           hasBeenClosed = true;
       }
    }
}
