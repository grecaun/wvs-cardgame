package com.sentinella.james.gui.view;

import com.sentinella.james.ClientCallback;
import com.sentinella.james.WvSUpdater;
import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by James on 6/30/2017.
 */
public class ClientPlayLayoutController implements ClientCallback, WvSUpdater{

    @FXML private SplitPane leftPane;
    @FXML private TextField chatText;
    @FXML private VBox chatMessages;

    @FXML private Button submitChat;

    private WarlordVScumbagClient client;

    @FXML
    private void updateChat() {
        String newMessage = chatText.getText();
        if (newMessage.length() < 1) return;
        chatText.clear();
        chatMessages.getChildren().add(new Label(newMessage));
    }

    @FXML
    private void checkEnter(KeyEvent event) {
        String keyPressed = event.getCode().toString();
        if (keyPressed.equalsIgnoreCase("enter")) updateChat();
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

    }

    @Override
    public void updateAll() {

    }

    @Override
    public void updateHand() {

    }

    @Override
    public void finished() {

    }

    @Override
    public void setOutConnection(PrintWriter out) {

    }

    public void setLeftPaneWidth(double v) {
        leftPane.setMaxWidth(v);
        leftPane.setMinWidth(v);
        chatText.setMaxWidth(v-45.0);
    }
}
