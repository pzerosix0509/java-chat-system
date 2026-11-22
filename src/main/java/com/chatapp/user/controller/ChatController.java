package com.chatapp.user.controller;

import java.io.IOException;

import com.chatapp.user.component.AvatarView;
import com.chatapp.user.navigation.UserNavigator;
import com.chatapp.user.navigation.UserRoute;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Direct-message variant of the main client screen. */
public class ChatController {

    @FXML
    private StackPane conversationAvatar1;
    @FXML
    private StackPane conversationAvatar2;
    @FXML
    private StackPane conversationAvatar3;
    @FXML
    private StackPane conversationAvatar4;
    @FXML
    private StackPane conversationAvatar5;
    @FXML
    private StackPane conversationAvatar6;
    @FXML
    private StackPane conversationAvatar7;

    @FXML
    private StackPane headerAvatar;
    @FXML
    private StackPane messageAvatar1;
    @FXML
    private StackPane messageAvatar2;
    @FXML
    private StackPane selfAvatar;

    @FXML
    private Node groupConversationRow;

    @FXML
    private Button viewProfileButton;
    @FXML
    private Button infoButton;

    @FXML
    private void initialize() {
        install(conversationAvatar1, "T", "#FF7700", true, 56);
        install(conversationAvatar2, "T", "#FF0000", true, 56);
        install(conversationAvatar3, "D", "#FFBB00", true, 56);
        install(conversationAvatar4, "A", "#007BFF", true, 56);
        install(conversationAvatar5, "H", "#BF00FF", true, 56);
        install(conversationAvatar6, "P", "#C86565", true, 56);
        install(conversationAvatar7, "L", "#0C0D58", true, 56);

        install(headerAvatar, "T", "#FF7700", true, 56);
        install(messageAvatar1, "T", "#FF7700", true, 56);
        install(messageAvatar2, "T", "#FF7700", true, 56);
        install(selfAvatar, "T", "#FF7700", true, 113);
    }

    @FXML
    private void openGroupChat() {
        try {
            Stage stage = (Stage) groupConversationRow
                .getScene()
                .getWindow();
            UserNavigator.showMain(stage, true);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void openProfile() {
        try {
            Node source = viewProfileButton != null
                ? viewProfileButton
                : infoButton;

            Stage stage = (Stage) source
                .getScene()
                .getWindow();
            UserNavigator.show(stage, UserRoute.PROFILE);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void install(
        StackPane host,
        String initials,
        String color,
        boolean online,
        double size
    ) {
        if (host != null) {
            host.getChildren().setAll(
                new AvatarView(initials, color, online, size)
            );
        }
    }
}
