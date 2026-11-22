package com.chatapp.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.chatapp.admin.dto.GroupRow;
import com.chatapp.admin.dto.GroupRow.MemberRow;
import com.chatapp.admin.dto.UserRow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class GroupAdminService {

    private static final GroupAdminService INSTANCE =
        new GroupAdminService();

    private final ObservableList<GroupRow> groups =
        FXCollections.observableArrayList();

    private GroupAdminService() {
        seedGroups();
    }

    public static GroupAdminService getInstance() {
        return INSTANCE;
    }

    public ObservableList<GroupRow> getGroups() {
        return FXCollections.unmodifiableObservableList(groups);
    }

    public Optional<GroupRow> findById(long id) {
        return groups.stream()
            .filter(group -> group.getId() == id)
            .findFirst();
    }

    private void seedGroups() {
        UserManagementService users = UserManagementService.getInstance();
        List<UserRow> people = users.getUsers();

        groups.addAll(
            createGroup(
                1, "BoTisUeuDang Group", people.get(0),
                LocalDateTime.of(2024, 10, 13, 8, 45), 12, 2,
                people, new int[] {0, 1, 2, 3, 4, 5}
            ),
            createGroup(
                2, "Nhóm đồ án Web", people.get(1),
                LocalDateTime.of(2024, 10, 9, 19, 20), 5, 1,
                people, new int[] {1, 0, 3, 5, 6}
            ),
            createGroup(
                3, "Chuyện cuối tuần", people.get(2),
                LocalDateTime.of(2024, 10, 2, 12, 10), 26, 3,
                people, new int[] {2, 0, 1, 4, 6, 5}
            ),
            createGroup(
                4, "UIT K19 Friends", people.get(4),
                LocalDateTime.of(2024, 9, 27, 21, 5), 18, 2,
                people, new int[] {4, 1, 0, 2, 3, 6}
            ),
            createGroup(
                5, "Hội mê Java", people.get(5),
                LocalDateTime.of(2024, 9, 18, 10, 0), 42, 4,
                people, new int[] {5, 0, 1, 2, 3, 4, 6}
            )
        );
    }

    private GroupRow createGroup(
        long id,
        String name,
        UserRow creator,
        LocalDateTime createdAt,
        int memberCount,
        int adminCount,
        List<UserRow> users,
        int[] memberIndexes
    ) {
        List<MemberRow> members = java.util.stream.IntStream
            .range(0, memberCount)
            .mapToObj(index -> {
                UserRow user = index < memberIndexes.length
                    ? users.get(memberIndexes[index])
                    : createSyntheticMember(id, index);
                boolean administrator = index < adminCount;
                return new MemberRow(
                    user,
                    LocalDate.of(2024, 10, 13).plusDays(id + index),
                    administrator
                );
            })
            .toList();

        return new GroupRow(
            id,
            name,
            creator,
            createdAt,
            memberCount,
            adminCount,
            members
        );
    }

    private UserRow createSyntheticMember(long groupId, int index) {
        int displayNumber = index + 1;
        return new UserRow(
            10_000L + groupId * 100 + index,
            "member" + groupId + "_" + displayNumber,
            String.format("Thành viên %02d", displayNumber),
            "TP.HCM",
            LocalDate.of(2002, 1, 1).plusDays(index),
            "Khác",
            "member" + groupId + "_" + displayNumber
                + "@mybrand.vn",
            LocalDateTime.of(2024, 9, 1, 8, 0)
                .plusDays(index),
            UserRow.Status.ACTIVE
        );
    }
}
