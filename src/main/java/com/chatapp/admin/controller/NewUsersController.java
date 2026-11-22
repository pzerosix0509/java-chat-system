package com.chatapp.admin.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import com.chatapp.admin.component.SortableTableView;
import com.chatapp.admin.dto.RegistrationStatRow;
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

public class NewUsersController {

    private static final String SORT_CREATED = "Thời gian tạo";
    private static final String SORT_NAME = "Họ tên";
    private static final String DESCENDING = "Giảm dần";
    private static final String ASCENDING = "Tăng dần";
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");

    @FXML private TextField keywordField;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> sortFieldFilter;
    @FXML private ComboBox<String> sortDirectionFilter;
    @FXML private Label filterWarningLabel;
    @FXML private Label resultCountLabel;
    @FXML private SortableTableView<RegistrationStatRow> newUsersTable;
    @FXML private TableColumn<RegistrationStatRow, UserRow> userColumn;
    @FXML private TableColumn<RegistrationStatRow, String> emailColumn;
    @FXML private TableColumn<RegistrationStatRow, Object> createdAtColumn;
    @FXML private TableColumn<RegistrationStatRow, Object> activityColumn;
    @FXML private TableColumn<RegistrationStatRow, UserRow.Status> statusColumn;

    private final StatisticsService statisticsService =
        StatisticsService.getInstance();
    private List<RegistrationStatRow> sourceRows;

    @FXML
    private void initialize() {
        sourceRows = statisticsService.getRegistrationStats();
        sortFieldFilter.setItems(FXCollections.observableArrayList(
            SORT_CREATED, SORT_NAME
        ));
        sortDirectionFilter.setItems(FXCollections.observableArrayList(
            DESCENDING, ASCENDING
        ));
        sortFieldFilter.setValue(SORT_CREATED);
        sortDirectionFilter.setValue(DESCENDING);
        configureColumns();
        applyFilters();
    }

    private void configureColumns() {
        userColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().user())
        );
        userColumn.setCellFactory(column -> createUserCell());
        emailColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().user().getEmail())
        );
        createdAtColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().registeredAt())
        );
        createdAtColumn.setCellFactory(column -> formattedDateCell());
        activityColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().lastActivityAt())
        );
        activityColumn.setCellFactory(column -> formattedDateCell());
        statusColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().user().getStatus())
        );
        statusColumn.setCellFactory(column -> statusCell());
    }

    @FXML
    private void applyFilters() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from != null && to != null && from.isAfter(to)) {
            showWarning("Ngày bắt đầu không được sau ngày kết thúc.");
            return;
        }
        hideWarning();

        String keyword = normalize(keywordField.getText());
        Predicate<RegistrationStatRow> matches = row -> {
            LocalDate createdDate = row.registeredAt().toLocalDate();
            boolean matchesKeyword = keyword.isBlank()
                || normalize(row.user().getFullName()).contains(keyword)
                || normalize(row.user().getUsername()).contains(keyword)
                || normalize(row.user().getEmail()).contains(keyword);
            boolean matchesFrom = from == null || !createdDate.isBefore(from);
            boolean matchesTo = to == null || !createdDate.isAfter(to);
            return matchesKeyword && matchesFrom && matchesTo;
        };

        Comparator<RegistrationStatRow> comparator =
            SORT_NAME.equals(sortFieldFilter.getValue())
                ? Comparator.comparing(
                    row -> normalize(row.user().getFullName())
                )
                : Comparator.comparing(RegistrationStatRow::registeredAt);
        if (DESCENDING.equals(sortDirectionFilter.getValue())) {
            comparator = comparator.reversed();
        }

        List<RegistrationStatRow> filtered = sourceRows.stream()
            .filter(matches)
            .sorted(comparator)
            .toList();
        newUsersTable.setItems(FXCollections.observableArrayList(filtered));
        resultCountLabel.setText("Hiển thị " + filtered.size()
            + " người dùng đăng ký mới");
    }

    @FXML
    private void resetFilters() {
        keywordField.clear();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        sortFieldFilter.setValue(SORT_CREATED);
        sortDirectionFilter.setValue(DESCENDING);
        applyFilters();
    }

    private TableCell<RegistrationStatRow, UserRow> createUserCell() {
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
                VBox text = new VBox(2, name, username);
                HBox content = new HBox(9, avatar, text);
                content.setAlignment(Pos.CENTER_LEFT);
                setText(null);
                setGraphic(content);
            }
        };
    }

    private TableCell<RegistrationStatRow, Object> formattedDateCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Object value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null
                    : DATE_TIME_FORMAT.format((java.time.LocalDateTime) value));
            }
        };
    }

    private TableCell<RegistrationStatRow, UserRow.Status> statusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(UserRow.Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status.getLabel());
                badge.getStyleClass().addAll(
                    "table-status-badge",
                    switch (status) {
                        case ACTIVE -> "table-status-active";
                        case LOCKED -> "table-status-locked";
                        case DELETED -> "table-status-deleted";
                    }
                );
                setText(null);
                setGraphic(badge);
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
