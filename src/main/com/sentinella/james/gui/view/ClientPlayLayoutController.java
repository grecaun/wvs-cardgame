package com.sentinella.james.gui.view;

import com.sentinella.james.MainWorker;
import com.sentinella.james.WvSUpdater;
import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

/**
 * Created by James on 6/30/2017.
 */
public class ClientPlayLayoutController implements WvSUpdater {

    @FXML private SplitPane leftPane;
    @FXML private TextField chatText;
    @FXML private VBox chatMessages;

    private WarlordVScumbagClient   client;
    private MainWorker              worker;

    @FXML
    private void sendChatMsg() {
        String newMessage = chatText.getText();
        if (newMessage.length() < 1) return;
        chatText.clear();
        worker.sendChat(newMessage);
    }

    @FXML
    private void checkEnter(KeyEvent event) {
        String keyPressed = event.getCode().toString();
        if (keyPressed.equalsIgnoreCase("enter")) sendChatMsg();
    }

    @Override
    public void updateTable() {

    }

    @Override
    public void updatePlayer() {

    }

    @Override
    public void updatePlayer(String name) {

    }

    @Override
    public void updateStatus() {

    }

    @Override
    public void updateLobby() {

    }

    @Override
    public void updateChat(String name, String message) {
        Platform.runLater(() -> chatMessages.getChildren().add(new Label(String.format("%8s: %s",name,message))));
    }

    @Override
    public void updateAll() {

    }

    @Override
    public void updateHand() {

    }

    public void setLeftPaneWidth(double v) {
        leftPane.setMaxWidth(v);
        leftPane.setMinWidth(v);
        chatText.setMaxWidth(v-45.0);
    }

    public void setClient(WarlordVScumbagClient client) {
        this.client = client;
    }

    public void setWorker(MainWorker worker) {
        this.worker = worker;
    }
}
