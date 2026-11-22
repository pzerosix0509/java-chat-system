package com.chatapp.admin;

import com.chatapp.admin.navigation.AdminNavigator;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class AdminApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        loadBundledFonts();
        AdminNavigator.showLogin(stage);
        stage.show();
    }

    private void loadBundledFonts() {
        Font.loadFont(
            AdminApp.class.getResourceAsStream(
                "/fonts/Inter-Variable.ttf"
            ),
            13
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
