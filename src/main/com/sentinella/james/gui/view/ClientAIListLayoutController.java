package com.sentinella.james.gui.view;

import com.sentinella.james.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientAIListLayoutController {
    private ArrayList<ClientRootLayoutController.AIClient> clients;
    private Stage                                          stage;

    @FXML private VBox aiList;

    @FXML
    private void close() { stage.close(); }

    public void setClients(ArrayList<ClientRootLayoutController.AIClient> clients) {
        this.clients = clients;
        updateClients();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private HBox getClientBox(ClientRootLayoutController.AIClient client) {
        HBox outBox = new HBox();
        outBox.setSpacing(10.0);
        outBox.setAlignment(Pos.CENTER);
        Label nameLabel = new Label(String.format("%s - Strikes: %d",client.getName(),client.getStrikes()));
        nameLabel.setFont(Font.font(18.0));
        outBox.getChildren().add(nameLabel);
        Button closeAIButton = new Button("Close");
        closeAIButton.setOnAction(event -> {
            client.quit();
            new Thread(()->{
                while (!client.isFinished()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Platform.runLater(this::updateClients);
            }).start();
        });
        outBox.getChildren().add(closeAIButton);

        return outBox;
    }

    private void updateClients() {
        aiList.getChildren().clear();
        if (clients.size() == 0) close();
        Iterator<ClientRootLayoutController.AIClient> iterator = clients.iterator();
        while (iterator.hasNext()) {
            ClientRootLayoutController.AIClient thisClient = iterator.next();
            if (thisClient.isFinished()) iterator.next();
            else aiList.getChildren().add(getClientBox(thisClient));
        }
    }
}
