package com.chatapp.admin.navigation;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import com.chatapp.admin.controller.AdminShellController;
import com.chatapp.admin.dto.AdminProfile;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class AdminNavigator {

    private static final double SCENE_WIDTH = 1280;
    private static final double SCENE_HEIGHT = 760;

    private AdminNavigator() {
    }

    public static void showLogin(Stage stage)
        throws IOException {

        FXMLLoader loader = createLoader(
            "/fxml/admin/admin_login.fxml"
        );

        Parent root = loader.load();

        setScene(
            stage,
            root,
            "MyBrand Admin - Đăng nhập"
        );
    }

    public static void showShell(
        Stage stage,
        AdminProfile profile
    ) throws IOException {

        FXMLLoader loader = createLoader(
            "/fxml/admin/admin_shell.fxml"
        );

        Parent root = loader.load();

        AdminShellController controller =
            loader.getController();

        controller.setAdminProfile(profile);

        setScene(stage, root, "MyBrand Admin");
    }

    public static FXMLLoader showRoute(
        StackPane contentHost,
        AdminRoute route
    ) throws IOException {

        Objects.requireNonNull(
            contentHost,
            "contentHost không được null"
        );
        Objects.requireNonNull(
            route,
            "AdminRoute không được null"
        );

        FXMLLoader loader = createLoader(
            route.getFxmlPath()
        );

        Parent page = loader.load();
        contentHost.getChildren().setAll(page);

        return loader;
    }

    private static FXMLLoader createLoader(
        String fxmlPath
    ) {
        URL resource = Objects.requireNonNull(
            AdminNavigator.class.getResource(fxmlPath),
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
            requireResource("/css/common.css")
                .toExternalForm()
        );

        scene.getStylesheets().add(
            requireResource("/css/admin.css")
                .toExternalForm()
        );

        stage.setTitle(title);
        stage.setMinWidth(1024);
        stage.setMinHeight(680);
        stage.setScene(scene);
    }

    private static URL requireResource(String path) {
        return Objects.requireNonNull(
            AdminNavigator.class.getResource(path),
            "Không tìm thấy resource: " + path
        );
    }
}
