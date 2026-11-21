package com.chatapp.admin.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.kordamp.ikonli.javafx.FontIcon;

import com.chatapp.admin.component.SortableTableView;
import com.chatapp.admin.dto.UserRow;
import com.chatapp.admin.dto.UserRow.Status;
import com.chatapp.admin.navigation.AdminNavigator;
import com.chatapp.admin.service.UserManagementService;
import com.chatapp.admin.service.UserManagementService.FriendEntry;
import com.chatapp.admin.service.UserManagementService.UserLogin;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class UserDetailController {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");

    private final UserManagementService userService =
        UserManagementService.getInstance();

    @FXML
    private Label avatarLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label identityLabel;

    @FXML
    private Label statusBadge;

    @FXML
    private Button lockButton;

    @FXML
    private FontIcon lockButtonIcon;

    @FXML
    private Label usernameValue;

    @FXML
    private Label emailValue;

    @FXML
    private Label genderValue;

    @FXML
    private Label birthDateValue;

    @FXML
    private Label addressValue;

    @FXML
    private Label createdAtValue;

    @FXML
    private Label lockInfoLabel;

    @FXML
    private SortableTableView<UserLogin> loginTable;

    @FXML
    private TableColumn<UserLogin, LocalDateTime> loginTimeColumn;

    @FXML
    private TableColumn<UserLogin, String> loginDeviceColumn;

    @FXML
    private TableColumn<UserLogin, String> loginIpColumn;

    @FXML
    private TableColumn<UserLogin, String> loginResultColumn;

    @FXML
    private SortableTableView<FriendEntry> friendsTable;

    @FXML
    private TableColumn<FriendEntry, UserRow> friendUserColumn;

    @FXML
    private TableColumn<FriendEntry, String> friendEmailColumn;

    @FXML
    private TableColumn<FriendEntry, String> friendSinceColumn;

    @FXML
    private TableColumn<FriendEntry, String> friendRelationColumn;

    private UserRow user;
    private Runnable backHandler = () -> {
    };
    private Runnable deletedHandler = () -> {
    };

    @FXML
    private void initialize() {
        configureLoginTable();
        configureFriendsTable();
    }

    public void setUser(UserRow user) {
        this.user = user;
        refreshView();
    }

    public void setBackHandler(Runnable backHandler) {
        this.backHandler = backHandler != null
            ? backHandler
            : () -> {
            };
    }

    public void setDeletedHandler(Runnable deletedHandler) {
        this.deletedHandler = deletedHandler != null
            ? deletedHandler
            : () -> {
            };
    }

    @FXML
    private void goBack() {
        backHandler.run();
    }

    @FXML
    private void editUser() {
        openUserForm().ifPresent(formValue -> {
            userService.updateUser(user, formValue);
            refreshView();
        });
    }

    @FXML
    private void toggleLock() {
        if (user.getStatus() == Status.LOCKED) {
            confirmUnlock();
        } else {
            openLockDialog();
        }
    }

    @FXML
    private void resetPassword() {
        PasswordField password = new PasswordField();
        password.setPromptText("Mật khẩu mới");
        PasswordField confirmation = new PasswordField();
        confirmation.setPromptText("Nhập lại mật khẩu");
        Label validation = new Label();
        validation.getStyleClass().add("form-validation");
        validation.setVisible(false);
        validation.setManaged(false);

        Label description = new Label(
            "Tạo mật khẩu mới cho @" + user.getUsername()
        );
        description.getStyleClass().add("dialog-description");
        Label passwordLabel = new Label("MẬT KHẨU MỚI");
        passwordLabel.getStyleClass().add("dialog-field-label");
        Label confirmationLabel = new Label("XÁC NHẬN MẬT KHẨU");
        confirmationLabel.getStyleClass().add("dialog-field-label");

        VBox content = new VBox(
            10,
            description,
            passwordLabel,
            password,
            confirmationLabel,
            confirmation,
            validation
        );
        content.getStyleClass().add("password-dialog-content");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đặt lại mật khẩu");
        dialog.initOwner(avatarLabel.getScene().getWindow());
        dialog.getDialogPane().setContent(content);
        copyStylesheets(dialog);

        ButtonType saveType = new ButtonType(
            "Cập nhật mật khẩu",
            ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(
            ButtonType.CANCEL,
            saveType
        );
        dialog.getDialogPane().getStyleClass().add("password-reset-dialog");
        dialog.getDialogPane().setPrefWidth(420);
        styleCancelButton(dialog);

        Node saveButton = dialog.getDialogPane()
            .lookupButton(saveType);
        saveButton.getStyleClass().add("button-primary");
        saveButton.addEventFilter(
            javafx.event.ActionEvent.ACTION,
            event -> {
                String error = null;
                if (password.getText().length() < 8) {
                    error = "Mật khẩu phải có ít nhất 8 ký tự.";
                } else if (!password.getText().equals(
                    confirmation.getText()
                )) {
                    error = "Xác nhận mật khẩu không trùng khớp.";
                }

                if (error != null) {
                    validation.setText(error);
                    validation.setVisible(true);
                    validation.setManaged(true);
                    event.consume();
                }
            }
        );

        if (dialog.showAndWait().filter(saveType::equals).isPresent()) {
            userService.resetPassword(user, password.getText());
            showInformation(
                "Đã cập nhật mật khẩu cho @" + user.getUsername()
            );
        }
    }

    @FXML
    private void deleteUser() {
        TextField confirmationField = new TextField();
        confirmationField.setPromptText("Nhập username người dùng");
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("form-validation");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Label warning = new Label(
            "Tài khoản sẽ bị xóa mềm và không thể đăng nhập mới."
        );
        warning.setWrapText(true);
        warning.getStyleClass().add("dialog-warning-copy");
        Label instruction = new Label(
            "Nhập username @" + user.getUsername() + " để xác nhận."
        );
        instruction.getStyleClass().add("dialog-field-label");

        VBox content = new VBox(
            12,
            warning,
            instruction,
            confirmationField,
            errorLabel
        );
        content.getStyleClass().add("delete-dialog-content");

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Xóa người dùng");
        dialog.initOwner(avatarLabel.getScene().getWindow());
        dialog.getDialogPane().setContent(content);
        copyStylesheets(dialog);

        ButtonType deleteType = new ButtonType(
            "Xóa người dùng",
            ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(
            ButtonType.CANCEL,
            deleteType
        );
        dialog.getDialogPane().getStyleClass().add("user-delete-dialog");
        dialog.getDialogPane().setPrefWidth(440);
        styleCancelButton(dialog);

        Node deleteButton = dialog.getDialogPane()
            .lookupButton(deleteType);
        deleteButton.getStyleClass().add("dialog-button-danger");
        deleteButton.addEventFilter(
            javafx.event.ActionEvent.ACTION,
            event -> {
                if (!user.getUsername().equals(
                    confirmationField.getText().trim()
                )) {
                    errorLabel.setText("Username xác nhận không chính xác.");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    confirmationField.requestFocus();
                    event.consume();
                }
            }
        );

        if (dialog.showAndWait().filter(deleteType::equals).isEmpty()) {
            return;
        }

        userService.softDelete(user);
        deletedHandler.run();
    }

    private void configureLoginTable() {
        loginTimeColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(
                data.getValue().time()
            )
        );
        loginTimeColumn.setCellFactory(column ->
            new TableCell<>() {
                @Override
                protected void updateItem(
                    LocalDateTime value,
                    boolean empty
                ) {
                    super.updateItem(value, empty);
                    setText(empty || value == null
                        ? null
                        : value.format(DATE_TIME_FORMAT));
                }
            }
        );
        loginDeviceColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().device()
            )
        );
        loginIpColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().ipAddress()
            )
        );
        loginResultColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().result()
            )
        );
        loginResultColumn.setCellFactory(column ->
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

    private void configureFriendsTable() {
        friendUserColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(
                data.getValue().user()
            )
        );
        friendUserColumn.setCellFactory(column ->
            new TableCell<>() {
                @Override
                protected void updateItem(
                    UserRow friend,
                    boolean empty
                ) {
                    super.updateItem(friend, empty);
                    if (empty || friend == null) {
                        setGraphic(null);
                        return;
                    }
                    Label avatar = new Label(friend.getInitials());
                    avatar.getStyleClass().add("table-user-avatar");
                    Label name = new Label(friend.getFullName());
                    name.getStyleClass().add("table-user-name");
                    Label username = new Label(
                        "@" + friend.getUsername()
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
            }
        );
        friendEmailColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().user().getEmail()
            )
        );
        friendSinceColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().connectedSince().format(DATE_FORMAT)
            )
        );
        friendRelationColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().relation()
            )
        );
    }

    private void refreshView() {
        if (user == null) {
            return;
        }

        avatarLabel.setText(user.getInitials());
        fullNameLabel.setText(user.getFullName());
        identityLabel.setText(
            "@" + user.getUsername() + " · " + user.getEmail()
        );

        usernameValue.setText("@" + user.getUsername());
        emailValue.setText(user.getEmail());
        genderValue.setText(user.getGender());
        birthDateValue.setText(user.getBirthDate().format(DATE_FORMAT));
        addressValue.setText(user.getAddress());
        createdAtValue.setText(
            user.getCreatedAt().format(DATE_TIME_FORMAT)
        );

        updateStatus();

        loginTable.setItems(FXCollections.observableArrayList(
            userService.getLoginHistory(user)
        ));
        friendsTable.setItems(FXCollections.observableArrayList(
            userService.getFriends(user)
        ));
    }

    private void updateStatus() {
        boolean locked = user.getStatus() == Status.LOCKED;
        statusBadge.setText(user.getStatus().getLabel());
        statusBadge.getStyleClass().removeAll(
            "detail-status-active",
            "detail-status-locked"
        );
        statusBadge.getStyleClass().add(
            locked
                ? "detail-status-locked"
                : "detail-status-active"
        );

        lockButton.setText(locked ? "Mở khóa" : "Khóa tài khoản");
        lockButtonIcon.setIconLiteral(
            locked ? "fas-lock-open" : "fas-lock"
        );
        lockButton.getStyleClass().removeAll(
            "button-lock",
            "button-unlock"
        );
        lockButton.getStyleClass().add(
            locked ? "button-unlock" : "button-lock"
        );

        if (!locked) {
            lockInfoLabel.setText(
                "Tài khoản hiện có thể đăng nhập bình thường."
            );
            return;
        }

        String until = user.getLockedUntil() == null
            ? "Vĩnh viễn"
            : user.getLockedUntil().format(DATE_TIME_FORMAT);
        lockInfoLabel.setText(
            "Khóa đến: " + until + " · Lý do: "
                + user.getLockReason()
        );
    }

    private Optional<UserRow> openUserForm() {
        try {
            FXMLLoader loader = AdminNavigator.createUserFormLoader();
            Parent form = loader.load();
            UserFormController controller = loader.getController();
            controller.configure(user);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Cập nhật người dùng");
            dialog.initOwner(avatarLabel.getScene().getWindow());
            dialog.getDialogPane().setContent(form);
            copyStylesheets(dialog);

            ButtonType saveType = new ButtonType(
                "Lưu thay đổi",
                ButtonBar.ButtonData.OK_DONE
            );
            dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.CANCEL,
                saveType
            );
            dialog.getDialogPane().getStyleClass().add("user-form-dialog");
            dialog.getDialogPane().setPrefWidth(620);
            styleCancelButton(dialog);

            Node saveButton = dialog.getDialogPane()
                .lookupButton(saveType);
            saveButton.getStyleClass().add("button-primary");
            saveButton.addEventFilter(
                javafx.event.ActionEvent.ACTION,
                event -> controller.validateForm()
                    .ifPresent(message -> event.consume())
            );

            if (dialog.showAndWait().filter(saveType::equals).isPresent()) {
                return Optional.of(controller.buildFormValue());
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            showError("Không thể mở biểu mẫu người dùng.");
        }
        return Optional.empty();
    }

    private void openLockDialog() {
        ComboBox<String> duration = new ComboBox<>();
        duration.getItems().setAll(
            "24 giờ", "3 ngày", "7 ngày", "30 ngày", "Vĩnh viễn"
        );
        duration.setValue("7 ngày");
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
        dialog.initOwner(avatarLabel.getScene().getWindow());
        dialog.getDialogPane().setContent(content);
        copyStylesheets(dialog);

        ButtonType lockType = new ButtonType(
            "Khóa tài khoản",
            ButtonBar.ButtonData.OK_DONE
        );
        dialog.getDialogPane().getButtonTypes().addAll(
            ButtonType.CANCEL,
            lockType
        );
        dialog.getDialogPane().getStyleClass().add("user-lock-dialog");
        dialog.getDialogPane().setPrefWidth(420);
        styleCancelButton(dialog);

        Node lockNode = dialog.getDialogPane().lookupButton(lockType);
        lockNode.getStyleClass().add("dialog-button-danger");
        lockNode.addEventFilter(
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
            refreshView();
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

    private void confirmUnlock() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mở khóa tài khoản");
        alert.setHeaderText(
            "Cho phép @" + user.getUsername()
                + " đăng nhập trở lại?"
        );
        alert.setContentText(
            "Trạng thái tài khoản sẽ chuyển sang đang hoạt động."
        );
        alert.initOwner(avatarLabel.getScene().getWindow());
        copyStylesheets(alert);
        alert.getDialogPane().getStyleClass().add("unlock-confirm-dialog");
        styleCancelButton(alert);

        if (alert.showAndWait().filter(ButtonType.OK::equals).isPresent()) {
            userService.unlockUser(user);
            refreshView();
        }
    }

    private void copyStylesheets(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().setAll(
            avatarLabel.getScene().getStylesheets()
        );
        dialog.getDialogPane().getStyleClass().add("admin-dialog");
    }

    private void styleCancelButton(Dialog<?> dialog) {
        Node cancelButton = dialog.getDialogPane()
            .lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("dialog-button-secondary");
        }
    }

    private void showInformation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.initOwner(avatarLabel.getScene().getWindow());
        copyStylesheets(alert);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initOwner(avatarLabel.getScene().getWindow());
        copyStylesheets(alert);
        alert.showAndWait();
    }
}
