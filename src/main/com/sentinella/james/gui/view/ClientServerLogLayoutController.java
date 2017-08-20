package com.sentinella.james.gui.view;

import com.sentinella.james.Server;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientServerLogLayoutController {
    private Stage                                   stage;
    private ArrayList<String>                       oldLog = new ArrayList<>();
    private List<String>                            newLog;
    private ClientRootLayoutController.GUIServer    server;

    @FXML private VBox logMessages;

    @FXML
    private void close() {
        synchronized (newLog) {
            newLog.addAll(0, oldLog);
            server.closeListener();
            this.stage.close();
        }
    }


    public synchronized void updateLog() {
        synchronized (newLog) {
            Iterator<String> iterator = newLog.iterator();
            while (iterator.hasNext()) {
                String cur = iterator.next();
                oldLog.add(cur);
                iterator.remove();
                logMessages.getChildren().add(new Label(cur));
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(e->{
            synchronized (newLog) {
                newLog.addAll(0, oldLog);
                server.closeListener();
            }
        });

    }

    public void setLog(List<String> log) {
        synchronized (log) {
            this.newLog = log;
        }
    }

    public void setServer(ClientRootLayoutController.GUIServer server) {
        this.server = server;
    }
}
