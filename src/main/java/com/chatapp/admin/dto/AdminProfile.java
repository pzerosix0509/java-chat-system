package com.chatapp.admin.dto;

public record AdminProfile(
    long id,
    String username,
    String displayName,
    String email,
    String role,
    String avatarText
) {
}
