package com.sentinella.james.gui.view;

import com.sentinella.james.MainWorker;
import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientLoginLayoutController {

    private WarlordVScumbagClient app;

    @FXML private TextField nameText;

    @FXML
    private void login() {
        app.login(nameText.getText());
    }

    @FXML
    private void checkEnter(KeyEvent event) {
        String button = event.getCode().toString();
        if (button.equalsIgnoreCase("enter")) login();
    }

    public void setMainApp(WarlordVScumbagClient app) {
        this.app = app;
    }
}
