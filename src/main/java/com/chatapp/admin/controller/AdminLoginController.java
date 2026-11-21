package com.chatapp.admin.controller;

import java.io.IOException;
import java.util.Optional;

import com.chatapp.admin.dto.AdminProfile;
import com.chatapp.admin.navigation.AdminNavigator;
import com.chatapp.admin.service.AdminAuthService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class AdminLoginController {

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
    private Label errorLabel;

    @FXML
    private Button loginButton;

    private final AdminAuthService authService =
        new AdminAuthService();

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

        passwordVisibilityButton.setAccessibleText(
            passwordVisible ? "Ẩn mật khẩu" : "Hiện mật khẩu"
        );

        TextField activePasswordField = passwordVisible
            ? visiblePasswordField
            : passwordField;

        activePasswordField.requestFocus();
        activePasswordField.positionCaret(
            activePasswordField.getText().length()
        );
    }

    @FXML
    private void handleLogin() {
        hideError();

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            showError(
                "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu."
            );
            return;
        }

        loginButton.setDisable(true);

        Optional<AdminProfile> authenticatedAdmin =
            authService.authenticate(username, password);

        if (authenticatedAdmin.isEmpty()) {
            loginButton.setDisable(false);
            passwordField.clear();

            if (passwordVisible) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }

            showError(
                "Tên đăng nhập hoặc mật khẩu không chính xác."
            );
            return;
        }

        Stage stage = (Stage) loginButton
            .getScene()
            .getWindow();

        try {
            AdminNavigator.showShell(
                stage,
                authenticatedAdmin.get()
            );
        } catch (IOException exception) {
            loginButton.setDisable(false);
            showError(
                "Không thể mở giao diện quản trị."
            );
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword() {
        showError(
            "Chức năng khôi phục mật khẩu quản trị "
            + "sẽ được triển khai ở bước xử lý tài khoản."
        );
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }
}
