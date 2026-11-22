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
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private Button passwordVisibilityButton;

    @FXML
    private FontIcon passwordVisibilityIcon;

    @FXML
    private Button brandButton;

    @FXML
    private Label errorLabel;

    private boolean passwordVisible;

    @FXML
    private void initialize() {
        visiblePasswordField.textProperty()
            .bindBidirectional(passwordField.textProperty());
        hideError();
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);
        visiblePasswordField.setVisible(passwordVisible);
        visiblePasswordField.setManaged(passwordVisible);

        passwordVisibilityIcon.setIconLiteral(
            passwordVisible ? "fas-eye-slash" : "fas-eye"
        );

        TextField active = passwordVisible
            ? visiblePasswordField
            : passwordField;
        active.requestFocus();
        active.positionCaret(active.getText().length());
    }

    @FXML
    private void handleLogin() {
        if (usernameField.getText().isBlank()
            || passwordField.getText().isBlank()) {

            errorLabel.setText(
                "Vui lòng nhập tên đăng nhập và mật khẩu."
            );
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        navigate(UserRoute.MAIN_LAYOUT);
    }

    @FXML
    private void handleForgotPassword() {
        navigate(UserRoute.FORGOT_PASSWORD);
    }

    @FXML
    private void handleRegister() {
        navigate(UserRoute.REGISTER);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void navigate(UserRoute route) {
        try {
            Stage stage = (Stage) usernameField
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
