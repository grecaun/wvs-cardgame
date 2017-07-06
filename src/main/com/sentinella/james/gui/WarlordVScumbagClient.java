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

    private GUIClient           theClient;
    private MainWorker          worker;

    private Thread              clientThread;
            boolean             debug = true;

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
            theClient    = new GUIClient(conIP, conPort, conName, false);
            worker       = new MainWorker(null, true);
            clientThread = new Thread(theClient);
            worker.setHand(theClient.getHand());
            theClient.setUiThread(rootController);
            rootController.setWorker(worker);
            theClient.setDebug(debug);
            theClient.setPrinter(new Printer() {
                @Override public void printString(String string) { System.out.println(String.format("MSG: %s",string)); }

                @Override public void printErrorMessage(String string) { System.err.println(String.format("ERR: %s",string)); }

                @Override public void printDebugMessage(String string) { System.out.println(String.format("DBG: %s",string)); }

                @Override public void printLine() { }
            });
        } catch (UnknownHostException e) {
            returnToLogin();
            return;
        }
        showPlayLayout();
        clientThread.start();
    }

    public void disconnect() {
        synchronized (this) {
            worker.sendQuit();
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
                cont.setDebug(debug);
                new Thread(() -> {
                    while (!theClient.isRunning()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Platform.runLater(() -> cont.enableChatSend());
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
