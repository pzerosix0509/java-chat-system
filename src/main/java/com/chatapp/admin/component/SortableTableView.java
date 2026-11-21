package com.chatapp.admin.component;

import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class SortableTableView<T> extends TableView<T> {

    public SortableTableView() {
        getStyleClass().add("admin-table");
        setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        setPlaceholder(new Label("Không có dữ liệu phù hợp"));
    }
}
