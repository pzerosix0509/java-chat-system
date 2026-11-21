package com.chatapp.admin.navigation;

public enum AdminRoute {
    DASHBOARD(
        "/fxml/admin/admin_dashboard.fxml",
        "Dashboard"
    ),
    USERS(
        "/fxml/admin/user_management.fxml",
        "Quản lý người dùng"
    ),
    USER_DETAIL(
        "/fxml/admin/user_detail.fxml",
        "Chi tiết người dùng"
    ),
    LOGIN_HISTORY(
        "/fxml/admin/login_history.fxml",
        "Lịch sử đăng nhập"
    ),
    GROUPS(
        "/fxml/admin/group_list.fxml",
        "Nhóm chat"
    ),
    GROUP_DETAIL(
        "/fxml/admin/group_detail.fxml",
        "Chi tiết nhóm"
    ),
    SPAM_REPORTS(
        "/fxml/admin/spam_reports.fxml",
        "Báo cáo spam"
    ),
    SPAM_REPORT_DETAIL(
        "/fxml/admin/spam_report_detail.fxml",
        "Chi tiết báo cáo"
    ),
    NEW_USERS(
        "/fxml/admin/new_users.fxml",
        "Người dùng đăng ký mới"
    ),
    FRIEND_COUNTS(
        "/fxml/admin/friend_counts.fxml",
        "Quan hệ bạn bè"
    ),
    ACTIVE_USERS(
        "/fxml/admin/active_users.fxml",
        "Người dùng hoạt động"
    );

    private final String fxmlPath;
    private final String title;

    AdminRoute(String fxmlPath, String title) {
        this.fxmlPath = fxmlPath;
        this.title = title;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }

    public String getTitle() {
        return title;
    }
}
