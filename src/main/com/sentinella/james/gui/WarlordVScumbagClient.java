package com.sentinella.james.gui;/**
 * Created by James on 6/30/2017.
 */

import com.sentinella.james.*;
import com.sentinella.james.gui.view.ClientLoginLayoutController;
import com.sentinella.james.gui.view.ClientPlayLayoutController;
import com.sentinella.james.gui.view.ClientRootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.UnknownHostException;

public class WarlordVScumbagClient extends Application {
    private Stage                       primaryStage;
    private BorderPane                  rootLayout;
    private ClientRootLayoutController  rootController;
    private ClientCallback              lifeLine;

    Client              theClient;
    MainWorker          worker;
    Table               table;
    Lobby               lobby;

    Thread              clientThread;
    boolean             debug = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Warlords Vs Scumbags");
        primaryStage.setResizable(false);

        initRootLayout();
        showLoginLayout();
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientRootLayout.fxml"));
            rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

            rootController = loader.getController();
            rootController.setPrimaryStage(primaryStage);
            rootController.setClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String ip, String port, String name) {
        String conIP = null, conName = null;
        int conPort = 0;
        if (ip.length() > 0) {
            conIP = ip;
        }
        if (port.length() > 0) {
            try {
                conPort = Integer.parseInt(port.trim());
            } catch (Exception e) {}
        }
        if (name.length() > 0) {
            conName = name;
        }
        try {
            theClient   = new Client(conIP, conPort, conName, false);
            worker      = new MainWorker(theClient.getOutConnection(), false);
            table       = theClient.getTable();
            lobby       = theClient.getLobby();
            worker.setHand(theClient.getHand());
            clientThread = new Thread(theClient);
            lifeLine     = rootController;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        showPlayLayout();
    }

    public void disconnect() {
        returnToLogin();
    }

    private void showPlayLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientPlayLayout.fxml"));
            AnchorPane playLayout = loader.load();
            rootLayout.setCenter(playLayout);

            ClientPlayLayoutController cont = loader.getController();
            rootController.setPlayController(cont);
            rootController.addMenuDisconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLoginLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientLoginLayout.fxml"));
            AnchorPane newLayout = loader.load();
            rootLayout.setCenter(newLayout);

            ClientLoginLayoutController cont = loader.getController();
            cont.setMainApp(this);
            rootController.setPlayController(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void returnToLogin() {
        rootController.removeMenuDisconnect();
        showLoginLayout();
    }
}
