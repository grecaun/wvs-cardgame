package com.sentinella.james.gui; /**
 * Copyright (c) 2017 James Sentinella.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import com.sentinella.james.Server;
import com.sentinella.james.gui.view.ServerRootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class WarlordVScumbagServer extends Application {
    private Stage                       primaryStage;
    private BorderPane                  rootLayout;
    private ServerRootLayoutController  rootController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Warlords Vs Scumbags");
        this.primaryStage.setResizable(false);

        initRootLayout();
    }

    @Override
    public void stop() {
        if (rootController != null) rootController.closeEverything();
    }

    private void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(WarlordVScumbagServer.class.getResource("view/ServerRootLayout.fxml"));
            rootLayout = loader.load();
            primaryStage.setScene(new Scene(rootLayout));
            primaryStage.show();
            rootController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
