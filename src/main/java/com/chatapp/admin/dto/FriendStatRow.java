package com.chatapp.admin.dto;

import java.util.Objects;

public record FriendStatRow(
    UserRow user,
    int directFriends,
    int secondDegreeFriends
) {
    public FriendStatRow {
        Objects.requireNonNull(user, "user");
        if (directFriends < 0 || secondDegreeFriends < 0) {
            throw new IllegalArgumentException(
                "Số lượng bạn bè không được âm"
            );
        }
    }

    public int totalConnections() {
        return directFriends + secondDegreeFriends;
    }
}
