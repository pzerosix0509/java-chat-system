package com.chatapp.user.controller;

import java.io.IOException;

import com.chatapp.user.navigation.UserNavigator;
import com.chatapp.user.navigation.UserRoute;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button brandButton;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void handleRegister() {
        if (!passwordField.getText()
            .equals(confirmPasswordField.getText())) {

            errorLabel.setText("Your password does not match");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        navigate(UserRoute.MAIN_LAYOUT);
    }

    @FXML
    private void handleLogin() {
        navigate(UserRoute.LOGIN);
    }

    @FXML
    private void handleBrand() {
        navigate(UserRoute.MAIN_LAYOUT);
    }

    private void navigate(UserRoute route) {
        try {
            Stage stage = (Stage) emailField
                .getScene()
                .getWindow();

            if (route == UserRoute.MAIN_LAYOUT) {
                UserNavigator.showMain(stage, false);
            } else {
                UserNavigator.show(stage, route);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
