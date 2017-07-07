package com.sentinella.james.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by James on 7/6/2017.
 */
public class ClientSettingsLayoutController {
    private ClientRootLayoutController.ClientOptionHolder options;
    private Stage                                         stage;

    @FXML private TextField port;
    @FXML private TextField ip;

    @FXML
    private void save() {
        if (ip.getText().trim().length() > 0) options.setHostname(ip.getText().trim());
        if (port.getText().trim().length() > 0) {
            try {
                options.setHostport(Integer.parseInt(port.getText().trim()));
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Invalid port number.", ButtonType.CLOSE).showAndWait();
                return;
            }
        }
        cancel();
    }

    @FXML
    private void cancel() {
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOptions(ClientRootLayoutController.ClientOptionHolder options) {
        this.options = options;
        port.setPromptText(String.valueOf(options.getHostport()));
        ip.setPromptText(options.getHostname());
    }
}
