package com.sentinella.james.gui.view;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 * Created by James on 7/1/2017.
 */
public class ClientScreenOptionsLayoutController {

    private Stage                       stage;
    private ClientRootLayoutController  rootController;

    @FXML private ToggleGroup   res;
    @FXML private TextField     width;
    @FXML private TextField     height;

    @FXML
    private void submit() {
        if (res.getSelectedToggle() != null) {
            switch (((RadioButton)res.getSelectedToggle()).getText()) {
                case "600x400":
                    rootController.setScreenSize(600.00,439.00);
                    break;
                case "800x600":
                    rootController.setScreenSize(800.00,639.00);
                    break;
                case "1280x720":
                    rootController.setScreenSize(1280.00,720.00);
                    break;
                case "1920x1080":
                    rootController.setScreenSize(1920.00,1080.00);
                    break;
                default:
                    double newWidth  = Double.parseDouble(width.getText());
                    double newHeight = Double.parseDouble(height.getText());
                    rootController.setScreenSize(newWidth,newHeight);
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