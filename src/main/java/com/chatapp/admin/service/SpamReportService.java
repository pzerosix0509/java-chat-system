package com.chatapp.admin.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.chatapp.admin.dto.SpamReportRow;
import com.chatapp.admin.dto.SpamReportRow.ConversationContext;
import com.chatapp.admin.dto.SpamReportRow.Evidence;
import com.chatapp.admin.dto.SpamReportRow.Status;
import com.chatapp.admin.dto.UserRow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class SpamReportService {

    private static final SpamReportService INSTANCE =
        new SpamReportService();

    private final ObservableList<SpamReportRow> reports =
        FXCollections.observableArrayList();

    private SpamReportService() {
        seedReports();
    }

    public static SpamReportService getInstance() {
        return INSTANCE;
    }

    public ObservableList<SpamReportRow> getReports() {
        return FXCollections.unmodifiableObservableList(reports);
    }

    public Optional<SpamReportRow> findById(long id) {
        return reports.stream()
            .filter(report -> report.getId() == id)
            .findFirst();
    }

    public void recordDecision(
        SpamReportRow report,
        boolean lockAccount,
        String duration,
        String adminReason,
        String reporterMessage
    ) {
        report.setDecision(
            lockAccount ? "Khóa tài khoản" : "Không khóa",
            lockAccount ? duration : null,
            adminReason,
            reporterMessage
        );
    }

    private void seedReports() {
        List<UserRow> users = UserManagementService.getInstance().getUsers();
        reports.addAll(
            createReport(
                1, LocalDateTime.of(2026, 11, 17, 9, 18),
                users.get(3), users.get(2),
                "Gửi tin nhắn quảng cáo lặp lại",
                "Tài khoản này liên tục gửi cùng một nội dung quảng cáo và đường dẫn lạ vào cuộc trò chuyện, kể cả sau khi tôi đã yêu cầu dừng lại.",
                LocalDateTime.of(2026, 11, 17, 8, 54),
                Status.PENDING
            ),
            createReport(
                2, LocalDateTime.of(2026, 11, 17, 8, 42),
                users.get(0), users.get(6),
                "Nội dung không phù hợp",
                "Người dùng gửi nội dung không phù hợp trong cuộc trò chuyện riêng.",
                LocalDateTime.of(2026, 11, 17, 8, 20),
                Status.REVIEWING
            ),
            createReport(
                3, LocalDateTime.of(2026, 11, 16, 22, 5),
                users.get(4), users.get(2),
                "Làm phiền trong nhóm chat",
                "Người dùng gửi nhiều tin nhắn liên tiếp vào nhóm sau khi được yêu cầu dừng lại.",
                LocalDateTime.of(2026, 11, 16, 21, 40),
                Status.PENDING
            ),
            createReport(
                4, LocalDateTime.of(2026, 11, 16, 18, 37),
                users.get(1), users.get(6),
                "Gửi liên kết đáng ngờ",
                "Liên kết được gửi trong cuộc trò chuyện có dấu hiệu dẫn đến trang giả mạo.",
                LocalDateTime.of(2026, 11, 16, 18, 10),
                Status.RESOLVED
            ),
            createReport(
                5, LocalDateTime.of(2026, 11, 15, 14, 21),
                users.get(5), users.get(2),
                "Spam nhiều cuộc trò chuyện",
                "Cùng một nội dung được gửi lặp lại cho nhiều người dùng trong thời gian ngắn.",
                LocalDateTime.of(2026, 11, 15, 13, 55),
                Status.PENDING
            )
        );
    }

    private SpamReportRow createReport(
        long id,
        LocalDateTime createdAt,
        UserRow reporter,
        UserRow accused,
        String reason,
        String content,
        LocalDateTime conversationTime,
        Status status
    ) {
        return new SpamReportRow(
            id,
            createdAt,
            reporter,
            accused,
            reason,
            content,
            new ConversationContext(conversationTime, "Cuộc trò chuyện trực tiếp"),
            List.of(
                new Evidence(
                    "Ảnh chụp màn hình hội thoại",
                    "Nguồn: gg",
                    "https://png.pngtree.com/png-clipart/20190904/original/pngtree-cartoon-info-icon-download-png-image_4476204.jpg",
                    false
                ),
                new Evidence(
                    "Trích đoạn 12 tin nhắn",
                    "TXT · 8 KB · Nội dung minh họa",
                    "",
                    true
                )
            ),
            status
        );
    }
}
