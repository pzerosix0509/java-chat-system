package com.chatapp.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class GroupRow {

    public record MemberRow(
        UserRow user,
        LocalDate joinedAt,
        boolean administrator
    ) {
    }

    private final long id;
    private final String name;
    private final UserRow creator;
    private final LocalDateTime createdAt;
    private final int memberCount;
    private final int adminCount;
    private final List<MemberRow> members;

    public GroupRow(
        long id,
        String name,
        UserRow creator,
        LocalDateTime createdAt,
        int memberCount,
        int adminCount,
        List<MemberRow> members
    ) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
        this.adminCount = adminCount;
        this.members = List.copyOf(members);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UserRow getCreator() {
        return creator;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public int getAdminCount() {
        return adminCount;
    }

    public List<MemberRow> getMembers() {
        return members;
    }

    public List<MemberRow> getAdministrators() {
        return members.stream()
            .filter(MemberRow::administrator)
            .toList();
    }

    public String getGroupCode() {
        return String.format("GR%03d", id);
    }

    public String getInitials() {
        String[] words = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int index = 0; index < Math.min(2, words.length); index++) {
            initials.append(words[index].charAt(0));
        }
        return initials.toString().toUpperCase();
    }
}
