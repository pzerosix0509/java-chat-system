package com.chatapp.admin.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import com.chatapp.admin.component.DatePickerStyler;
import com.chatapp.admin.dto.UserRow;
import com.chatapp.admin.dto.UserRow.Status;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class UserFormController {

    @FXML
    private Label formDescriptionLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField addressField;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private VBox passwordSection;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label validationLabel;

    private UserRow editingUser;

    @FXML
    private void initialize() {
        DatePickerStyler.installCalendarIcon(birthDatePicker);
        genderComboBox.getItems().setAll(
            "Nam",
            "Nữ",
            "Khác"
        );
        statusComboBox.getItems().setAll(
            Status.ACTIVE.getLabel(),
            Status.LOCKED.getLabel()
        );

        genderComboBox.getSelectionModel().selectFirst();
        statusComboBox.getSelectionModel().selectFirst();
        hideValidation();
    }

    public void configure(UserRow user) {
        editingUser = user;
        boolean creating = user == null;

        passwordSection.setVisible(creating);
        passwordSection.setManaged(creating);

        if (creating) {
            formDescriptionLabel.setText(
                "Tạo tài khoản mới cho hệ thống chat."
            );
            return;
        }

        formDescriptionLabel.setText(
            "Cập nhật thông tin hồ sơ người dùng."
        );
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        addressField.setText(user.getAddress());
        birthDatePicker.setValue(user.getBirthDate());
        genderComboBox.setValue(user.getGender());
        statusComboBox.setValue(user.getStatus().getLabel());
    }

    public Optional<String> validateForm() {
        hideValidation();

        if (usernameField.getText().trim().isEmpty()
            || fullNameField.getText().trim().isEmpty()
            || emailField.getText().trim().isEmpty()) {
            return validationError(
                "Username, họ tên và email không được để trống."
            );
        }

        if (!emailField.getText().contains("@")) {
            return validationError("Email không hợp lệ.");
        }

        if (birthDatePicker.getValue() == null) {
            return validationError("Vui lòng chọn ngày sinh.");
        }

        if (editingUser == null) {
            String password = passwordField.getText();
            String confirmation = confirmPasswordField.getText();

            if (password.length() < 8) {
                return validationError(
                    "Mật khẩu phải có ít nhất 8 ký tự."
                );
            }
            if (!password.equals(confirmation)) {
                return validationError(
                    "Xác nhận mật khẩu không trùng khớp."
                );
            }
        }

        return Optional.empty();
    }

    public UserRow buildFormValue() {
        long id = editingUser == null ? 0 : editingUser.getId();
        LocalDateTime createdAt = editingUser == null
            ? LocalDateTime.now()
            : editingUser.getCreatedAt();

        Status status = Status.ACTIVE.getLabel().equals(
            statusComboBox.getValue()
        ) ? Status.ACTIVE : Status.LOCKED;

        return new UserRow(
            id,
            usernameField.getText().trim(),
            fullNameField.getText().trim(),
            addressField.getText().trim(),
            birthDatePicker.getValue(),
            genderComboBox.getValue(),
            emailField.getText().trim(),
            createdAt,
            status
        );
    }

    private Optional<String> validationError(String message) {
        validationLabel.setText(message);
        validationLabel.setVisible(true);
        validationLabel.setManaged(true);
        return Optional.of(message);
    }

    private void hideValidation() {
        validationLabel.setText("");
        validationLabel.setVisible(false);
        validationLabel.setManaged(false);
    }
}
