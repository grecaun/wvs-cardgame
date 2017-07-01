package com.sentinella.james.gui;/**
 * Created by James on 6/30/2017.
 */

import com.sentinella.james.Client;
import com.sentinella.james.Lobby;
import com.sentinella.james.MainWorker;
import com.sentinella.james.Table;
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

public class WarlordVScumbagClient extends Application {
    private Stage       primaryStage;
    private BorderPane  rootLayout;

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
        showPlayLayout();
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientRootLayout.fxml"));
            rootLayout = loader.load();
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

            ClientRootLayoutController cont = loader.getController();
            cont.setPrimaryStage(primaryStage);
            System.out.println(String.format("Width: %f - Height: %f",primaryStage.getWidth(),primaryStage.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String ip, String port, String name) {
        showPlayLayout();
    }

    private void showPlayLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WarlordVScumbagClient.class.getResource("view/ClientPlayLayout.fxml"));
            AnchorPane playLayout = loader.load();
            rootLayout.setCenter(playLayout);

            ClientPlayLayoutController cont = loader.getController();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
