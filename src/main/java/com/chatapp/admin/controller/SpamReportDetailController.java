package com.chatapp.admin.controller;

import java.time.format.DateTimeFormatter;

import org.kordamp.ikonli.javafx.FontIcon;

import com.chatapp.admin.dto.SpamReportRow;
import com.chatapp.admin.dto.SpamReportRow.Evidence;
import com.chatapp.admin.dto.SpamReportRow.Status;
import com.chatapp.admin.service.SpamReportService;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SpamReportDetailController {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    private final SpamReportService reportService =
        SpamReportService.getInstance();

    @FXML
    private Label reportCodeLabel;

    @FXML
    private Label reportTimeLabel;

    @FXML
    private Label reportStatusLabel;

    @FXML
    private Label reportReasonLabel;

    @FXML
    private Label reporterNameLabel;

    @FXML
    private Label reporterUsernameLabel;

    @FXML
    private Label reporterAvatarLabel;

    @FXML
    private Label accusedNameLabel;

    @FXML
    private Label accusedUsernameLabel;

    @FXML
    private Label accusedAvatarLabel;

    @FXML
    private Label reportContentLabel;

    @FXML
    private Label contextTimeLabel;

    @FXML
    private Label contextTypeLabel;

    @FXML
    private FlowPane evidenceList;

    @FXML
    private GridPane decisionGrid;

    @FXML
    private VBox reportMain;

    @FXML
    private VBox decisionPanel;

    @FXML
    private ComboBox<String> durationComboBox;

    @FXML
    private TextArea adminReasonArea;

    @FXML
    private TextArea reporterMessageArea;

    @FXML
    private Label validationLabel;

    @FXML
    private Button lockAccountButton;

    @FXML
    private Button noLockButton;

    private SpamReportRow report;
    private Runnable backHandler = () -> {
    };
    private ToastHandler toastHandler = message -> {
    };

    @FunctionalInterface
    public interface ToastHandler {
        void show(String message);
    }

    @FXML
    private void initialize() {
        durationComboBox.getItems().setAll(
            "24 giờ",
            "3 ngày",
            "7 ngày",
            "30 ngày",
            "Vĩnh viễn"
        );
        durationComboBox.setPromptText("Chọn thời hạn khóa");
        hideValidation();
        decisionGrid.widthProperty().addListener(
            (observable, oldWidth, newWidth) -> updateDecisionLayout(
                newWidth.doubleValue()
            )
        );
    }

    public void setReport(SpamReportRow report) {
        this.report = report;
        refreshView();
    }

    public void setBackHandler(Runnable backHandler) {
        this.backHandler = backHandler != null
            ? backHandler
            : () -> {
            };
    }

    public void setToastHandler(ToastHandler toastHandler) {
        this.toastHandler = toastHandler != null
            ? toastHandler
            : message -> {
            };
    }

    @FXML
    private void goBack() {
        backHandler.run();
    }

    @FXML
    private void lockAccount() {
        String message = reporterMessageArea.getText().trim();
        String reason = adminReasonArea.getText().trim();
        String duration = durationComboBox.getValue();

        if (message.isEmpty()) {
            showValidation(
                "Nhập thông báo cho người báo cáo trước khi xử lý.",
                reporterMessageArea
            );
            return;
        }
        if (duration == null || duration.isBlank()) {
            showValidation("Chọn thời hạn khóa tài khoản.", durationComboBox);
            return;
        }
        if (reason.isEmpty()) {
            showValidation("Nhập lý do quản trị khi khóa tài khoản.", adminReasonArea);
            return;
        }

        hideValidation();
        reportService.recordDecision(
            report,
            true,
            duration,
            reason,
            message
        );
        backHandler.run();
        toastHandler.show("Đã ghi nhận quyết định khóa trong bản xem thử.");
    }

    @FXML
    private void doNotLock() {
        String message = reporterMessageArea.getText().trim();
        if (message.isEmpty()) {
            showValidation(
                "Nhập thông báo cho người báo cáo trước khi xử lý.",
                reporterMessageArea
            );
            return;
        }

        hideValidation();
        reportService.recordDecision(
            report,
            false,
            null,
            adminReasonArea.getText().trim(),
            message
        );
        backHandler.run();
        toastHandler.show("Đã ghi nhận quyết định không khóa trong bản xem thử.");
    }

    private void updateDecisionLayout(double width) {
        boolean narrow = width < 820;
        GridPane.setColumnIndex(decisionPanel, narrow ? 0 : 1);
        GridPane.setRowIndex(decisionPanel, narrow ? 1 : 0);
        GridPane.setColumnSpan(decisionPanel, narrow ? 2 : 1);
        decisionPanel.setPrefWidth(narrow ? Double.MAX_VALUE : 350);
        decisionPanel.setMaxWidth(Double.MAX_VALUE);
        reportMain.setMaxWidth(Double.MAX_VALUE);
        if (narrow) {
            GridPane.setColumnIndex(reportMain, 0);
            GridPane.setColumnSpan(reportMain, 2);
        } else {
            GridPane.setColumnSpan(reportMain, 1);
        }
    }

    private void refreshView() {
        if (report == null) {
            return;
        }
        reportCodeLabel.setText("BÁO CÁO #" + report.getReportCode());
        reportTimeLabel.setText(
            report.getCreatedAt().format(DATE_TIME_FORMAT)
        );
        reportReasonLabel.setText(report.getReasonSummary());
        reportStatusLabel.setText(report.getStatus().getLabel());
        reportStatusLabel.getStyleClass().removeAll(
            "spam-status-pending",
            "spam-status-reviewing",
            "spam-status-resolved"
        );
        reportStatusLabel.getStyleClass().add(statusStyle(report.getStatus()));

        reporterNameLabel.setText(report.getReporter().getFullName());
        reporterUsernameLabel.setText(
            "@" + report.getReporter().getUsername()
        );
        reporterAvatarLabel.setText(report.getReporter().getInitials());
        accusedNameLabel.setText(report.getAccused().getFullName());
        accusedUsernameLabel.setText("@" + report.getAccused().getUsername());
        accusedAvatarLabel.setText(report.getAccused().getInitials());

        reportContentLabel.setText("“" + report.getReportContent() + "”");
        contextTimeLabel.setText(
            "Hội thoại lúc "
                + report.getContext().occurredAt().format(DATE_TIME_FORMAT)
        );
        contextTypeLabel.setText(report.getContext().type());
        renderEvidence();

        boolean resolved = report.getStatus() == Status.RESOLVED;
        lockAccountButton.setDisable(resolved);
        noLockButton.setDisable(resolved);
    }

    private void renderEvidence() {
        evidenceList.getChildren().clear();
        for (Evidence evidence : report.getEvidence()) {
            evidenceList.getChildren().add(
                evidence.attachment()
                    ? createFileEvidence(evidence)
                    : createImageEvidence(evidence)
            );
        }
    }

    private Node createImageEvidence(Evidence evidence) {
        VBox card = new VBox(8);
        card.getStyleClass().add("spam-evidence-card");

        VBox preview = new VBox(7);
        preview.getStyleClass().addAll(
            "spam-evidence-preview",
            "spam-evidence-chat-preview"
        );
        Label sender = new Label("Bạn đã xem ưu đãi này chưa?");
        sender.setWrapText(true);
        sender.getStyleClass().add("spam-evidence-chat-bubble");
        Label repeated = new Label(
            "Nhấn vào liên kết này để nhận quà ngay."
        );
        repeated.setWrapText(true);
        repeated.getStyleClass().addAll(
            "spam-evidence-chat-bubble",
            "spam-evidence-chat-bubble-reported"
        );
        Label timestamp = new Label("08:52 · Nội dung được báo cáo");
        timestamp.getStyleClass().add("spam-evidence-chat-time");
        preview.getChildren().addAll(sender, repeated, timestamp);

        Label title = new Label(evidence.title());
        title.getStyleClass().add("spam-evidence-title");
        Label description = new Label(evidence.description());
        description.getStyleClass().add("spam-evidence-description");
        card.getChildren().addAll(preview, title, description);
        return card;
    }

    private Node createFileEvidence(Evidence evidence) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll(
            "spam-evidence-card",
            "spam-evidence-file"
        );
        FontIcon icon = new FontIcon("fas-file-alt");
        icon.setIconSize(25);
        Label title = new Label(evidence.title());
        title.getStyleClass().add("spam-evidence-title");
        Label description = new Label(evidence.description());
        description.getStyleClass().add("spam-evidence-description");
        card.getChildren().addAll(icon, title, description);
        return card;
    }

    private String statusStyle(Status status) {
        return switch (status) {
            case PENDING -> "spam-status-pending";
            case REVIEWING -> "spam-status-reviewing";
            case RESOLVED -> "spam-status-resolved";
        };
    }

    private void showValidation(String message, javafx.scene.Node target) {
        validationLabel.setText(message);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
        target.requestFocus();
    }

    private void hideValidation() {
        validationLabel.setText("");
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);
    }
}
