package com.sentinella.james.gui.view;

import javafx.fxml.FXML;
import javafx.stage.Stage;

/**
 * Created by James on 7/1/2017.
 */
public class HelpController {
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void close() {
        stage.close();
    }
}
