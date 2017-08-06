package com.sentinella.james.gui.view;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.stage.Stage;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class HelpController {
    private Stage stage;
    private HostServices hostServices;

    public void setStage(Stage stage) {this.stage = stage;}
    public void setHostServices(HostServices hs) {this.hostServices=hs;}

    @FXML
    private void close() {
        stage.close();
    }

    @FXML
    private void handleURL(ActionEvent event) {
        hostServices.showDocument(((Hyperlink)event.getSource()).getText());
    }
}
