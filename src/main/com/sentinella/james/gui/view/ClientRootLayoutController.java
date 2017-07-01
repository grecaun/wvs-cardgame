package com.sentinella.james.gui.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 * Created by James on 7/1/2017.
 */
public class ClientRootLayoutController {
    private double[] prevScreen = {800.0,600.0};
    private Stage primaryStage;

    public void setPrimaryStage(Stage pStage) {
        this.primaryStage = pStage;
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

    }

    @FXML
    private void about() {

    }
}
