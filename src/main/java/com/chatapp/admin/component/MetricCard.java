package com.chatapp.admin.component;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class MetricCard extends VBox {

    private final Label titleLabel = new Label();
    private final Label valueLabel = new Label();
    private final Label trendLabel = new Label();
    private final FontIcon metricIcon = new FontIcon();

    public MetricCard() {
        getStyleClass().add("dashboard-metric-card");
        setMaxWidth(Double.MAX_VALUE);
        setFillWidth(true);

        titleLabel.getStyleClass().add("dashboard-metric-title");
        valueLabel.getStyleClass().add("dashboard-metric-value");
        trendLabel.getStyleClass().add("dashboard-metric-trend");

        metricIcon.setIconSize(16);

        HBox iconBox = new HBox(metricIcon);
        iconBox.setAlignment(Pos.CENTER);
        iconBox.getStyleClass().add("dashboard-metric-icon");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox header = new HBox(titleLabel, spacer, iconBox);
        header.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(header, valueLabel, trendLabel);
    }

    public void setMetric(
        String title,
        String value,
        String trend,
        String iconLiteral,
        boolean alert
    ) {
        titleLabel.setText(title);
        valueLabel.setText(value);
        trendLabel.setText(trend);
        metricIcon.setIconLiteral(iconLiteral);

        getStyleClass().remove("dashboard-metric-alert");
        if (alert) {
            getStyleClass().add("dashboard-metric-alert");
        }
    }
}
