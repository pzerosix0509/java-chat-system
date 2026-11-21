package com.chatapp.admin.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.kordamp.ikonli.javafx.FontIcon;

import com.chatapp.admin.component.SortableTableView;
import com.chatapp.admin.dto.GroupRow;
import com.chatapp.admin.navigation.AdminRoute;
import com.chatapp.admin.service.GroupAdminService;

import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GroupListController {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm");

    private final GroupAdminService groupService =
        GroupAdminService.getInstance();

    @FXML
    private TextField keywordField;

    @FXML
    private ComboBox<String> sortFieldFilter;

    @FXML
    private ComboBox<String> sortDirectionFilter;

    @FXML
    private SortableTableView<GroupRow> groupsTable;

    @FXML
    private TableColumn<GroupRow, GroupRow> groupColumn;

    @FXML
    private TableColumn<GroupRow, String> creatorColumn;

    @FXML
    private TableColumn<GroupRow, LocalDateTime> createdAtColumn;

    @FXML
    private TableColumn<GroupRow, Integer> memberCountColumn;

    @FXML
    private TableColumn<GroupRow, Integer> adminCountColumn;

    @FXML
    private TableColumn<GroupRow, GroupRow> actionColumn;

    private Consumer<GroupRow> viewGroupHandler = group -> {
    };

    @FXML
    private void initialize() {
        configureFilters();
        configureTable();
        refreshGroups();
    }

    public void setViewGroupHandler(Consumer<GroupRow> viewGroupHandler) {
        this.viewGroupHandler = viewGroupHandler != null
            ? viewGroupHandler
            : group -> {
            };
    }

    @FXML
    private void applyFilters() {
        refreshGroups();
    }

    @FXML
    private void resetFilters() {
        keywordField.clear();
        sortFieldFilter.setValue("Tên nhóm");
        sortDirectionFilter.setValue("Tăng dần");
        refreshGroups();
    }

    private void configureFilters() {
        sortFieldFilter.getItems().setAll("Tên nhóm", "Thời gian tạo");
        sortDirectionFilter.getItems().setAll("Tăng dần", "Giảm dần");
        sortFieldFilter.setValue("Tên nhóm");
        sortDirectionFilter.setValue("Tăng dần");
    }

    private void configureTable() {
        groupColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue())
        );
        groupColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(GroupRow group, boolean empty) {
                super.updateItem(group, empty);
                if (empty || group == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label cover = new Label(group.getInitials());
                cover.getStyleClass().add("group-cover");
                Label name = new Label(group.getName());
                name.getStyleClass().add("group-table-name");
                Label code = new Label("ID #" + group.getGroupCode());
                code.getStyleClass().add("group-table-code");

                HBox content = new HBox(
                    9,
                    cover,
                    new VBox(3, name, code)
                );
                content.setAlignment(Pos.CENTER_LEFT);
                setGraphic(content);
            }
        });

        creatorColumn.setCellValueFactory(
            data -> new ReadOnlyStringWrapper(
                data.getValue().getCreator().getFullName()
            )
        );
        createdAtColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(
                data.getValue().getCreatedAt()
            )
        );
        createdAtColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null
                    ? null
                    : value.format(DATE_TIME_FORMAT));
            }
        });
        memberCountColumn.setCellValueFactory(
            data -> new ReadOnlyIntegerWrapper(
                data.getValue().getMemberCount()
            ).asObject()
        );
        adminCountColumn.setCellValueFactory(
            data -> new ReadOnlyIntegerWrapper(
                data.getValue().getAdminCount()
            ).asObject()
        );
        actionColumn.setCellValueFactory(
            data -> new ReadOnlyObjectWrapper<>(data.getValue())
        );
        actionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(GroupRow group, boolean empty) {
                super.updateItem(group, empty);
                if (empty || group == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Button detailButton = new Button("Chi tiết");
                detailButton.setGraphic(new FontIcon("fas-eye"));
                detailButton.getStyleClass().addAll(
                    "table-action-button",
                    "user-list-action-detail"
                );
                detailButton.setOnAction(
                    event -> viewGroupHandler.accept(group)
                );
                setGraphic(detailButton);
            }
        });
    }

    private void refreshGroups() {
        String keyword = keywordField.getText()
            .trim()
            .toLowerCase(Locale.ROOT);

        List<GroupRow> filtered = groupService.getGroups().stream()
            .filter(group -> group.getName()
                .toLowerCase(Locale.ROOT)
                .contains(keyword))
            .sorted(createComparator())
            .toList();

        groupsTable.setItems(FXCollections.observableArrayList(filtered));
        groupsTable.refresh();
    }

    private Comparator<GroupRow> createComparator() {
        Comparator<GroupRow> comparator = "Thời gian tạo".equals(
            sortFieldFilter.getValue()
        )
            ? Comparator.comparing(GroupRow::getCreatedAt)
            : Comparator.comparing(
                GroupRow::getName,
                String.CASE_INSENSITIVE_ORDER
            );

        if ("Giảm dần".equals(sortDirectionFilter.getValue())) {
            comparator = comparator.reversed();
        }
        return comparator;
    }
}
