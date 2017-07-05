package com.sentinella.james.gui.view;

import com.sentinella.james.ClientCallback;
import com.sentinella.james.MainWorker;
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
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by James on 7/1/2017.
 */
public class ClientRootLayoutController implements ClientCallback {
    private double[]                    prevScreen = {816.0,639.0};
    private Stage                       primaryStage;
    private ClientPlayLayoutController  playController;
    private WarlordVScumbagClient       client;
    private MainWorker                  worker;
    private ServerOptionHolder          serverOptions;

    @FXML private MenuBar           menu;
    @FXML private Menu              file;
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
        if (width < 1200) {
            if (playController != null) playController.updateLeftPane(200.00);
        } else {
            if (playController != null) playController.updateLeftPane(300.00);
        }
        if (playController != null) playController.updateView();
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
    }

    public void addMenuDisconnect() {
        menu.getMenus().add(3,lobby);
        file.getItems().add(2,disconnect);
        file.getItems().add(3,menuSep);
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
        for (String person: members) {
            lobby.getItems().add(new MenuItem(person));
        }
    }

    @FXML
    private void startAI() {

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

    }

    @FXML
    private void closeServer() {
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
        Platform.runLater(() ->client.returnToLogin());
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
}
