package com.chatapp.user;

import com.chatapp.user.navigation.UserNavigator;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class UserApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        loadBundledFonts();
        UserNavigator.show(stage, UserNavigator.LOGIN_ROUTE);
        stage.show();
    }

    private void loadBundledFonts() {
        Font.loadFont(
            UserApp.class.getResourceAsStream(
                "/fonts/Inter-Variable.ttf"
            ),
            13
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
