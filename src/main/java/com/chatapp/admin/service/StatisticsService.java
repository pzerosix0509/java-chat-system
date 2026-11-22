package com.chatapp.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chatapp.admin.dto.ActivityStatRow;
import com.chatapp.admin.dto.FriendStatRow;
import com.chatapp.admin.dto.RegistrationStatRow;
import com.chatapp.admin.dto.UserRow;

public final class StatisticsService {

    private static final StatisticsService INSTANCE =
        new StatisticsService();

    private final UserManagementService userService =
        UserManagementService.getInstance();

    private StatisticsService() {
    }

    public static StatisticsService getInstance() {
        return INSTANCE;
    }

    public List<RegistrationStatRow> getRegistrationStats() {
        List<UserRow> users = userService.getUsers();
        int[] daysSinceActivity = {0, 1, 2, 4, 7, 11, 18};
        List<RegistrationStatRow> result = new ArrayList<>();

        for (int index = 0; index < users.size(); index++) {
            UserRow user = users.get(index);
            result.add(new RegistrationStatRow(
                user,
                user.getCreatedAt(),
                LocalDateTime.of(2026, 11, 22, 10, 30)
                    .minusDays(daysSinceActivity[index % daysSinceActivity.length])
                    .minusMinutes(index * 17L)
            ));
        }
        return List.copyOf(result);
    }

    public List<FriendStatRow> getFriendStats() {
        int[] direct = {24, 18, 31, 12, 42, 28, 9};
        int[] secondDegree = {81, 67, 95, 43, 126, 74, 32};
        List<UserRow> users = userService.getUsers();
        List<FriendStatRow> result = new ArrayList<>();

        for (int index = 0; index < users.size(); index++) {
            result.add(new FriendStatRow(
                users.get(index),
                direct[index % direct.length],
                secondDegree[index % secondDegree.length]
            ));
        }
        return List.copyOf(result);
    }

    public List<ActivityStatRow> getActivityStats() {
        int[] opens = {38, 31, 22, 19, 45, 27, 12};
        int[] people = {16, 12, 8, 7, 21, 10, 4};
        int[] groups = {6, 4, 3, 5, 8, 2, 1};
        int[] minutesAgo = {5, 12, 35, 60, 120, 1_440, 4_320};
        List<UserRow> users = userService.getUsers();
        List<ActivityStatRow> result = new ArrayList<>();

        for (int index = 0; index < users.size(); index++) {
            result.add(new ActivityStatRow(
                users.get(index),
                opens[index % opens.length],
                people[index % people.length],
                groups[index % groups.length],
                LocalDateTime.of(2026, 11, 22, 15, 0)
                    .minusMinutes(minutesAgo[index % minutesAgo.length])
            ));
        }
        return List.copyOf(result);
    }
}
