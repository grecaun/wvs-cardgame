package com.sentinella.james.gui.view;

import com.sentinella.james.MainWorker;
import com.sentinella.james.WvSUpdater;
import com.sentinella.james.gui.WarlordVScumbagClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

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

    private WarlordVScumbagClient   client;
    private MainWorker              worker;

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
    }

    public void updateView() {
        double  paneWidth   = primaryStage.getWidth();
        paneWidth -= paneWidth < 1200 ? 200 : 300;
        double  paneHeight  = primaryStage.getHeight() - 39.0;
        int     avatarWidth = (int) (paneWidth/8);
        int     spacer = 5;
        double  topStatusAnchor = 50.0, topAvatarAnchor, topNameAnchor, topCardAnchor, topStrikeAnchor;
        double  iconWidth, iconLabelLeftAnchorDiff, iconLabelDiff;
        ScreenSize screenSize;
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
        System.out.println(String.format("PaneWidth: %f PaneHeight: %f AvatarWidth: %d Anchor: %f",paneWidth,paneHeight,avatarWidth,anchor));switch (screenSize) {
            case SMALL:
                topAvatarAnchor = topStatusAnchor + 15.0;
                topNameAnchor = topAvatarAnchor + avatarWidth + 5.0;
                topCardAnchor = topNameAnchor + 26.0;
                topStrikeAnchor = topCardAnchor + 25;
                iconWidth = 13; // height 20
                iconLabelLeftAnchorDiff = 18;
                iconLabelDiff = 2;
                break;
            case MEDIUM:
                topAvatarAnchor = topStatusAnchor + 24.0;
                topNameAnchor = topAvatarAnchor + avatarWidth;
                topCardAnchor = topNameAnchor + 30.0;
                topStrikeAnchor = topCardAnchor + 30;
                iconWidth = 16; // height 25
                iconLabelLeftAnchorDiff = 21;
                iconLabelDiff = 4;
                break;
            default:
                topAvatarAnchor = topStatusAnchor + 40.0;
                topNameAnchor = topAvatarAnchor + avatarWidth;
                topCardAnchor = topNameAnchor + 34.0;
                topStrikeAnchor = topCardAnchor + 35;
                iconWidth = 19; // height 30
                iconLabelLeftAnchorDiff = 24;
                iconLabelDiff = 6;
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

    public void updateLeftPane(double width) {
        leftPane.setMaxWidth(width);
        leftPane.setMinWidth(width);
        chatText.setMaxWidth(width-45.0);
        chatText.setMinWidth(width-45.0);
    }

    public void setClient(WarlordVScumbagClient client) {
        this.client = client;
    }

    public void setWorker(MainWorker worker) {
        this.worker = worker;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private enum ScreenSize {SMALL, MEDIUM, LARGE}
}
