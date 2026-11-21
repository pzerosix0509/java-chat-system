package com.chatapp.admin.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import org.kordamp.ikonli.javafx.FontIcon;

import com.chatapp.admin.component.SortableTableView;
import com.chatapp.admin.dto.UserRow;
import com.chatapp.admin.dto.UserRow.Status;
import com.chatapp.admin.navigation.AdminNavigator;
import com.chatapp.admin.service.UserManagementService;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class UserManagementController {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private final UserManagementService userService =
        UserManagementService.getInstance();

    @FXML
    private TextField keywordField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> sortFieldFilter;

    @FXML
    private ComboBox<String> sortDirectionFilter;

    @FXML
    private SortableTableView<UserRow> usersTable;

    @FXML
    private TableColumn<UserRow, UserRow> userColumn;

    @FXML
    private TableColumn<UserRow, String> emailColumn;

    @FXML
    private TableColumn<UserRow, String> genderColumn;

    @FXML
    private TableColumn<UserRow, LocalDateTime> createdAtColumn;

    @FXML
    private TableColumn<UserRow, Status> statusColumn;

    @FXML
    private TableColumn<UserRow, UserRow> actionColumn;

    @FXML
    private Label paginationLabel;

    private Consumer<UserRow> viewUserHandler = user -> {
    };

    @FXML
    private void initialize() {
        configureFilters();
        configureTable();
        refreshUsers();

        userService.getUsers().addListener(
            (ListChangeListener<UserRow>) change -> refreshUsers()
        );
    }

    public void setViewUserHandler(
        Consumer<UserRow> viewUserHandler
    ) {
        this.viewUserHandler = viewUserHandler != null
            ? viewUserHandler
            : user -> {
            };
    }

    @FXML
    private void applyFilters() {
        refreshUsers();
    }

    @FXML
    private void resetFilters() {
        keywordField.clear();
        statusFilter.getSelectionModel().selectFirst();
        sortFieldFilter.setValue("Ngày tạo");
        sortDirectionFilter.setValue("Giảm dần");
        refreshUsers();
    }

    @FXML
    private void addUser() {
        openUserForm(null).ifPresent(userService::createUser);
    }

    private void configureFilters() {
        statusFilter.getItems().setAll(
            "Tất cả trạng thái",
            Status.ACTIVE.getLabel(),
            Status.LOCKED.getLabel()
        );
        sortFieldFilter.getItems().setAll(
            "Họ tên",
            "Ngày tạo"
        );
        sortDirectionFilter.getItems().setAll(
            "Tăng dần",
            "Giảm dần"
        );

        statusFilter.getSelectionModel().selectFirst();
        sortFieldFilter.setValue("Ngày tạo");
        sortDirectionFilter.setValue("Giảm dần");
    }

    private void configureTable() {
        userColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue())
        );
        userColumn.setCellFactory(column -> createUserCell());

        emailColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().getEmail()
            )
        );
        genderColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().getGender()
            )
        );
        createdAtColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(
                data.getValue().getCreatedAt()
            )
        );
        createdAtColumn.setCellFactory(column ->
            new TableCell<>() {
                @Override
                protected void updateItem(
                    LocalDateTime value,
                    boolean empty
                ) {
                    super.updateItem(value, empty);
                    setText(empty || value == null
                        ? null
                        : value.format(DATE_FORMAT));
                }
            }
        );

        statusColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(
                data.getValue().getStatus()
            )
        );
        statusColumn.setCellFactory(column -> createStatusCell());

        actionColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue())
        );
        actionColumn.setCellFactory(column -> createActionCell());
    }

    private TableCell<UserRow, UserRow> createUserCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(
                UserRow user,
                boolean empty
            ) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    return;
                }

                Label avatar = new Label(user.getInitials());
                avatar.getStyleClass().add("table-user-avatar");

                Label name = new Label(user.getFullName());
                name.getStyleClass().add("table-user-name");

                Label username = new Label(
                    "@" + user.getUsername()
                );
                username.getStyleClass().add(
                    "table-user-username"
                );

                HBox content = new HBox(
                    9,
                    avatar,
                    new VBox(3, name, username)
                );
                content.setAlignment(Pos.CENTER_LEFT);
                setGraphic(content);
            }
        };
    }

    private TableCell<UserRow, Status> createStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(
                Status status,
                boolean empty
            ) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }

                Label badge = new Label(status.getLabel());
                badge.getStyleClass().addAll(
                    "table-status-badge",
                    status == Status.LOCKED
                        ? "table-status-locked"
                        : "table-status-active"
                );
                setGraphic(badge);
            }
        };
    }

    private TableCell<UserRow, UserRow> createActionCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(
                UserRow user,
                boolean empty
            ) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setGraphic(null);
                    return;
                }

                Button detailButton = actionButton(
                    "Chi tiết",
                    "fas-eye",
                    "user-list-action-detail"
                );
                detailButton.setOnAction(
                    event -> viewUserHandler.accept(user)
                );

                boolean locked = user.getStatus() == Status.LOCKED;
                Button lockButton = actionButton(
                    locked ? "Mở khóa" : "Khóa",
                    locked ? "fas-lock-open" : "fas-lock",
                    locked
                        ? "user-list-action-unlock"
                        : "user-list-action-lock"
                );
                lockButton.setOnAction(event -> {
                    if (locked) {
                        confirmUnlock(user);
                    } else {
                        openLockDialog(user);
                    }
                });

                HBox actions = new HBox(
                    6,
                    detailButton,
                    lockButton
                );
                actions.setAlignment(Pos.CENTER_LEFT);
                setGraphic(actions);
            }
        };
    }

    private Button actionButton(
        String text,
        String iconLiteral,
        String styleClass
    ) {
        Button button = new Button(text);
        button.setGraphic(new FontIcon(iconLiteral));
        button.getStyleClass().addAll(
            "table-action-button",
            styleClass
        );
        return button;
    }

    private void refreshUsers() {
        String keyword = keywordField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilter.getValue();

        List<UserRow> filtered = userService.getUsers().stream()
            .filter(user -> user.getStatus() != Status.DELETED)
            .filter(user -> matchesKeyword(user, keyword))
            .filter(user -> matchesStatus(user, selectedStatus))
            .sorted(createComparator())
            .toList();

        usersTable.setItems(
            FXCollections.observableArrayList(filtered)
        );
        paginationLabel.setText(filtered.isEmpty()
            ? "Không có người dùng phù hợp"
            : "Hiển thị 1–" + filtered.size()
                + " trên " + filtered.size() + " người dùng");
        usersTable.refresh();
    }

    private boolean matchesKeyword(
        UserRow user,
        String keyword
    ) {
        return keyword.isEmpty()
            || user.getFullName().toLowerCase(Locale.ROOT)
                .contains(keyword)
            || user.getUsername().toLowerCase(Locale.ROOT)
                .contains(keyword)
            || user.getEmail().toLowerCase(Locale.ROOT)
                .contains(keyword);
    }

    private boolean matchesStatus(
        UserRow user,
        String selectedStatus
    ) {
        return selectedStatus == null
            || selectedStatus.startsWith("Tất cả")
            || user.getStatus().getLabel().equals(selectedStatus);
    }

    private Comparator<UserRow> createComparator() {
        Comparator<UserRow> comparator = "Họ tên".equals(
            sortFieldFilter.getValue()
        )
            ? Comparator.comparing(
                UserRow::getFullName,
                String.CASE_INSENSITIVE_ORDER
            )
            : Comparator.comparing(UserRow::getCreatedAt);

        if ("Giảm dần".equals(sortDirectionFilter.getValue())) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    private Optional<UserRow> openUserForm(UserRow editingUser) {
        try {
            FXMLLoader loader = AdminNavigator.createUserFormLoader();
            Parent form = loader.load();
            UserFormController controller = loader.getController();
            controller.configure(editingUser);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(
                editingUser == null
                    ? "Thêm người dùng"
                    : "Cập nhật người dùng"
            );
            dialog.initOwner(usersTable.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.getDialogPane().setContent(form);
            copyStylesheets(dialog);

            ButtonType saveType = new ButtonType(
                editingUser == null
                    ? "Tạo người dùng"
                    : "Lưu thay đổi",
                ButtonBar.ButtonData.OK_DONE
            );
            dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.CANCEL,
                saveType
            );
            dialog.getDialogPane().getStyleClass().add("user-form-dialog");
            dialog.getDialogPane().setPrefWidth(620);

            Node cancelButton = dialog.getDialogPane()
                .lookupButton(ButtonType.CANCEL);
            cancelButton.getStyleClass().add("dialog-button-secondary");

            Node saveButton = dialog.getDialogPane()
                .lookupButton(saveType);
            saveButton.getStyleClass().add("button-primary");
            saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> controller.validateForm()
                    .ifPresent(message -> event.consume())
            );

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.filter(saveType::equals).isPresent()) {
                return Optional.of(controller.buildFormValue());
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            showError("Không thể mở biểu mẫu người dùng.");
        }
        return Optional.empty();
    }

    private void openLockDialog(UserRow user) {
        ComboBox<String> duration = new ComboBox<>();
        duration.getItems().setAll(
            "24 giờ",
            "3 ngày",
            "7 ngày",
            "30 ngày",
            "Vĩnh viễn"
        );
        duration.getSelectionModel().select("7 ngày");
        duration.setMaxWidth(Double.MAX_VALUE);

        TextArea reason = new TextArea();
        reason.setPromptText("Nhập lý do khóa...");
        reason.setPrefRowCount(3);
        reason.setWrapText(true);

        VBox content = new VBox(
            8,
            new Label("THỜI HẠN KHÓA"),
            duration,
            new Label("LÝ DO KHÓA"),
            reason
        );
        content.getStyleClass().add("lock-dialog-content");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Khóa @" + user.getUsername());
        dialog.initOwner(usersTable.getScene().getWindow());
        dialog.getDialogPane().setContent(content);
        copyStylesheets(dialog);
        dialog.getDialogPane().getStyleClass().add("user-lock-dialog");
        dialog.getDialogPane().setPrefWidth(420);

        ButtonType lockType = new ButtonType(
            "Khóa tài khoản",
            ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(
            ButtonType.CANCEL,
            lockType
        );

        Node cancelButton = dialog.getDialogPane()
            .lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().add("dialog-button-secondary");

        Node lockButton = dialog.getDialogPane()
            .lookupButton(lockType);
        lockButton.getStyleClass().add("dialog-button-danger");
        lockButton.addEventFilter(
            javafx.event.ActionEvent.ACTION,
            event -> {
                if (reason.getText().trim().isEmpty()) {
                    reason.requestFocus();
                    event.consume();
                }
            }
        );

        if (dialog.showAndWait().filter(lockType::equals).isPresent()) {
            userService.lockUser(
                user,
                calculateLockUntil(duration.getValue()),
                reason.getText().trim()
            );
            refreshUsers();
        }
    }

    private LocalDateTime calculateLockUntil(String duration) {
        LocalDateTime now = LocalDateTime.now();
        return switch (duration) {
            case "24 giờ" -> now.plusHours(24);
            case "3 ngày" -> now.plusDays(3);
            case "7 ngày" -> now.plusDays(7);
            case "30 ngày" -> now.plusDays(30);
            default -> null;
        };
    }

    private void confirmUnlock(UserRow user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mở khóa tài khoản");
        alert.setHeaderText(
            "Cho phép @" + user.getUsername()
                + " đăng nhập trở lại?"
        );
        alert.setContentText(
            "Trạng thái tài khoản sẽ chuyển sang đang hoạt động."
        );
        alert.initOwner(usersTable.getScene().getWindow());
        copyStylesheets(alert);

        alert.getDialogPane().getStyleClass().add("unlock-confirm-dialog");
        styleCancelButton(alert);

        if (alert.showAndWait()
            .filter(ButtonType.OK::equals)
            .isPresent()) {
            userService.unlockUser(user);
            refreshUsers();
        }
    }

    private void copyStylesheets(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().setAll(
            usersTable.getScene().getStylesheets()
        );
        dialog.getDialogPane().getStyleClass().add(
            "admin-dialog"
        );
    }

    private void styleCancelButton(Dialog<?> dialog) {
        Node cancelButton = dialog.getDialogPane()
            .lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("dialog-button-secondary");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initOwner(usersTable.getScene().getWindow());
        copyStylesheets(alert);
        alert.showAndWait();
    }
}
