package com.sentinella.james.gui.view;

import com.sentinella.james.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
public class ClientPlayLayoutController implements WvSUpdater {

    private Stage primaryStage;

    @FXML private BorderPane leftPane;
    @FXML private TextField  chatText;
    @FXML private Button     sendChat;
    @FXML private VBox       chatMessages;
    @FXML private AnchorPane displPane;

    private double     messageWidth = 189.0;
    private ScreenSize screenSize = ScreenSize.SMALL;

    private ImageView[] playerAvatar = {null,null,null,null,null,null,null};
    private ImageView[] playerCardIcons = {null,null,null,null,null,null,null};
    private ImageView[] playerStrikeIcons = {null,null,null,null,null,null,null};
    private ImageView[] playerStatusIcons = {null,null,null,null,null,null,null};
    private Label[]     playerNames = {null,null,null,null,null,null,null};
    private Label[]     playerCards = {null,null,null,null,null,null,null};
    private Label[]     playerStrikes = {null,null,null,null,null,null,null};

    private CardImageView[] tableCards = {null,null,null,null};
    private CardImageView[] handCards = {null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
    private boolean[]   handCardSelected = {false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
    private int         numberHandCardSelected = 0;
    private int         maxHandCardsSelected = 4;

    private Label       myName;
    private String      myNameString;

    private Button      sendPlay;

    private Client                      client;
    private MainWorker                  worker;
    private ClientRootLayoutController  rootController;

    private LogBook     log = new GUILogBook();

    private String      cardDirString = "/com/sentinella/james/gui/view/images/cards-sm/";

    @FXML
    private void initialize() {
        for (int i=0; i<playerAvatar.length; i++) {
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
        for (int i=0; i<tableCards.length; i++) {
            tableCards[i] = new CardImageView();
            tableCards[i].setCard(cardDirString ,"02_clubs.png");
            tableCards[i].setPreserveRatio(true);
            tableCards[i].setSmooth(true);
            tableCards[i].setCache(true);
            //tableCards[i].setVisible(false);
            displPane.getChildren().add(tableCards[i]);
        }
        for (int i=0; i<handCards.length; i++) {
            handCards[i] = new CardImageView();
            handCards[i].setCard(cardDirString ,"02_clubs.png");
            handCards[i].setPreserveRatio(true);
            handCards[i].setSmooth(true);
            handCards[i].setCache(true);
            handCards[i].setOnMouseClicked(this::handCardClick);
            handCards[i].setId(String.valueOf(i));
            //handCards[i].setVisible(false);
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

        myName = new Label();
        myName.setText("You are John");
        myName.setVisible(false);
        displPane.getChildren().add(myName);

        chatMessages.setMinWidth(198);
        chatMessages.setMaxWidth(198);
        chatMessages.setAlignment(Pos.TOP_RIGHT);

        sendChat.setDisable(true);
        chatText.setDisable(true);
    }

    private void sendPlayButtonPress() {
        if (sendPlay.getText() == "Send Play") {
            int[] cards = {52, 52, 52, 52};
            int cardsIx = 0;
            PlayerHand hand = client.getHand();
            for (int i = 0; i < hand.count(); i++) {
                if (handCardSelected[i]) {
                    cards[cardsIx++] = hand.getHand().get(i).getCardIndexNumber();
                }
            }
            for (int card : cards) {
                hand.remove(card);
            }
            worker.sendPlay(cards);
            resetHand();
            runLaterUpdateHand(hand.getHand());
        } else {
            int card = 52;
            PlayerHand hand = client.getHand();
            for (int i = 0; i < hand.count(); i++) {
                if (handCardSelected[i]) {
                    card = hand.getHand().get(i).getCardIndexNumber();
                    break;
                }
            }
            hand.remove(card);
            worker.sendSwap(card);
            resetHand();
            runLaterUpdateHand(hand.getHand());
        }
    }

    private void resetHand() {
        for (int i=0;i<handCardSelected.length;i++) {
            if (handCardSelected[i]) {
                double oldTopAnchor = AnchorPane.getTopAnchor(handCards[i]);
                AnchorPane.setTopAnchor(handCards[i], oldTopAnchor + 20);
                handCardSelected[i] = false;
                numberHandCardSelected--;
            }
        }
    }

    public void enableChatSend() {
        sendChat.setDisable(false);
        chatText.setDisable(false);
    }

    private void handCardClick(MouseEvent event) {
        int cardNo = Integer.parseInt(((ImageView)event.getSource()).getId());
        double oldTopAnchor = AnchorPane.getTopAnchor(handCards[cardNo]);
        if (handCardSelected[cardNo]) {
            AnchorPane.setTopAnchor(handCards[cardNo], oldTopAnchor + 20);
            handCardSelected[cardNo] = false;
            numberHandCardSelected--;
        } else {
            if (numberHandCardSelected >= maxHandCardsSelected) return;
            AnchorPane.setTopAnchor(handCards[cardNo], oldTopAnchor - 20);
            handCardSelected[cardNo] = true;
            numberHandCardSelected++;
        }
    }

    public void updateView() {
        log.printDebMsg("ClientPlayLayoutController.updateView",3);
        // window attributes
        double  paneWidth   = primaryStage.getWidth();
        paneWidth -= paneWidth < 1200 ? 200 : 300;
        // settings for player images
        int     avatarWidth = (int) (paneWidth/8);
        int     spacer = 5;
        double  topStatusAnchor = 50.0, topAvatarAnchor, topNameAnchor, topCardAnchor, topStrikeAnchor;
        double  iconWidth, iconLabelLeftAnchorDiff, iconLabelDiff;

        double cardWidth, cardOffset, topTableCardAnchor, topHandCardAnchor, leftHandCardAnchor = 25.0, leftTableCardAnchor, sendButtonDiff, topMyNameAnchor;

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
                // Name Label Vals
                topMyNameAnchor = topHandCardAnchor + 101;
                myName.setFont(Font.font(25.0));
                cardDirString = "/com/sentinella/james/gui/view/images/cards-sm/";
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
                // Name Label Vals
                topMyNameAnchor = topHandCardAnchor + 119;
                myName.setFont(Font.font(35.0));
                cardDirString = "/com/sentinella/james/gui/view/images/cards-md/";
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
                // Name Label Vals
                topMyNameAnchor = topHandCardAnchor + 185;
                myName.setFont(Font.font(45.0));
                cardDirString = "/com/sentinella/james/gui/view/images/cards-lg/";
        }
        for (int i=0; i<playerNames.length; i++) {
            switch (screenSize) {
                case SMALL:
                    playerNames[i].setFont(Font.font(16.0));
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

        for (CardImageView tableCard : tableCards) {
            tableCard.setFitWidth(cardWidth);
            tableCard.updateCard(cardDirString);
            AnchorPane.setTopAnchor(tableCard, topTableCardAnchor);
            AnchorPane.setLeftAnchor(tableCard, leftTableCardAnchor);
            leftTableCardAnchor += cardWidth + spacer;
        }
        AnchorPane.setTopAnchor(sendPlay, topTableCardAnchor + sendButtonDiff);
        AnchorPane.setLeftAnchor(sendPlay, leftTableCardAnchor + 45);
        for (int i=0;i<handCards.length;i++) {
            handCards[i].setFitWidth(cardWidth);
            handCards[i].updateCard(cardDirString);
            if (handCardSelected[i]) {
                AnchorPane.setTopAnchor(handCards[i], topHandCardAnchor - 20);
            } else {
                AnchorPane.setTopAnchor(handCards[i], topHandCardAnchor);
            }
            AnchorPane.setLeftAnchor(handCards[i],leftHandCardAnchor);
            leftHandCardAnchor += cardOffset;
        }
        AnchorPane.setTopAnchor(myName,topMyNameAnchor);
        AnchorPane.setLeftAnchor(myName,50.0);
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
    public void updateTable(Table table) {
        log.printDebMsg("ClientPlayLayoutController.updateTable",3);
        Platform.runLater(() -> runLaterUpdateTable(table));
    }

    private void runLaterUpdateTable(Table table) {
        log.printDebMsg("ClientPlayLayoutController.runLaterUpdateTable",3);
        updateJoin(null);
        for (int i = 0; i < playerAvatar.length; i++) {
            StringBuilder avatarURL = new StringBuilder("/com/sentinella/james/gui/view/images/avatars/");
            switch (i) {
                case 0:
                    if (table.isNotRanked()) {
                        avatarURL.append("0avatar.png");
                    } else {
                        avatarURL.append("crown.png");
                    }
                    break;
                case 6:
                    if (table.isNotRanked()) {
                        avatarURL.append("6avatar.png");
                    } else {
                        avatarURL.append("hat.png");
                    }
                    break;
                default:
                    avatarURL.append(i);
                    avatarURL.append("avatar.png");
                    break;
            }
            playerStatusIcons[i].setVisible(false);
            if (table.getSeatStatus(i) == pStatus.EMPTY) {
                playerAvatar[i].setImage(new Image("/com/sentinella/james/gui/view/images/avatars/chair.png"));
                playerAvatar[i].setVisible(true);
                playerNames[i].setVisible(false);
                playerCardIcons[i].setVisible(false);
                playerCards[i].setVisible(false);
                playerStrikeIcons[i].setVisible(false);
                playerStrikes[i].setVisible(false);
            } else {
                playerStatusIcons[i].setVisible(false);
                switch (table.getSeatStatus(i)) {
                    case ACTIVE:
                        playerStatusIcons[i].setImage(new Image("/com/sentinella/james/gui/view/images/status/active.png"));
                        playerStatusIcons[i].setVisible(true);
                        break;
                    case DISCONNECTED:
                        playerStatusIcons[i].setImage(new Image("/com/sentinella/james/gui/view/images/status/dc.png"));
                        playerStatusIcons[i].setVisible(true);
                        break;
                    case PASSED:
                        playerStatusIcons[i].setImage(new Image("/com/sentinella/james/gui/view/images/status/passed.png"));
                        playerStatusIcons[i].setVisible(true);
                        break;
                }
                playerAvatar[i].setImage(new Image(avatarURL.toString()));
                playerAvatar[i].setVisible(true);
                String thisPlayerName = table.getPlayerbySeat(i).trim();
                playerNames[i].setText(thisPlayerName.equalsIgnoreCase(myNameString) ? "You" : thisPlayerName);
                playerNames[i].setVisible(true);
                playerCardIcons[i].setVisible(true);
                playerCards[i].setText(String.valueOf(table.getCardsLeftBySeat(i)));
                playerCards[i].setVisible(true);
                playerStrikeIcons[i].setVisible(true);
                playerStrikes[i].setText(String.valueOf(table.getStrikesBySeat(i)));
                playerStrikes[i].setVisible(true);
            }
        }
        int[] inPlay = table.getInPlay();
        for (int i = 0; i < tableCards.length; i++) {
            if (inPlay[i] < 52) {
                int cardV = inPlay[i] / 4;
                int cardS = inPlay[i] % 4;
                StringBuilder url = new StringBuilder();
                switch (cardV) {
                    case 8:
                        url.append("ja_");
                        break;
                    case 9:
                        url.append("qu_");
                        break;
                    case 10:
                        url.append("ki_");
                        break;
                    case 11:
                        url.append("ac_");
                        break;
                    case 12:
                        url.append("02_");
                        break;
                    default:
                        url.append(String.format("%02d_", cardV + 3));
                }
                switch (cardS) {
                    case 0:
                        url.append("clubs.png");
                        break;
                    case 1:
                        url.append("diamonds.png");
                        break;
                    case 2:
                        url.append("hearts.png");
                        break;
                    default:
                        url.append("spades.png");
                }
                tableCards[i].setCard(cardDirString,url.toString());
                tableCards[i].setVisible(true);
            } else {
                tableCards[i].setVisible(false);
            }
        }
        sendPlay.setVisible(true);
        runLaterUpdateStatus(table);
    }

    @Override
    public void updateJoin(String name) {
        log.printDebMsg("ClientPlayLayoutController.updatePlayer(name)",3);
        Platform.runLater(()-> runLaterUpdateJoin(client.getName()));
    }

    private void runLaterUpdateJoin(String name) {
        log.printDebMsg("ClientPlayLayoutController.runLaterUpdateJoin",3);
        myNameString = name.trim();
        runLaterUpdateStatus(client.getTable());
    }

    @Override
    public void updateStatus(Table table) {
        log.printDebMsg("ClientPlayLayoutController.updateStatus",3);
        Platform.runLater(() -> runLaterUpdateStatus(client.getTable()));
    }

    private void runLaterUpdateStatus(Table table) {
        log.printDebMsg("ClientPlayLayoutController.runLaterUpdateStatus",3);
        String statusString;
        switch (table.getPlayerStatus(myNameString)) {
            case PASSED:
                statusString = " You were passed.";
                break;
            case DISCONNECTED:
                statusString = " You are a cat.";
                break;
            case WAITING:
                statusString = " You are waiting.";
                break;
            default:
                statusString = "";
        }
        switch (client.getState()) {
            case SWAP: // waiting for swap message
                statusString = " Select a card for the Scumbag.";
                sendPlay.setDisable(false);
                sendPlay.setText("Send Swap");
                resetHand();
                maxHandCardsSelected = 1;
                break;
            case CLIENTTURN: // waiting for play
                statusString = " It is your turn.";
                sendPlay.setDisable(false);
                sendPlay.setText("Send Play");
                resetHand();
                maxHandCardsSelected = 4;
                break;
            default:
                sendPlay.setDisable(true);
                sendPlay.setText("Waiting");
        }
        myName.setText(String.format("Your name is %s.%s", myNameString, statusString));
        myName.setVisible(true);
    }

    @Override
    public void updateLobby(ArrayList<String> names) {
        log.printDebMsg("ClientPlayLayoutController.updateLobby",3);
        Platform.runLater(() -> rootController.updateLobby(names));
    }

    @Override
    public void updateChat(String name, String message) {
        log.printDebMsg("ClientPlayLayoutController.updateChat",3);
        Platform.runLater(() -> runLaterUpdateChat(name,message));
    }

    public void runLaterUpdateChat(String name, String message) {
        log.printDebMsg("ClientPlayLayoutController.runLaterUpdateChat",3);
        Label newMsg = new Label(String.format("%8s: %s", name, message));
        newMsg.setWrapText(true);
        newMsg.setMinWidth(messageWidth);
        newMsg.setMaxWidth(messageWidth);
        chatMessages.getChildren().add(newMsg);
    }

    @Override
    public void updateHand(ArrayList<Card> cards) {
        log.printDebMsg("ClientPlayLayoutController.updateHand",3);
        Platform.runLater(() -> runLaterUpdateHand(cards));
    }

    @Override
    public void updateStrike(int strikeVal, int numStrikes) {

    }

    @Override
    public void updateSwapW(Card newCard) {

    }

    @Override
    public void updateSwapS(Card newCard, Card oldCard) {

    }

    @Override
    public void setLogBookInfo(LogBook log, String debugStr) {
        this.log = LogBookFactory.getLogBook(log, debugStr);
    }

    private void runLaterUpdateHand(ArrayList<Card> cards) {
        log.printDebMsg("ClientPlayLayoutController.runLaterUpdateHand",3);
        int index;
        for (index = 0; index < cards.size(); index++) {
            StringBuilder url = new StringBuilder();
            int cardNo = cards.get(index).getCardNumericFaceValue();
            int cardSuit = cards.get(index).getCardIndexNumber() % 4;
            switch (cardNo) {
                case 8:
                    url.append("ja_");
                    break;
                case 9:
                    url.append("qu_");
                    break;
                case 10:
                    url.append("ki_");
                    break;
                case 11:
                    url.append("ac_");
                    break;
                case 12:
                    url.append("02_");
                    break;
                default:
                    url.append(String.format("%02d_", cardNo + 3));
            }
            switch (cardSuit) {
                case 0:
                    url.append("clubs.png");
                    break;
                case 1:
                    url.append("diamonds.png");
                    break;
                case 2:
                    url.append("hearts.png");
                    break;
                default:
                    url.append("spades.png");
            }
            handCards[index].setCard(cardDirString,url.toString());
            handCards[index].setVisible(true);
        }
        for (; index < 18; index++) {
            handCards[index].setVisible(false);
        }
    }

    // non interface/callback methods
    public void updateLeftPane(double width) {
        switch (screenSize) {
            case SMALL:
                messageWidth = width - 11;
                break;
            case MEDIUM:
                messageWidth = width - 21;
                break;
            case LARGE:
                messageWidth = width - 37;
                break;
            default:
        }
        leftPane.setMaxWidth(width);
        leftPane.setMinWidth(width);
        chatMessages.setMinWidth(width-2);
        chatMessages.setMaxWidth(width-2);
        chatText.setMaxWidth(width-55.0);
        chatText.setMinWidth(width-55.0);
        for (Node node : chatMessages.getChildren()) {
            ((Label) node).setMinWidth(messageWidth);
            ((Label) node).setMaxWidth(messageWidth);
        }//*/
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

    private class CardImageView extends ImageView {
        String curCard      = "02_clubs.png";

        public void setCard(String cardDir, String cardFilename) {
            curCard = cardFilename;
            this.setImage(new Image(cardDir+curCard));
        }

        public void updateCard(String cardDir) {
            this.setImage(new Image(cardDir+curCard));
        }
    }
}
