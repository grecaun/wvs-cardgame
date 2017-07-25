package com.sentinella.james.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientScreenOptionsLayoutController {

    private Stage                       stage;
    private ClientRootLayoutController  rootController;

    @FXML private ToggleGroup   res;

    @FXML
    private void submit() {
        if (res.getSelectedToggle() != null) {
            switch (((RadioButton)res.getSelectedToggle()).getText()) {
                case "800x600":
                    rootController.setScreenSize(816.00,Objects.equals(System.getProperty("os.name"), "Mac OS X") ? 661.00 : 639.00);
                    break;
                case "1280x720":
                    rootController.setScreenSize(1280.00,720.00);
                    break;
                case "1920x1080":
                    rootController.setScreenSize(1920.00,1080.00);
                    break;
            }
        }
        this.stage.close();
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    void setRootController(ClientRootLayoutController rootController) {
        this.rootController = rootController;
    }
}
