package com.sentinella.james.gui.view;

import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by James on 7/1/2017.
 */
public class ClientRootLayoutController {
    private double[] prevScreen = {816.0,639.0};
    private Stage primaryStage;

    public void setPrimaryStage(Stage pStage) {
        this.primaryStage = pStage;
    }

    void setScreenSize(double width, double height) {
        prevScreen[0]  = primaryStage.getWidth();
        prevScreen[1] = primaryStage.getHeight();
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
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
    private void fullscreen() {
        prevScreen[0] = primaryStage.getWidth();
        prevScreen[1] = primaryStage.getHeight();
        primaryStage.setMaximized(true);
    }

    @FXML
    private void previous() {
        double prevWidth  = primaryStage.getWidth();
        double prevHeight = primaryStage.getHeight();
        primaryStage.setWidth(prevScreen[0]);
        primaryStage.setHeight(prevScreen[1]);
        prevScreen[0] = prevWidth;
        prevScreen[1] = prevHeight;
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
}
