package com.chatapp.admin.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public final class ChartPane extends VBox {

    public record Stat(String label, String value) {
    }

    private final Label titleLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label typeLabel = new Label();
    private final StackPane chartHost = new StackPane();
    private final HBox statsBox = new HBox(8);
    private final Label insightValueLabel = new Label();
    private final Text insightText = new Text();

    public ChartPane() {
        getStyleClass().addAll(
            "dashboard-card",
            "dashboard-chart-card"
        );
        setMinWidth(0);
        setMaxWidth(Double.MAX_VALUE);

        titleLabel.getStyleClass().add("dashboard-card-title");
        descriptionLabel.getStyleClass().add(
            "dashboard-card-description"
        );
        typeLabel.getStyleClass().add("dashboard-chart-type");

        VBox heading = new VBox(3, titleLabel, descriptionLabel);
        HBox.setHgrow(heading, Priority.ALWAYS);

        HBox header = new HBox(12, heading, typeLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("dashboard-card-header");

        chartHost.getStyleClass().add("dashboard-chart-host");
        chartHost.setMinHeight(250);
        chartHost.setPrefHeight(250);

        statsBox.getStyleClass().add("dashboard-chart-stats");
        statsBox.setFillHeight(true);
        VBox.setMargin(
            statsBox,
            new Insets(0, 16, 0, 16)
        );

        insightValueLabel.getStyleClass().add(
            "dashboard-insight-value"
        );

        Text insightPrefix = new Text("Phân tích xu hướng. ");
        insightPrefix.getStyleClass().add(
            "dashboard-insight-prefix"
        );
        insightText.getStyleClass().add("dashboard-insight-text");

        TextFlow insightCopy = new TextFlow(
            insightPrefix,
            insightText
        );
        HBox.setHgrow(insightCopy, Priority.ALWAYS);

        HBox insightBox = new HBox(
            11,
            insightValueLabel,
            insightCopy
        );
        insightBox.setAlignment(Pos.CENTER_LEFT);
        insightBox.getStyleClass().add("dashboard-chart-insight");
        VBox.setMargin(
            insightBox,
            new Insets(12, 16, 16, 16)
        );

        getChildren().addAll(
            header,
            chartHost,
            statsBox,
            insightBox
        );
    }

    public void setHeader(
        String title,
        String description,
        String type
    ) {
        titleLabel.setText(title);
        descriptionLabel.setText(description);
        typeLabel.setText(type);
    }

    public void setChart(Node chart) {
        if (chart instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        chartHost.getChildren().setAll(chart);
    }

    public void setStats(Stat... stats) {
        statsBox.getChildren().clear();

        for (Stat stat : stats) {
            Label label = new Label(stat.label());
            label.getStyleClass().add("dashboard-stat-label");

            Label value = new Label(stat.value());
            value.getStyleClass().add("dashboard-stat-value");

            VBox statBox = new VBox(4, label, value);
            statBox.setMaxWidth(Double.MAX_VALUE);
            statBox.getStyleClass().add("dashboard-stat-box");
            HBox.setHgrow(statBox, Priority.ALWAYS);

            statsBox.getChildren().add(statBox);
        }
    }

    public void setInsight(
        String growth,
        String analysis,
        boolean dangerAccent
    ) {
        insightValueLabel.setText(growth);
        insightText.setText(analysis);

        insightValueLabel.getStyleClass().remove(
            "dashboard-insight-danger"
        );
        if (dangerAccent) {
            insightValueLabel.getStyleClass().add(
                "dashboard-insight-danger"
            );
        }
    }
}
