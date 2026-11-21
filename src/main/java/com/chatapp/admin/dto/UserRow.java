package com.chatapp.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class UserRow {

    public enum Status {
        ACTIVE("Đang hoạt động"),
        LOCKED("Đã khóa"),
        DELETED("Đã xóa");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final long id;
    private final LocalDateTime createdAt;
    private String username;
    private String fullName;
    private String address;
    private LocalDate birthDate;
    private String gender;
    private String email;
    private Status status;
    private LocalDateTime lockedUntil;
    private String lockReason;

    public UserRow(
        long id,
        String username,
        String fullName,
        String address,
        LocalDate birthDate,
        String gender,
        String email,
        LocalDateTime createdAt,
        Status status
    ) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.address = address;
        this.birthDate = birthDate;
        this.gender = gender;
        this.email = email;
        this.createdAt = createdAt;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public String getInitials() {
        String[] words = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (int index = Math.max(0, words.length - 2);
             index < words.length;
             index++) {
            if (!words[index].isBlank()) {
                initials.append(words[index].charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }
}
