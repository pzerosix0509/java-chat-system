package com.chatapp.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class SpamReportRow {

    public enum Status {
        PENDING("Chưa xử lý"),
        REVIEWING("Đang xem xét"),
        RESOLVED("Đã xử lý");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public record Evidence(
        String title,
        String description,
        String imageUrl,
        boolean attachment
    ) {
    }

    public record ConversationContext(
        LocalDateTime occurredAt,
        String type
    ) {
    }

    private final long id;
    private final LocalDateTime createdAt;
    private final UserRow reporter;
    private final UserRow accused;
    private final String reasonSummary;
    private final String reportContent;
    private final ConversationContext context;
    private final List<Evidence> evidence;
    private Status status;
    private String decision;
    private String duration;
    private String adminReason;
    private String reporterMessage;

    public SpamReportRow(
        long id,
        LocalDateTime createdAt,
        UserRow reporter,
        UserRow accused,
        String reasonSummary,
        String reportContent,
        ConversationContext context,
        List<Evidence> evidence,
        Status status
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.reporter = reporter;
        this.accused = accused;
        this.reasonSummary = reasonSummary;
        this.reportContent = reportContent;
        this.context = context;
        this.evidence = List.copyOf(evidence);
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getReportCode() {
        return String.format("SP-2026-%04d", id + 30);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserRow getReporter() {
        return reporter;
    }

    public UserRow getAccused() {
        return accused;
    }

    public String getReasonSummary() {
        return reasonSummary;
    }

    public String getReportContent() {
        return reportContent;
    }

    public ConversationContext getContext() {
        return context;
    }

    public List<Evidence> getEvidence() {
        return evidence;
    }

    public Status getStatus() {
        return status;
    }

    public void setDecision(
        String decision,
        String duration,
        String adminReason,
        String reporterMessage
    ) {
        this.decision = decision;
        this.duration = duration;
        this.adminReason = adminReason;
        this.reporterMessage = reporterMessage;
        status = Status.RESOLVED;
    }

    public String getDecision() {
        return decision;
    }

    public String getDuration() {
        return duration;
    }

    public String getAdminReason() {
        return adminReason;
    }

    public String getReporterMessage() {
        return reporterMessage;
    }
}
