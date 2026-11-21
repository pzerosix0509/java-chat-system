package com.chatapp.admin;

import com.chatapp.admin.navigation.AdminNavigator;

import javafx.application.Application;
import javafx.stage.Stage;

public class AdminApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AdminNavigator.showLogin(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
