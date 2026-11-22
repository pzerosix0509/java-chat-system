package com.chatapp.admin.component;

import java.time.format.DateTimeFormatter;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.StackPane;
import javafx.util.converter.LocalDateStringConverter;

public final class DatePickerStyler {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private DatePickerStyler() {
    }

    public static void installCalendarIcon(DatePicker datePicker) {
        datePicker.setConverter(
            new LocalDateStringConverter(DATE_FORMAT, DATE_FORMAT)
        );
        datePicker.skinProperty().addListener(
            (observable, oldSkin, newSkin) ->
                Platform.runLater(() -> replaceCalendarIcon(datePicker))
        );
        datePicker.sceneProperty().addListener(
            (observable, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(
                        () -> replaceCalendarIcon(datePicker)
                    );
                }
            }
        );
        Platform.runLater(() -> replaceCalendarIcon(datePicker));
    }

    private static void replaceCalendarIcon(DatePicker datePicker) {
        datePicker.applyCss();
        Node arrowButton = datePicker.lookup(".arrow-button");
        if (!(arrowButton instanceof StackPane buttonContent)) {
            return;
        }

        FontIcon icon = new FontIcon("fas-calendar-alt");
        icon.getStyleClass().add("date-picker-calendar-icon");
        icon.setMouseTransparent(true);
        buttonContent.getStyleClass().add("date-picker-arrow-button");
        buttonContent.getChildren().setAll(icon);
    }
}
