package com.chatapp.user.controller;

import java.io.IOException;

import com.chatapp.user.component.AvatarView;
import com.chatapp.user.navigation.UserNavigator;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Group-message variant of the main client screen. */
public class GroupChatController {

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
    private StackPane memberAvatar1;
    @FXML
    private StackPane memberAvatar2;
    @FXML
    private StackPane memberAvatar3;
    @FXML
    private StackPane memberAvatar4;

    @FXML
    private Node directConversationRow;

    @FXML
    private void initialize() {
        install(conversationAvatar1, "T", "#FF7700", true, 56);
        install(conversationAvatar2, "B", "#FF0000", true, 56);
        install(conversationAvatar3, "D", "#FFBB00", true, 56);
        install(conversationAvatar4, "A", "#007BFF", true, 56);
        install(conversationAvatar5, "H", "#BF00FF", true, 56);
        install(conversationAvatar6, "P", "#C86565", true, 56);
        install(conversationAvatar7, "L", "#0C0D58", true, 56);

        install(headerAvatar, "B", "#FF0000", true, 56);
        install(messageAvatar1, "T", "#FF7700", true, 56);
        install(messageAvatar2, "T", "#FF7700", true, 56);
        install(selfAvatar, "B", "#FF0000", true, 113);

        install(memberAvatar1, "T", "#FF7700", true, 28);
        install(memberAvatar2, "D", "#FFBB00", true, 28);
        install(memberAvatar3, "A", "#007BFF", true, 28);
        install(memberAvatar4, "H", "#BF00FF", true, 28);
    }

    @FXML
    private void openDirectChat() {
        try {
            Stage stage = (Stage) directConversationRow
                .getScene()
                .getWindow();
            UserNavigator.showMain(stage, false);
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
