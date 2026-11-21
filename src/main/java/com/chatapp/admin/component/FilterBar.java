package com.chatapp.admin.component;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

public class FilterBar extends HBox {

    public FilterBar() {
        getStyleClass().add("filter-bar");
        setAlignment(Pos.BOTTOM_LEFT);
        setSpacing(10);
    }
}
