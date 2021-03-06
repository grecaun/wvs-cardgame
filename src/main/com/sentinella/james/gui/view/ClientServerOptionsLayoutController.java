package com.sentinella.james.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientServerOptionsLayoutController {

    private ServerOptionHolder          options;
    private ClientRootLayoutController  rootLayoutController;
    private Stage                       stage;

    @FXML private TextField portNumber;
    @FXML private TextField lobbyTimeOut;
    @FXML private TextField playTimeOut;
    @FXML private TextField minPlayers;
    @FXML private TextField maxClients;
    @FXML private TextField maxStrikes;

    @FXML
    private void moreInfo(MouseEvent e) {
        Label selected = (Label) e.getSource();
        Alert alert;
        switch (selected.getId()) {
            case "port":
                alert = new Alert(Alert.AlertType.INFORMATION, "The port the server listens to for new connections.", ButtonType.CLOSE);
                break;
            case "lobby":
                alert = new Alert(Alert.AlertType.INFORMATION, "The amount of time in seconds between when the server gets enough players to start a game and when the first game starts.", ButtonType.CLOSE);
                break;
            case "play":
                alert = new Alert(Alert.AlertType.INFORMATION, "The amount of time in seconds the server will wait for a response when waiting for a player to play or send a card for the swap. 0 means it will wait forever.", ButtonType.CLOSE);
                break;
            case "min":
                alert = new Alert(Alert.AlertType.INFORMATION, "The minimum number of players required for a game to start. May not be less than 3 or greater than 7.", ButtonType.CLOSE);
                break;
            case "max":
                alert = new Alert(Alert.AlertType.INFORMATION, "The maximum number of clients that can be connected to the server at any given point.", ButtonType.CLOSE);
                break;
            case "strikes":
                alert = new Alert(Alert.AlertType.INFORMATION, "The maximum number of strikes a person can receive before being disconnected from the server.", ButtonType.CLOSE);
                break;
            default:
                alert = new Alert(Alert.AlertType.INFORMATION, "Something went wrong. Please close this.", ButtonType.CLOSE);
        }
        alert.showAndWait();
    }

    @FXML
    private void saveSettings() {
        if (options != null) {
            int val;
            boolean error = false;
            if (portNumber.getText().length() > 0) {
                try {
                    val = Integer.parseInt(portNumber.getText().trim());
                    if (val > 100) options.setPortNumber(val);
                } catch (Exception e) {
                    error = true;
                }
            }
            if (lobbyTimeOut.getText().length() > 0) {
                try {
                    val = Integer.parseInt(lobbyTimeOut.getText().trim());
                    if (val >= 0) options.setLobbyTime(val);
                } catch (Exception e) {
                    error = true;
                }
            }
            if (playTimeOut.getText().length() > 0) {
                try {
                    val = Integer.parseInt(playTimeOut.getText().trim());
                    if (val >= 0) options.setPlayTime(val);
                } catch (Exception e) {
                    error = true;
                }
            }
            if (minPlayers.getText().length() > 0) {
                try {
                    val = Integer.parseInt(minPlayers.getText().trim());
                    if (val > 2 || val < 8) options.setMinPlayers(val);
                } catch (Exception e) {
                    error = true;
                }
            }
            if (maxClients.getText().length() > 0) {
                try {
                    val = Integer.parseInt(maxClients.getText().trim());
                    if (val > 2 || val < 91) options.setMaxClients(val);
                } catch (Exception e) {
                    error = true;
                }
            }
            if (maxStrikes.getText().length() > 0) {
                try {
                    val = Integer.parseInt(maxStrikes.getText().trim());
                    if (val >= 0) options.setMaxStrikes(val);
                } catch (Exception e) {
                    error = true;
                }
            }
            if (error) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "One or more settings is unable to be processed. Only numerical values are acceptable.", ButtonType.CLOSE);
                alert.showAndWait();
            } else  {
                rootLayoutController.updateServerSettings();
                this.stage.close();
            }
        }
    }

    @FXML
    private void cancel() { this.stage.close(); }

    public void setOptions(ServerOptionHolder options) {
        this.options = options;
        portNumber.setPromptText(String.valueOf(options.getPortNumber()));
        lobbyTimeOut.setPromptText(String.valueOf(options.getLobbyTime()));
        playTimeOut.setPromptText(String.valueOf(options.getPlayTime()));
        minPlayers.setPromptText(String.valueOf(options.getMinPlayers()));
        maxClients.setPromptText(String.valueOf(options.getMaxClients()));
        maxStrikes.setPromptText(String.valueOf(options.getMaxStrikes()));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setRootLayoutController(ClientRootLayoutController rootLayoutController) {
        this.rootLayoutController = rootLayoutController;
    }
}
