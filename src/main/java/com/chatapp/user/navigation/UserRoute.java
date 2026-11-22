package com.chatapp.user.navigation;

public enum UserRoute {
    LOGIN(
        "/fxml/user/login.fxml",
        "MyBrand - Đăng nhập"
    ),
    REGISTER(
        "/fxml/user/register.fxml",
        "MyBrand - Đăng ký"
    ),
    FORGOT_PASSWORD(
        "/fxml/user/forgot_password.fxml",
        "MyBrand - Quên mật khẩu"
    ),
    FORGOT_SUCCESS(
        "/fxml/user/forgot_password_success.fxml",
        "MyBrand - Gửi email thành công"
    ),
    FORGOT_FAIL(
        "/fxml/user/forgot_password_fail.fxml",
        "MyBrand - Gửi email thất bại"
    ),
    MAIN_LAYOUT(
        "/fxml/user/main_layout.fxml",
        "MyBrand"
    ),
    PROFILE(
        "/fxml/user/profile.fxml",
        "MyBrand - Thông tin cá nhân"
    );

    private final String fxmlPath;
    private final String title;

    UserRoute(String fxmlPath, String title) {
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
