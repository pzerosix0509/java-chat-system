package com.chatapp.admin.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public record RegistrationStatRow(
    UserRow user,
    LocalDateTime registeredAt,
    LocalDateTime lastActivityAt
) {
    public RegistrationStatRow {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(registeredAt, "registeredAt");
        Objects.requireNonNull(lastActivityAt, "lastActivityAt");
    }
}
