package com.chatapp.admin.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.kordamp.ikonli.javafx.FontIcon;

import com.chatapp.admin.component.SortableTableView;
import com.chatapp.admin.dto.SpamReportRow;
import com.chatapp.admin.dto.SpamReportRow.Status;
import com.chatapp.admin.service.SpamReportService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SpamReportsController {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    private final SpamReportService reportService =
        SpamReportService.getInstance();

    @FXML
    private TextField keywordField;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> sortFilter;

    @FXML
    private Label filterWarningLabel;

    @FXML
    private SortableTableView<SpamReportRow> reportsTable;

    @FXML
    private TableColumn<SpamReportRow, LocalDateTime> timeColumn;

    @FXML
    private TableColumn<SpamReportRow, SpamReportRow> reporterColumn;

    @FXML
    private TableColumn<SpamReportRow, SpamReportRow> accusedColumn;

    @FXML
    private TableColumn<SpamReportRow, String> reasonColumn;

    @FXML
    private TableColumn<SpamReportRow, Status> statusColumn;

    @FXML
    private TableColumn<SpamReportRow, SpamReportRow> detailColumn;

    private Consumer<SpamReportRow> viewReportHandler = report -> {
    };

    @FXML
    private void initialize() {
        configureFilters();
        configureTable();
        refreshReports();
    }

    public void setViewReportHandler(
        Consumer<SpamReportRow> viewReportHandler
    ) {
        this.viewReportHandler = viewReportHandler != null
            ? viewReportHandler
            : report -> {
            };
    }

    @FXML
    private void applyFilters() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from != null && to != null && from.isAfter(to)) {
            filterWarningLabel.setText(
                "Từ ngày không được lớn hơn đến ngày."
            );
            filterWarningLabel.setVisible(true);
            filterWarningLabel.setManaged(true);
            return;
        }
        hideWarning();
        refreshReports();
    }

    @FXML
    private void resetFilters() {
        keywordField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        statusFilter.getSelectionModel().selectFirst();
        sortFilter.setValue("Thời gian mới nhất");
        hideWarning();
        refreshReports();
    }

    private void configureFilters() {
        statusFilter.getItems().setAll(
            "Tất cả trạng thái",
            Status.PENDING.getLabel(),
            Status.REVIEWING.getLabel(),
            Status.RESOLVED.getLabel()
        );
        sortFilter.getItems().setAll(
            "Thời gian mới nhất",
            "Thời gian cũ nhất",
            "Tên đăng nhập"
        );
        statusFilter.getSelectionModel().selectFirst();
        sortFilter.setValue("Thời gian mới nhất");
        hideWarning();
    }

    private void configureTable() {
        timeColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue().getCreatedAt())
        );
        timeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime time, boolean empty) {
                super.updateItem(time, empty);
                setText(empty || time == null
                    ? null
                    : time.format(DATE_TIME_FORMAT));
            }
        });

        reporterColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue())
        );
        reporterColumn.setCellFactory(column -> createUserCell(true));
        accusedColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue())
        );
        accusedColumn.setCellFactory(column -> createUserCell(false));

        reasonColumn.setCellValueFactory(data ->
            new ReadOnlyStringWrapper(data.getValue().getReasonSummary())
        );
        statusColumn.setCellValueFactory(data ->
            new ReadOnlyObjectWrapper<>(data.getValue().getStatus())
        );
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label badge = new Label(status.getLabel());
                badge.getStyleClass().addAll(
                    "spam-status-badge",
                    switch (status) {
                        case PENDING -> "spam-status-pending";
                        case REVIEWING -> "spam-status-reviewing";
                        case RESOLVED -> "spam-status-resolved";
                    }
                );
                setGraphic(badge);
            }
        });
        detailColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue())
        );
        detailColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(SpamReportRow report, boolean empty) {
                super.updateItem(report, empty);
                if (empty || report == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Button button = new Button("Xem chi tiết");
                button.setGraphic(new FontIcon("fas-eye"));
                button.getStyleClass().addAll(
                    "table-action-button",
                    "user-list-action-detail"
                );
                button.setOnAction(
                    event -> viewReportHandler.accept(report)
                );
                setGraphic(button);
            }
        });
    }

    private TableCell<SpamReportRow, SpamReportRow> createUserCell(
        boolean reporter
    ) {
        return new TableCell<>() {
            @Override
            protected void updateItem(SpamReportRow report, boolean empty) {
                super.updateItem(report, empty);
                if (empty || report == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                var user = reporter
                    ? report.getReporter()
                    : report.getAccused();
                Label avatar = new Label(user.getInitials());
                avatar.getStyleClass().add("table-user-avatar");
                Label name = new Label(user.getFullName());
                name.getStyleClass().add("table-user-name");
                Label username = new Label("@" + user.getUsername());
                username.getStyleClass().add("table-user-username");
                HBox content = new HBox(
                    8,
                    avatar,
                    new VBox(2, name, username)
                );
                content.setAlignment(Pos.CENTER_LEFT);
                setGraphic(content);
            }
        };
    }

    private void refreshReports() {
        String keyword = keywordField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        String selectedStatus = statusFilter.getValue();

        List<SpamReportRow> filtered = reportService.getReports().stream()
            .filter(report -> matchesKeyword(report, keyword))
            .filter(report -> matchesDate(report, from, to))
            .filter(report -> selectedStatus == null
                || selectedStatus.startsWith("Tất cả")
                || report.getStatus().getLabel().equals(selectedStatus))
            .sorted(createComparator())
            .toList();

        reportsTable.setItems(FXCollections.observableArrayList(filtered));
        reportsTable.refresh();
    }

    private boolean matchesKeyword(SpamReportRow report, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return report.getReporter().getFullName().toLowerCase(Locale.ROOT)
                .contains(keyword)
            || report.getReporter().getUsername().toLowerCase(Locale.ROOT)
                .contains(keyword)
            || report.getAccused().getFullName().toLowerCase(Locale.ROOT)
                .contains(keyword)
            || report.getAccused().getUsername().toLowerCase(Locale.ROOT)
                .contains(keyword);
    }

    private boolean matchesDate(
        SpamReportRow report,
        LocalDate from,
        LocalDate to
    ) {
        LocalDate date = report.getCreatedAt().toLocalDate();
        return (from == null || !date.isBefore(from))
            && (to == null || !date.isAfter(to));
    }

    private Comparator<SpamReportRow> createComparator() {
        String selectedSort = sortFilter.getValue();
        return switch (selectedSort == null
            ? "Thời gian mới nhất"
            : selectedSort) {
            case "Thời gian cũ nhất" ->
                Comparator.comparing(SpamReportRow::getCreatedAt);
            case "Tên đăng nhập" -> Comparator.comparing(
                report -> report.getAccused().getUsername(),
                String.CASE_INSENSITIVE_ORDER
            );
            default -> Comparator.comparing(
                SpamReportRow::getCreatedAt
            ).reversed();
        };
    }

    private void hideWarning() {
        filterWarningLabel.setText("");
        filterWarningLabel.setVisible(false);
        filterWarningLabel.setManaged(false);
    }
}
