package com.chatapp.user.controller;

import java.io.IOException;

import com.chatapp.user.component.AvatarView;
import com.chatapp.user.navigation.UserNavigator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ProfileController {

    @FXML
    private StackPane profileAvatar;

    @FXML
    private Button backButton;

    @FXML
    private Button editButton;

    @FXML
    private void initialize() {
        profileAvatar.getChildren().setAll(
            new AvatarView("T", "#FF7700", true, 113)
        );
    }

    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) backButton
                .getScene()
                .getWindow();
            UserNavigator.showMain(stage, false);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
