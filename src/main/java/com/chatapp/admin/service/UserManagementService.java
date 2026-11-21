package com.chatapp.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.chatapp.admin.dto.UserRow;
import com.chatapp.admin.dto.UserRow.Status;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class UserManagementService {

    public record UserLogin(
        LocalDateTime time,
        String device,
        String ipAddress,
        String result
    ) {
    }

    public record FriendEntry(
        UserRow user,
        LocalDate connectedSince,
        String relation
    ) {
    }

    private static final UserManagementService INSTANCE =
        new UserManagementService();

    private final ObservableList<UserRow> users =
        FXCollections.observableArrayList();

    private final AtomicLong idSequence = new AtomicLong(8);

    private UserManagementService() {
        seedUsers();
    }

    public static UserManagementService getInstance() {
        return INSTANCE;
    }

    public ObservableList<UserRow> getUsers() {
        return users;
    }

    public Optional<UserRow> findById(long id) {
        return users.stream()
            .filter(user -> user.getId() == id)
            .findFirst();
    }

    public UserRow createUser(UserRow formValue) {
        UserRow created = new UserRow(
            idSequence.getAndIncrement(),
            formValue.getUsername(),
            formValue.getFullName(),
            formValue.getAddress(),
            formValue.getBirthDate(),
            formValue.getGender(),
            formValue.getEmail(),
            LocalDateTime.now(),
            formValue.getStatus()
        );
        users.add(0, created);
        return created;
    }

    public void updateUser(UserRow target, UserRow formValue) {
        target.setUsername(formValue.getUsername());
        target.setFullName(formValue.getFullName());
        target.setAddress(formValue.getAddress());
        target.setBirthDate(formValue.getBirthDate());
        target.setGender(formValue.getGender());
        target.setEmail(formValue.getEmail());

        if (formValue.getStatus() == Status.ACTIVE
            && target.getStatus() == Status.LOCKED) {
            unlockUser(target);
        } else if (formValue.getStatus() != Status.DELETED) {
            target.setStatus(formValue.getStatus());
        }
    }

    public void lockUser(
        UserRow user,
        LocalDateTime lockedUntil,
        String reason
    ) {
        user.setStatus(Status.LOCKED);
        user.setLockedUntil(lockedUntil);
        user.setLockReason(reason);
    }

    public void unlockUser(UserRow user) {
        user.setStatus(Status.ACTIVE);
        user.setLockedUntil(null);
        user.setLockReason(null);
    }

    public void resetPassword(
        UserRow user,
        String newPassword
    ) {
        // Mock UI: chưa lưu password vào bộ nhớ hay database.
    }

    public void softDelete(UserRow user) {
        user.setStatus(Status.DELETED);
    }

    public List<UserLogin> getLoginHistory(UserRow user) {
        return List.of(
            new UserLogin(
                LocalDateTime.of(2026, 11, 17, 9, 42, 18),
                "Chrome · Windows 11",
                "103.28.36.12",
                "Thành công"
            ),
            new UserLogin(
                LocalDateTime.of(2026, 11, 16, 21, 10, 32),
                "Safari · iPhone",
                "14.161.8.94",
                "Thành công"
            ),
            new UserLogin(
                LocalDateTime.of(2026, 11, 15, 18, 37, 4),
                "Edge · Windows 11",
                "113.161.72.5",
                "Thất bại"
            )
        );
    }

    public List<FriendEntry> getFriends(UserRow selectedUser) {
        return users.stream()
            .filter(user -> user.getStatus() != Status.DELETED)
            .filter(user -> user.getId() != selectedUser.getId())
            .limit(5)
            .map(user -> new FriendEntry(
                user,
                LocalDate.of(2024, 10, 13)
                    .plusDays(user.getId()),
                "Bạn bè"
            ))
            .toList();
    }

    private void seedUsers() {
        users.addAll(
            createSeed(
                1, "truongduyphu", "Trương Duy Phú",
                "Thủ Đức, TP.HCM", LocalDate.of(2002, 8, 14),
                "Nam", "phu.truong@gmail.com",
                LocalDateTime.of(2024, 10, 12, 8, 45),
                Status.ACTIVE
            ),
            createSeed(
                2, "tuanvan", "Tuấn Văn",
                "Quận 7, TP.HCM", LocalDate.of(2002, 3, 21),
                "Nam", "tuan.van@gmail.com",
                LocalDateTime.of(2024, 10, 8, 19, 20),
                Status.ACTIVE
            ),
            createSeed(
                3, "duyhieu", "Duy Hiếu",
                "Bình Thạnh, TP.HCM", LocalDate.of(2001, 11, 9),
                "Nam", "hieu.duy@gmail.com",
                LocalDateTime.of(2024, 10, 2, 12, 10),
                Status.LOCKED
            ),
            createSeed(
                4, "tuananh", "Tuấn Anh",
                "Quận 3, TP.HCM", LocalDate.of(2003, 5, 26),
                "Nam", "anh.tuan@gmail.com",
                LocalDateTime.of(2024, 9, 29, 9, 30),
                Status.ACTIVE
            ),
            createSeed(
                5, "huuhiep", "Hữu Hiệp",
                "Gò Vấp, TP.HCM", LocalDate.of(2002, 12, 18),
                "Nam", "hiep.huu@gmail.com",
                LocalDateTime.of(2024, 9, 22, 14, 15),
                Status.ACTIVE
            ),
            createSeed(
                6, "dophuc", "Đỗ Phúc",
                "Tân Bình, TP.HCM", LocalDate.of(2001, 7, 7),
                "Nam", "phuc.do@gmail.com",
                LocalDateTime.of(2024, 9, 17, 10, 0),
                Status.ACTIVE
            ),
            createSeed(
                7, "lewlew", "Lew.Lew",
                "Quận 1, TP.HCM", LocalDate.of(2002, 1, 30),
                "Khác", "lew@gmail.com",
                LocalDateTime.of(2024, 9, 10, 15, 35),
                Status.LOCKED
            )
        );

        users.stream()
            .filter(user -> user.getStatus() == Status.LOCKED)
            .forEach(user -> {
                user.setLockedUntil(LocalDateTime.now().plusDays(7));
                user.setLockReason("Vi phạm quy định cộng đồng");
            });
    }

    private UserRow createSeed(
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
        return new UserRow(
            id,
            username,
            fullName,
            address,
            birthDate,
            gender,
            email,
            createdAt,
            status
        );
    }
}
