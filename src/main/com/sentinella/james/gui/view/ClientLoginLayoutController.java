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
 * Created by James on 7/1/2017.
 */
public class ClientLoginLayoutController {

    private WarlordVScumbagClient app;

    @FXML private TextField ipText;
    @FXML private TextField portText;
    @FXML private TextField nameText;

    @FXML
    private void login() {
        app.login(ipText.getText(),portText.getText(),nameText.getText());
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
