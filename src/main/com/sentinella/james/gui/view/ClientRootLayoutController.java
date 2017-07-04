package com.sentinella.james.gui.view;

import com.sentinella.james.ClientCallback;
import com.sentinella.james.MainWorker;
import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
        disconnect  = new MenuItem("Disconnect");
        disconnect.setOnAction(e -> disconnect());
        menuSep     = new SeparatorMenuItem();
        lobby       = new Menu("Lobby");
    }

    public void addMenuDisconnect() {
        menu.getMenus().add(3,lobby);
        file.getItems().add(2,disconnect);
        file.getItems().add(3,menuSep);
    }

    public void removeMenuDisconnect() {
        menu.getMenus().remove(lobby);
        file.getItems().remove(disconnect);
        file.getItems().remove(menuSep);
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
}
