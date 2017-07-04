package com.sentinella.james.gui.view;

import com.sentinella.james.*;
import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by James on 6/30/2017.
 */
public class ClientPlayLayoutController implements WvSUpdater {

    private Stage primaryStage;

    @FXML private BorderPane leftPane;
    @FXML private TextField  chatText;
    @FXML private VBox       chatMessages;
    @FXML private AnchorPane displPane;

    private ImageView[] playerAvatar = {null,null,null,null,null,null,null};
    private ImageView[] playerCardIcons = {null,null,null,null,null,null,null};
    private ImageView[] playerStrikeIcons = {null,null,null,null,null,null,null};
    private ImageView[] playerStatusIcons = {null,null,null,null,null,null,null};
    private Label[]     playerNames = {null,null,null,null,null,null,null};
    private Label[]     playerCards = {null,null,null,null,null,null,null};
    private Label[]     playerStrikes = {null,null,null,null,null,null,null};

    private ImageView[] tableCards = {null,null,null,null};
    private ImageView[] handCards = {null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
    private boolean[]   handCardSelected = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
    private int         numberHandCardSelected = 0;

    private Button      sendPlay;

    private Client                      client;
    private MainWorker                  worker;
    private ClientRootLayoutController  rootController;
    private ScreenSize                  screenSize;

    @FXML
    private void initialize() {
        for (int i=0; i<7; i++) {
            playerAvatar[i] = new ImageView();
            playerAvatar[i].setImage(new Image("/com/sentinella/james/gui/view/images/avatars/chair.png"));
            playerAvatar[i].setPreserveRatio(true);
            playerAvatar[i].setSmooth(true);
            playerAvatar[i].setCache(true);
            playerAvatar[i].setVisible(false);
            displPane.getChildren().add(playerAvatar[i]);

            playerCardIcons[i] = new ImageView();
            playerCardIcons[i].setImage(new Image("/com/sentinella/james/gui/view/images/icons/card_back.png"));
            playerCardIcons[i].setPreserveRatio(true);
            playerCardIcons[i].setSmooth(true);
            playerCardIcons[i].setCache(true);
            playerCardIcons[i].setVisible(false);
            displPane.getChildren().add(playerCardIcons[i]);

            playerStrikeIcons[i] = new ImageView();
            playerStrikeIcons[i].setImage(new Image("/com/sentinella/james/gui/view/images/icons/strike.png"));
            playerStrikeIcons[i].setPreserveRatio(true);
            playerStrikeIcons[i].setSmooth(true);
            playerStrikeIcons[i].setCache(true);
            playerStrikeIcons[i].setVisible(false);
            displPane.getChildren().add(playerStrikeIcons[i]);

            playerStatusIcons[i] = new ImageView();
            playerStatusIcons[i].setImage(new Image("/com/sentinella/james/gui/view/images/status/blank.png"));
            playerStatusIcons[i].setPreserveRatio(true);
            playerStatusIcons[i].setSmooth(true);
            playerStatusIcons[i].setCache(true);
            playerStatusIcons[i].setVisible(false);
            displPane.getChildren().add(playerStatusIcons[i]);

            playerNames[i] = new Label();
            playerNames[i].setText(String.format("Player%d",i+1));
            playerNames[i].setVisible(false);
            displPane.getChildren().add(playerNames[i]);

            playerCards[i] = new Label();
            playerCards[i].setText("0");
            playerCards[i].setVisible(false);
            displPane.getChildren().add(playerCards[i]);

            playerStrikes[i] = new Label();
            playerStrikes[i].setText("0");
            playerStrikes[i].setVisible(false);
            displPane.getChildren().add(playerStrikes[i]);
        }
        for (int i=0; i<4; i++) {
            tableCards[i] = new ImageView();
            tableCards[i].setImage(new Image("/com/sentinella/james/gui/view/images/playingcards/02_clubs.png"));
            tableCards[i].setPreserveRatio(true);
            tableCards[i].setSmooth(true);
            tableCards[i].setCache(true);
            tableCards[i].setVisible(false);
            displPane.getChildren().add(tableCards[i]);
        }
        for (int i=0; i<18; i++) {
            handCards[i] = new ImageView();
            handCards[i].setImage(new Image("/com/sentinella/james/gui/view/images/playingcards/02_clubs.png"));
            handCards[i].setPreserveRatio(true);
            handCards[i].setSmooth(true);
            handCards[i].setCache(true);
            handCards[i].setOnMouseClicked(this::handCardClick);
            handCards[i].setId(String.valueOf(i));
            handCards[i].setVisible(false);
            displPane.getChildren().add(handCards[i]);
        }
        sendPlay = new Button();
        sendPlay.setText("Send Play");
        sendPlay.setMaxWidth(100);
        sendPlay.setMinWidth(100);
        sendPlay.setMaxHeight(50);
        sendPlay.setMinHeight(50);
        sendPlay.setOnAction(e -> sendPlayButtonPress());
        sendPlay.setVisible(false);
        displPane.getChildren().add(sendPlay);
    }

    private void sendPlayButtonPress() {
    }

    private void handCardClick(MouseEvent event) {
        int cardNo = Integer.parseInt(((ImageView)event.getSource()).getId());
        double oldTopAnchor = AnchorPane.getTopAnchor(handCards[cardNo]);
        if (handCardSelected[cardNo]) {
            AnchorPane.setTopAnchor(handCards[cardNo], oldTopAnchor + 20);
            handCardSelected[cardNo] = false;
            numberHandCardSelected--;
        } else {
            if (numberHandCardSelected > 3) return;
            AnchorPane.setTopAnchor(handCards[cardNo], oldTopAnchor - 20);
            handCardSelected[cardNo] = true;
            numberHandCardSelected++;
        }
        System.out.println(String.format("Card number %d selected. There are %d cards selected.", cardNo, numberHandCardSelected));
    }

    public void updateView() {
        // window attributes
        double  paneWidth   = primaryStage.getWidth();
        paneWidth -= paneWidth < 1200 ? 200 : 300;
        // settings for player images
        int     avatarWidth = (int) (paneWidth/8);
        int     spacer = 5;
        double  topStatusAnchor = 50.0, topAvatarAnchor, topNameAnchor, topCardAnchor, topStrikeAnchor;
        double  iconWidth, iconLabelLeftAnchorDiff, iconLabelDiff;

        double cardWidth, cardOffset, topTableCardAnchor, topHandCardAnchor, leftHandCardAnchor = 25.0, leftTableCardAnchor, sendButtonDiff;

        switch (avatarWidth) {
            case 122:
                spacer = 10;
                screenSize = ScreenSize.MEDIUM;
                break;
            case 202:
                spacer = 15;
                screenSize = ScreenSize.LARGE;
                break;
            default:
                spacer = 5;
                screenSize = ScreenSize.SMALL;
        }
        double anchor = (paneWidth - ((spacer*6) + (avatarWidth*7)))/2;
        switch (screenSize) {
            case SMALL:
                // Players @ Table
                topAvatarAnchor = topStatusAnchor + 15.0;
                topNameAnchor = topAvatarAnchor + avatarWidth + 5.0;
                topCardAnchor = topNameAnchor + 26.0;
                topStrikeAnchor = topCardAnchor + 25;
                iconWidth = 13; // height 20
                iconLabelLeftAnchorDiff = 18;
                iconLabelDiff = 2;
                // Cards
                cardWidth  = 50;
                cardOffset = 30;
                topTableCardAnchor = topStrikeAnchor + 78;
                topHandCardAnchor = topTableCardAnchor + 151;
                leftTableCardAnchor = 125;
                sendButtonDiff = 11;
                break;
            case MEDIUM:
                // Players @ Table
                topAvatarAnchor = topStatusAnchor + 24.0;
                topNameAnchor = topAvatarAnchor + avatarWidth;
                topCardAnchor = topNameAnchor + 30.0;
                topStrikeAnchor = topCardAnchor + 30;
                iconWidth = 16; // height 25
                iconLabelLeftAnchorDiff = 21;
                iconLabelDiff = 4;
                // Cards
                cardWidth  = 75;
                cardOffset = 50;
                topTableCardAnchor = topStrikeAnchor + 60;
                topHandCardAnchor = topTableCardAnchor + 169;
                leftTableCardAnchor = 250;
                sendButtonDiff = 29;
                break;
            default:
                // Players @ Table
                topAvatarAnchor = topStatusAnchor + 40.0;
                topNameAnchor = topAvatarAnchor + avatarWidth;
                topCardAnchor = topNameAnchor + 34.0;
                topStrikeAnchor = topCardAnchor + 35;
                iconWidth = 19; // height 30
                iconLabelLeftAnchorDiff = 24;
                iconLabelDiff = 6;
                // Cards
                cardWidth  = 100;
                cardOffset = 86;
                topTableCardAnchor = topStrikeAnchor + 120;
                topHandCardAnchor = topTableCardAnchor + 265;
                leftTableCardAnchor = 500;
                sendButtonDiff = 47;
        }
        for (int i=0; i<7; i++) {
            switch (screenSize) {
                case SMALL:
                    playerNames[i].setFont(Font.font(18.0));
                    playerCards[i].setFont(Font.font(12.0));
                    playerStrikes[i].setFont(Font.font(12.0));
                    break;
                case MEDIUM:
                    playerNames[i].setFont(Font.font(22.0));
                    playerCards[i].setFont(Font.font(15.0));
                    playerStrikes[i].setFont(Font.font(15.0));
                    break;
                default:
                    playerNames[i].setFont(Font.font(26.0));
                    playerCards[i].setFont(Font.font(18.0));
                    playerStrikes[i].setFont(Font.font(18.0));
            }
            // status icons
            playerStatusIcons[i].setFitWidth((double)avatarWidth);
            AnchorPane.setTopAnchor(playerStatusIcons[i],topStatusAnchor);
            AnchorPane.setLeftAnchor(playerStatusIcons[i],anchor);

            // avatars
            playerAvatar[i].setFitWidth((double)avatarWidth);
            AnchorPane.setTopAnchor(playerAvatar[i],topAvatarAnchor);
            AnchorPane.setLeftAnchor(playerAvatar[i],anchor);

            // Names
            playerNames[i].setMinWidth((double)avatarWidth);
            playerNames[i].setMaxWidth((double)avatarWidth);
            AnchorPane.setTopAnchor(playerNames[i],topNameAnchor);
            AnchorPane.setLeftAnchor(playerNames[i],anchor);

            // Card Icons
            playerCardIcons[i].setFitWidth(iconWidth);
            AnchorPane.setTopAnchor(playerCardIcons[i],topCardAnchor);
            AnchorPane.setLeftAnchor(playerCardIcons[i],anchor);

            // Card Labels
            AnchorPane.setTopAnchor(playerCards[i], topCardAnchor + iconLabelDiff);
            AnchorPane.setLeftAnchor(playerCards[i], anchor + iconLabelLeftAnchorDiff);

            // Strike Icons
            playerStrikeIcons[i].setFitWidth(iconWidth);
            AnchorPane.setTopAnchor(playerStrikeIcons[i],topStrikeAnchor);
            AnchorPane.setLeftAnchor(playerStrikeIcons[i],anchor);

            // Strike Labels
            AnchorPane.setTopAnchor(playerStrikes[i], topStrikeAnchor + iconLabelDiff);
            AnchorPane.setLeftAnchor(playerStrikes[i], anchor + iconLabelLeftAnchorDiff);

            anchor += (spacer + avatarWidth);
        }

        for (int i=0;i<4;i++) {
            tableCards[i].setFitWidth(cardWidth);
            AnchorPane.setTopAnchor(tableCards[i],topTableCardAnchor);
            AnchorPane.setLeftAnchor(tableCards[i],leftTableCardAnchor);
            leftTableCardAnchor += cardWidth + spacer;
        }
        AnchorPane.setTopAnchor(sendPlay, topTableCardAnchor + sendButtonDiff);
        AnchorPane.setLeftAnchor(sendPlay, leftTableCardAnchor + 45);
        for (int i=0;i<18;i++) {
            handCards[i].setFitWidth(cardWidth);
            if (handCardSelected[i]) {
                AnchorPane.setTopAnchor(handCards[i], topHandCardAnchor - 20);
            } else {
                AnchorPane.setTopAnchor(handCards[i], topHandCardAnchor);
            }
            AnchorPane.setLeftAnchor(handCards[i],leftHandCardAnchor);
            leftHandCardAnchor += cardOffset;
        }
    }

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
    public void updateLobby(ArrayList<String> names) {
        Platform.runLater(() -> rootController.updateLobby(names));
    }

    @Override
    public void updateChat(String name, String message) {
        Platform.runLater(() -> chatMessages.getChildren().add(new Label(String.format("%8s: %s",name,message))));
    }

    @Override
    public void updateAll() {

    }

    @Override
    public void updateHand(ArrayList<Card> cards) {

    }

    public void updateLeftPane(double width) {
        leftPane.setMaxWidth(width);
        leftPane.setMinWidth(width);
        chatText.setMaxWidth(width-45.0);
        chatText.setMinWidth(width-45.0);
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setWorker(MainWorker worker) {
        this.worker = worker;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setRootController(ClientRootLayoutController rootController) {
        this.rootController = rootController;
    }

    private enum ScreenSize {SMALL, MEDIUM, LARGE}
}
