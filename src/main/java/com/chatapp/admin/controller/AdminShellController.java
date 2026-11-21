package com.chatapp.admin.controller;

import java.io.IOException;
import java.util.List;

import com.chatapp.admin.dto.AdminProfile;
import com.chatapp.admin.navigation.AdminNavigator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AdminShellController {

    @FXML
    private StackPane contentHost;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button usersButton;

    @FXML
    private Button loginHistoryButton;

    @FXML
    private Button groupsButton;

    @FXML
    private Button spamReportsButton;

    @FXML
    private Button newUsersButton;

    @FXML
    private Button friendRelationsButton;

    @FXML
    private Button activeUsersButton;

    @FXML
    private Label adminInitialsLabel;

    @FXML
    private Label adminNameLabel;

    @FXML
    private Label adminEmailLabel;

    @FXML
    private Label topbarAdminInitialsLabel;

    @FXML
    private Label topbarAdminNameLabel;

    @FXML
    private Label topbarAdminRoleLabel;

    private List<Button> navigationButtons;

    @FXML
    private void initialize() {
        navigationButtons = List.of(
            dashboardButton,
            usersButton,
            loginHistoryButton,
            groupsButton,
            spamReportsButton,
            newUsersButton,
            friendRelationsButton,
            activeUsersButton
        );

        activateMenu(dashboardButton, "Dashboard");

    }

    @FXML
    private void showDashboard() {
        activateMenu(dashboardButton, "Dashboard");
    }

    @FXML
    private void showUsers() {
        activateMenu(usersButton, "Quản lý người dùng");
    }

    @FXML
    private void showLoginHistory() {
        activateMenu(
            loginHistoryButton,
            "Lịch sử đăng nhập"
        );
    }

    @FXML
    private void showGroups() {
        activateMenu(groupsButton, "Nhóm chat");
    }

    @FXML
    private void showSpamReports() {
        activateMenu(
            spamReportsButton,
            "Báo cáo spam"
        );
    }

    @FXML
    private void showNewUsers() {
        activateMenu(
            newUsersButton,
            "Người dùng đăng ký mới"
        );
    }

    @FXML
    private void showFriendRelations() {
        activateMenu(
            friendRelationsButton,
            "Quan hệ bạn bè"
        );
    }

    @FXML
    private void showActiveUsers() {
        activateMenu(
            activeUsersButton,
            "Người dùng hoạt động"
        );
    }

    @FXML
    private void handleLogout() {
        Stage stage = (Stage) contentHost
            .getScene()
            .getWindow();

        try {
            AdminNavigator.showLogin(stage);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void activateMenu(
        Button selectedButton,
        String pageTitle
    ) {
        for (Button button : navigationButtons) {
            button.getStyleClass().remove(
                "nav-button-active"
            );
        }

        if (!selectedButton
                .getStyleClass()
                .contains("nav-button-active")) {

            selectedButton
                .getStyleClass()
                .add("nav-button-active");
        }

        pageTitleLabel.setText(pageTitle);
    }

    public void setAdminProfile(AdminProfile profile) {
        adminInitialsLabel.setText(profile.avatarText());
        adminNameLabel.setText(profile.displayName());
        adminEmailLabel.setText(profile.email());

        topbarAdminInitialsLabel.setText(
            profile.avatarText()
        );
        topbarAdminNameLabel.setText(
            profile.displayName()
        );
        topbarAdminRoleLabel.setText(profile.role());
    }
}
