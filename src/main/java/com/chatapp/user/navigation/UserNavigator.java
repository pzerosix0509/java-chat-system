package com.chatapp.user.navigation;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import com.chatapp.user.controller.MainLayoutController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class UserNavigator {

    public static final UserRoute LOGIN_ROUTE = UserRoute.LOGIN;

    private static final double SCENE_WIDTH = 1440;
    private static final double SCENE_HEIGHT = 1024;

    private UserNavigator() {
    }

    public static void show(Stage stage, UserRoute route)
        throws IOException {

        Objects.requireNonNull(route, "UserRoute không được null");

        FXMLLoader loader = createLoader(route.getFxmlPath());
        Parent root = loader.load();

        setScene(stage, root, route.getTitle());
    }

    public static void showMain(Stage stage, boolean groupMode)
        throws IOException {

        FXMLLoader loader = createLoader(
            UserRoute.MAIN_LAYOUT.getFxmlPath()
        );

        Parent root = loader.load();

        MainLayoutController controller =
            loader.getController();
        controller.setGroupMode(groupMode);

        setScene(stage, root, UserRoute.MAIN_LAYOUT.getTitle());
    }

    public static FXMLLoader createLoader(String fxmlPath) {
        URL resource = Objects.requireNonNull(
            UserNavigator.class.getResource(fxmlPath),
            "Không tìm thấy FXML: " + fxmlPath
        );
        return new FXMLLoader(resource);
    }

    private static void setScene(
        Stage stage,
        Parent root,
        String title
    ) {
        Scene scene = new Scene(
            root,
            SCENE_WIDTH,
            SCENE_HEIGHT
        );

        scene.getStylesheets().add(
            requireResource("/css/common.css").toExternalForm()
        );
        scene.getStylesheets().add(
            requireResource("/css/user.css").toExternalForm()
        );
        scene.getStylesheets().add(
            requireResource("/css/chat.css").toExternalForm()
        );

        stage.setTitle(title);
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.setScene(scene);
    }

    private static URL requireResource(String path) {
        return Objects.requireNonNull(
            UserNavigator.class.getResource(path),
            "Không tìm thấy resource: " + path
        );
    }
}
