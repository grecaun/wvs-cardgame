package com.sentinella.james.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
public class ClientServerLogLayoutController {
    private Stage                       stage;
    private ArrayList<String>           oldLog = new ArrayList<>();
    private ArrayList<String>           newLog;

    @FXML private VBox logMessages;

    @FXML
    private void close() {
        this.stage.close();
    }


    public synchronized void updateLog() {
        Iterator<String> iterator = newLog.iterator();
        while (iterator.hasNext()) {
            String cur = iterator.next();
            oldLog.add(cur);
            iterator.remove();
            logMessages.getChildren().add(new Label(cur));
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(e->{
            newLog.addAll(0,oldLog);
        });
    }

    public void setLog(ArrayList<String> log) {
        this.newLog = log;
    }
}
