package com.sentinella.james.gui;/**
 * Created by James on 6/30/2017.
 */

import com.sentinella.james.*;
import com.sentinella.james.gui.view.ClientLoginLayoutController;
import com.sentinella.james.gui.view.ClientPlayLayoutController;
import com.sentinella.james.gui.view.ClientRootLayoutController;
import javafx.application.Application;
import javafx.application.Platform;
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

    private Client              theClient;
    private MainWorker          worker;

    private Thread              clientThread;
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

    @Override
    public void stop() {
        if (theClient != null) theClient.quit();
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
            } catch (Exception e) { conPort = 0; }
        }
        if (name.length() > 0) {
            conName = name;
        }
        try {
            theClient    = new Client(conIP, conPort, conName, false);
            worker       = new MainWorker(null, false);
            clientThread = new Thread(theClient);
            worker.setHand(theClient.getHand());
            theClient.setUiThread(rootController);
            rootController.setWorker(worker);
            theClient.setDebug(false);
            clientThread.start();
        } catch (UnknownHostException e) {
            returnToLogin();
            return;
        }
        showPlayLayout();
    }

    public void disconnect() {
        worker.sendQuit();
        Platform.runLater(() -> theClient.quit());
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
            cont.setWorker(rootController.getWorker());
            theClient.setUpdater(cont);
            cont.setPrimaryStage(primaryStage);
            cont.updateView();
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
