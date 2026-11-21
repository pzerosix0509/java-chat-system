package com.chatapp.admin.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.chatapp.admin.component.DatePickerStyler;
import com.chatapp.admin.component.SortableTableView;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

public class LoginHistoryController {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");

    private static final List<LoginHistoryRow> MOCK_ROWS = List.of(
        new LoginHistoryRow(
            LocalDateTime.of(2026, 11, 17, 9, 42, 18),
            "truongduyphu", "Trương Duy Phú",
            "Chrome · Windows 11", "103.28.36.12", "Thành công"
        ),
        new LoginHistoryRow(
            LocalDateTime.of(2026, 11, 17, 9, 31, 6),
            "tuananh", "Tuấn Anh",
            "Safari · iPhone", "14.161.8.94", "Thành công"
        ),
        new LoginHistoryRow(
            LocalDateTime.of(2026, 11, 17, 9, 12, 40),
            "tuanvan", "Tuấn Văn",
            "Edge · Windows 11", "113.161.72.5", "Thành công"
        ),
        new LoginHistoryRow(
            LocalDateTime.of(2026, 11, 17, 8, 58, 21),
            "huuhiep", "Hữu Hiệp",
            "Chrome · Android", "27.71.103.8", "Thành công"
        ),
        new LoginHistoryRow(
            LocalDateTime.of(2026, 11, 16, 23, 44, 5),
            "dophuc", "Đỗ Phúc",
            "Firefox · Ubuntu", "171.251.22.4", "Thành công"
        ),
        new LoginHistoryRow(
            LocalDateTime.of(2026, 11, 16, 21, 10, 32),
            "duyhieu", "Duy Hiếu",
            "Chrome · Windows 10", "115.79.41.2", "Thất bại"
        ),
        new LoginHistoryRow(
            LocalDateTime.of(2026, 11, 15, 18, 37, 4),
            "lewlew", "Lew.Lew",
            "Chrome · Windows 11", "118.69.52.31", "Thất bại"
        ),
        new LoginHistoryRow(
            LocalDateTime.of(2026, 11, 14, 12, 20, 55),
            "truongduyphu", "Trương Duy Phú",
            "Safari · macOS", "103.28.36.12", "Thành công"
        )
    );

    @FXML
    private TextField keywordField;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Label filterWarningLabel;

    @FXML
    private SortableTableView<LoginHistoryRow> loginHistoryTable;

    @FXML
    private TableColumn<LoginHistoryRow, LocalDateTime> timeColumn;

    @FXML
    private TableColumn<LoginHistoryRow, String> usernameColumn;

    @FXML
    private TableColumn<LoginHistoryRow, String> fullNameColumn;

    @FXML
    private TableColumn<LoginHistoryRow, String> deviceColumn;

    @FXML
    private TableColumn<LoginHistoryRow, String> ipColumn;

    @FXML
    private TableColumn<LoginHistoryRow, String> resultColumn;

    @FXML
    private Label paginationLabel;

    @FXML
    private void initialize() {
        configureTable();
        DatePickerStyler.installCalendarIcon(fromDatePicker);
        DatePickerStyler.installCalendarIcon(toDatePicker);
        fromDatePicker.setValue(LocalDate.of(2026, 11, 1));
        toDatePicker.setValue(LocalDate.of(2026, 11, 17));
        hideWarning();
        applyFilters();
    }

    @FXML
    private void applyFilters() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if (fromDate != null
            && toDate != null
            && fromDate.isAfter(toDate)) {
            filterWarningLabel.setText(
                "Từ ngày không được lớn hơn đến ngày."
            );
            filterWarningLabel.setVisible(true);
            filterWarningLabel.setManaged(true);
            return;
        }

        hideWarning();
        String keyword = keywordField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);

        List<LoginHistoryRow> filtered = MOCK_ROWS.stream()
            .filter(row -> matchesKeyword(row, keyword))
            .filter(row -> matchesDate(row, fromDate, toDate))
            .sorted(
                Comparator.comparing(LoginHistoryRow::time)
                    .reversed()
            )
            .toList();

        loginHistoryTable.setItems(
            FXCollections.observableArrayList(filtered)
        );
        paginationLabel.setText(filtered.isEmpty()
            ? "Không có lượt đăng nhập phù hợp"
            : "Hiển thị 1–" + filtered.size()
                + " trên " + filtered.size() + " lượt đăng nhập");
    }

    @FXML
    private void resetFilters() {
        keywordField.clear();
        fromDatePicker.setValue(LocalDate.of(2026, 11, 1));
        toDatePicker.setValue(LocalDate.of(2026, 11, 17));
        applyFilters();
    }

    private void configureTable() {
        timeColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue().time())
        );
        timeColumn.setCellFactory(column ->
            new TableCell<>() {
                @Override
                protected void updateItem(
                    LocalDateTime time,
                    boolean empty
                ) {
                    super.updateItem(time, empty);
                    setText(empty || time == null
                        ? null
                        : time.format(DATE_TIME_FORMAT));
                }
            }
        );
        usernameColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                "@" + data.getValue().username()
            )
        );
        fullNameColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().fullName()
            )
        );
        deviceColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().device()
            )
        );
        ipColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().ipAddress()
            )
        );
        resultColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().result()
            )
        );
        resultColumn.setCellFactory(column ->
            new TableCell<>() {
                @Override
                protected void updateItem(
                    String result,
                    boolean empty
                ) {
                    super.updateItem(result, empty);
                    if (empty || result == null) {
                        setGraphic(null);
                        return;
                    }
                    Label badge = new Label(result);
                    badge.getStyleClass().addAll(
                        "table-status-badge",
                        "Thành công".equals(result)
                            ? "table-status-active"
                            : "table-status-locked"
                    );
                    setGraphic(badge);
                }
            }
        );
    }

    private boolean matchesKeyword(
        LoginHistoryRow row,
        String keyword
    ) {
        return keyword.isEmpty()
            || row.username().toLowerCase(Locale.ROOT)
                .contains(keyword)
            || row.fullName().toLowerCase(Locale.ROOT)
                .contains(keyword);
    }

    private boolean matchesDate(
        LoginHistoryRow row,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        LocalDate date = row.time().toLocalDate();
        boolean afterFrom = fromDate == null
            || !date.isBefore(fromDate);
        boolean beforeTo = toDate == null
            || !date.isAfter(toDate);
        return afterFrom && beforeTo;
    }

    private void hideWarning() {
        filterWarningLabel.setText("");
        filterWarningLabel.setVisible(false);
        filterWarningLabel.setManaged(false);
    }

    private record LoginHistoryRow(
        LocalDateTime time,
        String username,
        String fullName,
        String device,
        String ipAddress,
        String result
    ) {
    }
}
