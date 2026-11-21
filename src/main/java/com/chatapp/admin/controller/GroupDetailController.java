package com.chatapp.admin.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.chatapp.admin.component.SortableTableView;
import com.chatapp.admin.dto.GroupRow;
import com.chatapp.admin.dto.GroupRow.MemberRow;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;

public class GroupDetailController {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    @FXML
    private Label groupInitialsLabel;

    @FXML
    private Label groupNameLabel;

    @FXML
    private Label groupCodeLabel;

    @FXML
    private Label creatorLabel;

    @FXML
    private Label createdAtLabel;

    @FXML
    private Label memberCountLabel;

    @FXML
    private Label adminCountLabel;

    @FXML
    private SortableTableView<MemberRow> membersTable;

    @FXML
    private TableColumn<MemberRow, MemberRow> memberUserColumn;

    @FXML
    private TableColumn<MemberRow, String> memberEmailColumn;

    @FXML
    private TableColumn<MemberRow, String> memberJoinedColumn;

    @FXML
    private TableColumn<MemberRow, String> memberRoleColumn;

    @FXML
    private SortableTableView<MemberRow> adminsTable;

    @FXML
    private TableColumn<MemberRow, MemberRow> adminUserColumn;

    @FXML
    private TableColumn<MemberRow, String> adminEmailColumn;

    @FXML
    private TableColumn<MemberRow, String> adminJoinedColumn;

    @FXML
    private TableColumn<MemberRow, String> adminRoleColumn;

    private GroupRow group;
    private Runnable backHandler = () -> {
    };

    @FXML
    private void initialize() {
        configureMemberTable(
            membersTable,
            memberUserColumn,
            memberEmailColumn,
            memberJoinedColumn,
            memberRoleColumn
        );
        configureMemberTable(
            adminsTable,
            adminUserColumn,
            adminEmailColumn,
            adminJoinedColumn,
            adminRoleColumn
        );
    }

    public void setGroup(GroupRow group) {
        this.group = group;
        refreshView();
    }

    public void setBackHandler(Runnable backHandler) {
        this.backHandler = backHandler != null
            ? backHandler
            : () -> {
            };
    }

    @FXML
    private void goBack() {
        backHandler.run();
    }

    private void refreshView() {
        if (group == null) {
            return;
        }

        groupInitialsLabel.setText(group.getInitials());
        groupNameLabel.setText(group.getName());
        groupCodeLabel.setText("ID #" + group.getGroupCode());
        creatorLabel.setText(group.getCreator().getFullName());
        createdAtLabel.setText(group.getCreatedAt().format(DATE_TIME_FORMAT));
        memberCountLabel.setText(
            Integer.toString(group.getMemberCount())
        );
        adminCountLabel.setText(
            Integer.toString(group.getAdminCount())
        );
        membersTable.setItems(FXCollections.observableArrayList(
            group.getMembers()
        ));
        adminsTable.setItems(FXCollections.observableArrayList(
            group.getAdministrators()
        ));
    }

    private void configureMemberTable(
        SortableTableView<MemberRow> table,
        TableColumn<MemberRow, MemberRow> userColumn,
        TableColumn<MemberRow, String> emailColumn,
        TableColumn<MemberRow, String> joinedColumn,
        TableColumn<MemberRow, String> roleColumn
    ) {
        userColumn.setCellValueFactory(
            data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                data.getValue()
            )
        );
        userColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(MemberRow member, boolean empty) {
                super.updateItem(member, empty);
                if (empty || member == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label avatar = new Label(member.user().getInitials());
                avatar.getStyleClass().add("table-user-avatar");
                Label name = new Label(member.user().getFullName());
                name.getStyleClass().add("table-user-name");
                Label username = new Label(
                    "@" + member.user().getUsername()
                );
                username.getStyleClass().add("table-user-username");
                javafx.scene.layout.HBox content =
                    new javafx.scene.layout.HBox(
                        9,
                        avatar,
                        new javafx.scene.layout.VBox(3, name, username)
                    );
                content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                setGraphic(content);
            }
        });
        emailColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().user().getEmail()
            )
        );
        joinedColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().joinedAt().format(
                    DateTimeFormatter.ofPattern("dd/MM/uuuu")
                )
            )
        );
        roleColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().administrator()
                ? "Quản trị viên"
                : "Thành viên"
        ));
    }
}
