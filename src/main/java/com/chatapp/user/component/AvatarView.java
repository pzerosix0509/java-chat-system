package com.chatapp.user.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Circular avatar with an optional online status dot, used by the user UI.
 */
public class AvatarView extends StackPane {

    private final Circle statusDot;

    public AvatarView(
        String initials,
        String color,
        boolean online,
        double size
    ) {
        setPrefSize(size, size);
        setMaxSize(size, size);
        setMinSize(size, size);
        setAlignment(Pos.CENTER);

        Circle background = new Circle(size / 2);
        background.setFill(Color.web(color));

        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle(
            "-fx-text-fill: #FAF9F9; -fx-font-size: "
            + Math.max(9, size * 0.32)
            + "px; -fx-font-weight: bold;"
        );

        double dotSize = size * 0.27;
        statusDot = new Circle(dotSize / 2);
        statusDot.setFill(
            online ? Color.web("#99FFA5") : Color.web("#D9D9D9")
        );
        statusDot.setStroke(Color.WHITE);
        statusDot.setStrokeWidth(1.5);
        StackPane.setAlignment(statusDot, Pos.BOTTOM_LEFT);
        statusDot.setTranslateX(-size * 0.05);
        statusDot.setTranslateY(size * 0.05);
        statusDot.setVisible(online);

        getChildren().addAll(background, initialsLabel, statusDot);
    }

    public void setOnline(boolean online) {
        statusDot.setVisible(online);
    }
}
