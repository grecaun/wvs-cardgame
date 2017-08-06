package com.sentinella.james.gui;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import com.sentinella.james.*;
import com.sentinella.james.gui.view.ClientLoginLayoutController;
import com.sentinella.james.gui.view.ClientPlayLayoutController;
import com.sentinella.james.gui.view.ClientRootLayoutController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;

public class WarlordVScumbagClient extends Application {
    private Stage                      primaryStage;
    private BorderPane                 rootLayout;
    private ClientRootLayoutController rootController;

    private GUIClient           theClient;
    private MainWorker          worker;

    private Thread              clientThread;
    private LogBook log = new GUILogBook(3,true,"GUICLIENT");

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

    @Override
    public void stop() {
        if (theClient != null) theClient.quit();
        if (rootController != null) rootController.closeEverything();
        try {
            if (clientThread != null) clientThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            rootController.setLogBookInfo(log,String.format("%s:%s",log.getDebugStr(),"ROOTCONTROLLER"));
            rootController.setHostServices(getHostServices());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String name) {
        String conName = null;
        if (name.length() > 0) {
            conName = name;
        }
        try {
            theClient    = new GUIClient(rootController.getHostName(), rootController.getHostPort(), conName, false);
            worker       = new MainWorker(null);
            clientThread = new Thread(theClient);
            worker.setHand(theClient.getHand());
            theClient.setUiThread(rootController);
            rootController.setWorker(worker);
        } catch (UnknownHostException e) {
            new Alert(Alert.AlertType.ERROR, "Unable to connect.", ButtonType.CLOSE).showAndWait();
            returnToLogin();
            return;
        }
        showPlayLayout();
        clientThread.start();
    }

    public void disconnect() {
        synchronized (this) {
            theClient.quit();
        }
    }

    private void showPlayLayout() {
        synchronized (this) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientPlayLayout.fxml"));
                AnchorPane playLayout = loader.load();
                rootLayout.setCenter(playLayout);

                ClientPlayLayoutController cont = loader.getController();
                rootController.setPlayController(cont);
                rootController.addMenuDisconnect();
                cont.setWorker(rootController.getWorker());
                theClient.setUpdater(cont);
                cont.setPrimaryStage(primaryStage);
                cont.setClient(theClient);
                cont.setRootController(rootController);
                cont.setLogBookInfo(log,String.format("%s:%s",log.getDebugStr(),"PLAYCONTROLLER"));
                new Thread(() -> {
                    while (!theClient.isRunning()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Platform.runLater(cont::enableChatSend);
                }).start();
                if (primaryStage.getWidth() < 1280) {
                    cont.updateLeftPane(200.00);
                } else {
                    cont.updateLeftPane(300);
                }
                cont.updateView();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    class GUIClient extends Client {

        GUIClient(String conIP, int conPort, String conName, boolean b) throws UnknownHostException {
            super(conIP,conPort,conName,b);
        }

        boolean isRunning() {
            return this.cState != ClientState.INIT;
        }
    }
}
