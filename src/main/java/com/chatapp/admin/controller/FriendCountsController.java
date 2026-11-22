package com.chatapp.admin.controller;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.chatapp.admin.component.SortableTableView;
import com.chatapp.admin.dto.FriendStatRow;
import com.chatapp.admin.dto.UserRow;
import com.chatapp.admin.service.StatisticsService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FriendCountsController {

    private static final String EQUAL = "Bằng";
    private static final String LESS_THAN = "Nhỏ hơn";
    private static final String GREATER_THAN = "Lớn hơn";
    private static final String SORT_NAME = "Họ tên";
    private static final String SORT_CREATED = "Thời gian tạo";
    private static final String SORT_DIRECT = "Bạn trực tiếp";
    private static final String ASCENDING = "Tăng dần";
    private static final String DESCENDING = "Giảm dần";
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TextField keywordField;
    @FXML private ComboBox<String> comparisonFilter;
    @FXML private TextField directFriendValueField;
    @FXML private ComboBox<String> sortFieldFilter;
    @FXML private ComboBox<String> sortDirectionFilter;
    @FXML private Label filterWarningLabel;
    @FXML private Label resultCountLabel;
    @FXML private SortableTableView<FriendStatRow> friendCountsTable;
    @FXML private TableColumn<FriendStatRow, UserRow> userColumn;
    @FXML private TableColumn<FriendStatRow, Object> createdAtColumn;
    @FXML private TableColumn<FriendStatRow, Integer> directColumn;
    @FXML private TableColumn<FriendStatRow, Integer> secondDegreeColumn;
    @FXML private TableColumn<FriendStatRow, Integer> totalColumn;

    private final StatisticsService statisticsService =
        StatisticsService.getInstance();
    private List<FriendStatRow> sourceRows;

    @FXML
    private void initialize() {
        sourceRows = statisticsService.getFriendStats();
        comparisonFilter.setItems(FXCollections.observableArrayList(
            EQUAL, LESS_THAN, GREATER_THAN
        ));
        sortFieldFilter.setItems(FXCollections.observableArrayList(
            SORT_NAME, SORT_CREATED, SORT_DIRECT
        ));
        sortDirectionFilter.setItems(FXCollections.observableArrayList(
            ASCENDING, DESCENDING
        ));
        comparisonFilter.setValue(GREATER_THAN);
        sortFieldFilter.setValue(SORT_NAME);
        sortDirectionFilter.setValue(ASCENDING);
        configureColumns();
        applyFilters();
    }

    private void configureColumns() {
        userColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().user())
        );
        userColumn.setCellFactory(column -> createUserCell());
        createdAtColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().user().getCreatedAt())
        );
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Object value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null
                    : DATE_FORMAT.format((java.time.LocalDateTime) value));
            }
        });
        directColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().directFriends())
        );
        secondDegreeColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().secondDegreeFriends())
        );
        totalColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().totalConnections())
        );
        directColumn.setCellFactory(column -> metricCell());
        secondDegreeColumn.setCellFactory(column -> metricCell());
        totalColumn.setCellFactory(column -> metricCell());
    }

    @FXML
    private void applyFilters() {
        Integer target = parseTargetValue();
        if (!directFriendValueField.getText().isBlank() && target == null) {
            showWarning("Số bạn trực tiếp phải là số nguyên không âm.");
            return;
        }
        hideWarning();
        String keyword = normalize(keywordField.getText());

        Comparator<FriendStatRow> comparator = switch (
            sortFieldFilter.getValue()
        ) {
            case SORT_CREATED -> Comparator.comparing(
                row -> row.user().getCreatedAt()
            );
            case SORT_DIRECT -> Comparator.comparingInt(
                FriendStatRow::directFriends
            );
            default -> Comparator.comparing(
                row -> normalize(row.user().getFullName())
            );
        };
        if (DESCENDING.equals(sortDirectionFilter.getValue())) {
            comparator = comparator.reversed();
        }

        Integer finalTarget = target;
        List<FriendStatRow> filtered = sourceRows.stream()
            .filter(row -> keyword.isBlank()
                || normalize(row.user().getFullName()).contains(keyword)
                || normalize(row.user().getUsername()).contains(keyword))
            .filter(row -> finalTarget == null
                || compare(row.directFriends(), finalTarget))
            .sorted(comparator)
            .toList();
        friendCountsTable.setItems(
            FXCollections.observableArrayList(filtered)
        );
        resultCountLabel.setText("Hiển thị " + filtered.size()
            + " người dùng");
    }

    @FXML
    private void resetFilters() {
        keywordField.clear();
        directFriendValueField.clear();
        comparisonFilter.setValue(GREATER_THAN);
        sortFieldFilter.setValue(SORT_NAME);
        sortDirectionFilter.setValue(ASCENDING);
        applyFilters();
    }

    private boolean compare(int actual, int target) {
        return switch (comparisonFilter.getValue()) {
            case EQUAL -> actual == target;
            case LESS_THAN -> actual < target;
            default -> actual > target;
        };
    }

    private Integer parseTargetValue() {
        String text = directFriendValueField.getText().trim();
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

    private TableCell<FriendStatRow, UserRow> createUserCell() {
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

    private TableCell<FriendStatRow, Integer> metricCell() {
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
