package com.chatapp.user.controller;

import java.io.IOException;

import com.chatapp.user.navigation.UserNavigator;
import com.chatapp.user.navigation.UserRoute;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Button brandButton;

    @FXML
    private void handleSend() {
        String email = emailField.getText().toLowerCase();

        UserRoute result = email.contains("fail")
            ? UserRoute.FORGOT_FAIL
            : UserRoute.FORGOT_SUCCESS;

        try {
            Stage stage = (Stage) emailField
                .getScene()
                .getWindow();
            UserNavigator.show(stage, result);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleBrand() {
        navigate(UserRoute.REGISTER);
    }

    private void navigate(UserRoute route) {
        try {
            Stage stage = (Stage) emailField
                .getScene()
                .getWindow();
            UserNavigator.show(stage, route);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
