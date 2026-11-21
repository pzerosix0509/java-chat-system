package com.chatapp.admin.service;

import java.util.List;
import java.util.Optional;

import com.chatapp.admin.dto.AdminProfile;

public class AdminAuthService {

    private final List<MockAccount> mockAccounts = List.of(
        new MockAccount(
            "admin",
            "Admin@123",
            new AdminProfile(
                1L,
                "admin",
                "Admin",
                "admin@mybrand.vn",
                "Quản trị viên",
                "AD"
            )
        )
    );

    public Optional<AdminProfile> authenticate(
        String username,
        String password
    ) {
        return mockAccounts.stream()
            .filter(account ->
                account.username().equalsIgnoreCase(username)
            )
            .filter(account ->
                account.password().equals(password)
            )
            .map(MockAccount::profile)
            .findFirst();
    }

    private record MockAccount(
        String username,
        String password,
        AdminProfile profile
    ) {
    }
}
