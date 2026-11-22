package com.chatapp.user.controller;

import java.io.IOException;

import com.chatapp.user.navigation.UserNavigator;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class MainLayoutController {

    @FXML
    private StackPane contentHost;

    private boolean groupMode;

    @FXML
    private void initialize() {
        // Rendered once UserNavigator supplies the initial mode.
    }

    public void setGroupMode(boolean groupMode) {
        this.groupMode = groupMode;
        render();
    }

    private void render() {
        String fxmlPath = groupMode
            ? "/fxml/user/group_chat.fxml"
            : "/fxml/user/chat.fxml";

        try {
            FXMLLoader loader = UserNavigator.createLoader(fxmlPath);
            Parent page = loader.load();
            contentHost.getChildren().setAll(page);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
