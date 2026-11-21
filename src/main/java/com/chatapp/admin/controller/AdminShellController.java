package com.chatapp.admin.controller;

import java.io.IOException;
import java.util.List;

import com.chatapp.admin.dto.AdminProfile;
import com.chatapp.admin.navigation.AdminNavigator;
import com.chatapp.admin.navigation.AdminRoute;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    private AdminProfile adminProfile;

    private AdminDashboardController dashboardController;

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

        navigate(AdminRoute.DASHBOARD, dashboardButton);
    }

    @FXML
    private void showDashboard() {
        navigate(AdminRoute.DASHBOARD, dashboardButton);
    }

    @FXML
    private void showUsers() {
        navigate(AdminRoute.USERS, usersButton);
    }

    @FXML
    private void showLoginHistory() {
        navigate(AdminRoute.LOGIN_HISTORY, loginHistoryButton);
    }

    @FXML
    private void showGroups() {
        navigate(AdminRoute.GROUPS, groupsButton);
    }

    @FXML
    private void showSpamReports() {
        navigate(AdminRoute.SPAM_REPORTS, spamReportsButton);
    }

    @FXML
    private void showNewUsers() {
        navigate(AdminRoute.NEW_USERS, newUsersButton);
    }

    @FXML
    private void showFriendRelations() {
        navigate(AdminRoute.FRIEND_COUNTS, friendRelationsButton);
    }

    @FXML
    private void showActiveUsers() {
        navigate(AdminRoute.ACTIVE_USERS, activeUsersButton);
    }

    private void navigate(
        AdminRoute route,
        Button selectedButton
    ) {
        try {
            FXMLLoader loader = AdminNavigator.showRoute(
                contentHost,
                route
            );

            configureLoadedController(route, loader);
            activateMenu(selectedButton, route.getTitle());
        } catch (IOException exception) {
            System.err.println(
                "Không thể mở route: " + route.name()
            );
            exception.printStackTrace();
        }
    }

    private void configureLoadedController(
        AdminRoute route,
        FXMLLoader loader
    ) {
        dashboardController = null;

        if (route != AdminRoute.DASHBOARD) {
            return;
        }

        dashboardController = loader.getController();
        dashboardController.setNavigationHandler(
            this::navigateFromDashboard
        );

        if (adminProfile != null) {
            dashboardController.setAdminProfile(adminProfile);
        }
    }

    private void navigateFromDashboard(AdminRoute route) {
        Button targetButton = switch (route) {
            case USERS, USER_DETAIL -> usersButton;
            case LOGIN_HISTORY -> loginHistoryButton;
            case GROUPS, GROUP_DETAIL -> groupsButton;
            case SPAM_REPORTS, SPAM_REPORT_DETAIL ->
                spamReportsButton;
            case NEW_USERS -> newUsersButton;
            case FRIEND_COUNTS -> friendRelationsButton;
            case ACTIVE_USERS -> activeUsersButton;
            case DASHBOARD -> dashboardButton;
        };

        navigate(route, targetButton);
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
        adminProfile = profile;

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

        if (dashboardController != null) {
            dashboardController.setAdminProfile(profile);
        }
    }
}
