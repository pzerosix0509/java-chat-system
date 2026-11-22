package com.chatapp.user.controller;

import java.io.IOException;

import com.chatapp.user.navigation.UserNavigator;
import com.chatapp.user.navigation.UserRoute;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ForgotFailController {

    @FXML
    private Button backButton;

    @FXML
    private Button brandButton;

    @FXML
    private void handleBackToLogin() {
        navigate(UserRoute.LOGIN);
    }

    @FXML
    private void handleBrand() {
        navigate(UserRoute.REGISTER);
    }

    private void navigate(UserRoute route) {
        try {
            Stage stage = (Stage) backButton
                .getScene()
                .getWindow();
            UserNavigator.show(stage, route);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
