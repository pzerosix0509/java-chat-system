package com.chatapp.admin.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public record ActivityStatRow(
    UserRow user,
    int appOpenCount,
    int peopleChattedCount,
    int groupsChattedCount,
    LocalDateTime lastActivityAt
) {
    public ActivityStatRow {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(lastActivityAt, "lastActivityAt");
        if (appOpenCount < 0
            || peopleChattedCount < 0
            || groupsChattedCount < 0) {
            throw new IllegalArgumentException(
                "Chỉ số hoạt động không được âm"
            );
        }
    }
}
