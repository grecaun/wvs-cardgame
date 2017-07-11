package com.sentinella.james.gui.view;

import com.sentinella.james.gui.WarlordVScumbagServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Copyright (c) 2017 James.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ServerRootLayoutController {
    private WarlordVScumbagServer serverGUI;
    private ServerOptionHolder    options;
    private boolean               serverRunning = false;

    @FXML private VBox      clients;
    @FXML private VBox      log;
    @FXML private TextField lobbyTO;
    @FXML private TextField playTO;
    @FXML private TextField minPlayers;
    @FXML private TextField maxClients;
    @FXML private TextField maxStrikes;
    @FXML private TextField portText;
    @FXML private Label     portLabel;
    @FXML private Button    disconnect;

    @FXML
    public void initialize() {
        options = new ServerOptionHolder();
        lobbyTO.setPromptText(String.valueOf(options.getLobbyTime()));
        playTO.setPromptText(String.valueOf(options.getPlayTime()));
        minPlayers.setPromptText(String.valueOf(options.getMinPlayers()));
        maxClients.setPromptText(String.valueOf(options.getMaxClients()));
        maxStrikes.setPromptText(String.valueOf(options.getMaxStrikes()));
        portText.setPromptText(String.valueOf(options.getPortNumber()));
    }

    @FXML
    private void submit() {
        if (!updateSettings()) return;
        if (!serverRunning) {
            // start server
            serverRunning = true;
            portLabel.setDisable(true);
            portText.setDisable(true);
            portLabel.setVisible(false);
            portText.setVisible(false);
            disconnect.setVisible(true);
        } else {
            serverRunning = false;
            portLabel.setDisable(false);
            portText.setDisable(false);
            portLabel.setVisible(true);
            portText.setVisible(true);
            disconnect.setVisible(false);
        }
    }

    private boolean updateSettings() {
        String current = lobbyTO.getText();
        if (current.length() > 0) {
            try {
                options.setLobbyTime(Integer.parseInt(current));
                lobbyTO.setPromptText(current);
                lobbyTO.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = playTO.getText();
        if (current.length() > 0) {
            try {
                options.setPlayTime(Integer.parseInt(current));
                playTO.setPromptText(current);
                playTO.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = minPlayers.getText();
        if (current.length() > 0) {
            try {
                int min = Integer.parseInt(current);
                if (min >= 3 && min <=7) {
                    options.setMinPlayers(Integer.parseInt(current));
                    minPlayers.setPromptText(current);
                }
                minPlayers.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = maxClients.getText();
        if (current.length() > 0) {
            try {
                int max = Integer.parseInt(current);
                if (max >= 3) {
                    options.setMaxClients(Integer.parseInt(current));
                    maxClients.setPromptText(current);
                }
                maxClients.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = maxStrikes.getText();
        if (current.length() > 0) {
            try {
                options.setMaxStrikes(Integer.parseInt(current));
                maxStrikes.setPromptText(current);
                maxStrikes.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        current = portText.getText();
        if (current.length() > 0) {
            try {
                int num = Integer.parseInt(current);
                if (num >= 1024) {
                    options.setPortNumber(Integer.parseInt(current));
                    portText.setPromptText(current);
                }
                portText.clear();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "One or more settings is not a number.", ButtonType.CLOSE);
                return false;
            }
        }
        return true;
    }

    @FXML
    private void disconnect() {

    }

    @FXML
    private void close() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void help() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagServer.class.getResource("view/ServerHelpLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((HelpController)loader.getController()).setStage(newStage);
        newStage.show();
    }

    @FXML
    private void about() {
        Stage newStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(WarlordVScumbagServer.class.getResource("view/ServerAboutLayout.fxml"));
        try {
            newStage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((HelpController)loader.getController()).setStage(newStage);
        newStage.show();
    }
}
