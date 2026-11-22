package com.chatapp.admin.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.chatapp.admin.component.SortableTableView;
import com.chatapp.admin.dto.ActivityStatRow;
import com.chatapp.admin.dto.UserRow;
import com.chatapp.admin.service.StatisticsService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ActiveUsersController {

    private static final String METRIC_OPENS = "Số lần mở ứng dụng";
    private static final String METRIC_PEOPLE = "Số người đã chat";
    private static final String METRIC_GROUPS = "Số nhóm đã chat";
    private static final String EQUAL = "Bằng";
    private static final String LESS_THAN = "Nhỏ hơn";
    private static final String GREATER_THAN = "Lớn hơn";
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");

    @FXML private TextField keywordField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> metricFilter;
    @FXML private ComboBox<String> comparisonFilter;
    @FXML private TextField metricValueField;
    @FXML private Label filterWarningLabel;
    @FXML private Label resultCountLabel;
    @FXML private SortableTableView<ActivityStatRow> activeUsersTable;
    @FXML private TableColumn<ActivityStatRow, UserRow> userColumn;
    @FXML private TableColumn<ActivityStatRow, Integer> opensColumn;
    @FXML private TableColumn<ActivityStatRow, Integer> peopleColumn;
    @FXML private TableColumn<ActivityStatRow, Integer> groupsColumn;
    @FXML private TableColumn<ActivityStatRow, LocalDateTime> lastActivityColumn;

    private final StatisticsService statisticsService =
        StatisticsService.getInstance();
    private List<ActivityStatRow> sourceRows;

    @FXML
    private void initialize() {
        sourceRows = statisticsService.getActivityStats();
        metricFilter.setItems(FXCollections.observableArrayList(
            METRIC_OPENS, METRIC_PEOPLE, METRIC_GROUPS
        ));
        comparisonFilter.setItems(FXCollections.observableArrayList(
            EQUAL, LESS_THAN, GREATER_THAN
        ));
        metricFilter.setValue(METRIC_OPENS);
        comparisonFilter.setValue(GREATER_THAN);
        configureColumns();
        applyFilters();
    }

    private void configureColumns() {
        userColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().user())
        );
        userColumn.setCellFactory(column -> createUserCell());
        opensColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().appOpenCount())
        );
        peopleColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().peopleChattedCount())
        );
        groupsColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().groupsChattedCount())
        );
        opensColumn.setCellFactory(column -> metricCell());
        peopleColumn.setCellFactory(column -> metricCell());
        groupsColumn.setCellFactory(column -> metricCell());
        lastActivityColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().lastActivityAt())
        );
        lastActivityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null
                    ? null : DATE_TIME_FORMAT.format(value));
            }
        });
    }

    @FXML
    private void applyFilters() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from != null && to != null && from.isAfter(to)) {
            showWarning("Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }
        Integer target = parseTargetValue();
        if (!metricValueField.getText().isBlank() && target == null) {
            showWarning("Giá trị hoạt động phải là số nguyên không âm.");
            return;
        }
        hideWarning();
        String keyword = normalize(keywordField.getText());

        Integer finalTarget = target;
        List<ActivityStatRow> filtered = sourceRows.stream()
            .filter(row -> keyword.isBlank()
                || normalize(row.user().getFullName()).contains(keyword)
                || normalize(row.user().getUsername()).contains(keyword))
            .filter(row -> matchesDate(row, from, to))
            .filter(row -> finalTarget == null
                || compare(metricValue(row), finalTarget))
            .sorted(Comparator.comparing(
                ActivityStatRow::lastActivityAt
            ).reversed())
            .toList();
        activeUsersTable.setItems(
            FXCollections.observableArrayList(filtered)
        );
        resultCountLabel.setText("Hiển thị " + filtered.size()
            + " người dùng có hoạt động");
    }

    @FXML
    private void resetFilters() {
        keywordField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        metricFilter.setValue(METRIC_OPENS);
        comparisonFilter.setValue(GREATER_THAN);
        metricValueField.clear();
        applyFilters();
    }

    private boolean matchesDate(
        ActivityStatRow row,
        LocalDate from,
        LocalDate to
    ) {
        LocalDate activityDate = row.lastActivityAt().toLocalDate();
        return (from == null || !activityDate.isBefore(from))
            && (to == null || !activityDate.isAfter(to));
    }

    private int metricValue(ActivityStatRow row) {
        return switch (metricFilter.getValue()) {
            case METRIC_PEOPLE -> row.peopleChattedCount();
            case METRIC_GROUPS -> row.groupsChattedCount();
            default -> row.appOpenCount();
        };
    }

    private boolean compare(int actual, int target) {
        return switch (comparisonFilter.getValue()) {
            case EQUAL -> actual == target;
            case LESS_THAN -> actual < target;
            default -> actual > target;
        };
    }

    private Integer parseTargetValue() {
        String text = metricValueField.getText().trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text);
            return value >= 0 ? value : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private TableCell<ActivityStatRow, UserRow> createUserCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(UserRow user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    return;
                }
                Label avatar = new Label(user.getInitials());
                avatar.getStyleClass().add("table-user-avatar");
                Label name = new Label(user.getFullName());
                name.getStyleClass().add("table-user-name");
                Label username = new Label("@" + user.getUsername());
                username.getStyleClass().add("table-user-username");
                HBox content = new HBox(
                    9, avatar, new VBox(2, name, username)
                );
                content.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(content);
            }
        };
    }

    private TableCell<ActivityStatRow, Integer> metricCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.toString());
                setAlignment(Pos.CENTER_LEFT);
                getStyleClass().remove("stat-number-cell");
                if (!empty && value != null) {
                    getStyleClass().add("stat-number-cell");
                }
            }
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private void showWarning(String message) {
        filterWarningLabel.setText(message);
        filterWarningLabel.setManaged(true);
        filterWarningLabel.setVisible(true);
    }

    private void hideWarning() {
        filterWarningLabel.setManaged(false);
        filterWarningLabel.setVisible(false);
    }
}
