# Java Chat System — Scaffold

> **Trạng thái:** chỉ là cấu trúc thư mục / khung tập tin. Chưa có implementation.
> Mọi file `.java`, `.fxml`, `.sql`, `.css` đang để trống làm placeholder.

Đồ án cuối kỳ môn Lập trình ứng dụng Java. Gồm 2 phân hệ dùng chung một cơ sở dữ liệu MySQL:

- **Admin** — quản lý người dùng, lịch sử đăng nhập, nhóm chat, spam, thống kê & biểu đồ.
- **User** — đăng ký/đăng nhập, bạn bè, chat 1-1, nhóm chat mã hóa, LLM gợi ý tin nhắn.

Stack dự kiến: **JavaFX + Maven + MySQL 8 + Socket (TCP) + Jackson + Logback**.

---

## 1. Cấu trúc thư mục

```
java-chat-system/
├── src/
│   ├── main/
│   │   ├── java/com/chatapp/
│   │   │   ├── common/            # Dùng chung cho cả 2 phân hệ + server
│   │   │   │   ├── config/        # Đọc file cấu hình DB/server/LLM/email
│   │   │   │   ├── constant/      # Enum, hằng số
│   │   │   │   ├── model/         # Entity ánh xạ bảng CSDL
│   │   │   │   ├── dto/           # PageRequest, PageResult, ApiResponse
│   │   │   │   ├── util/          # DbUtil, PasswordUtil, EmailUtil, DateUtil...
│   │   │   │   ├── exception/     # AppException và các lớp con
│   │   │   │   ├── network/       # Protocol, Packet, ChatClient, listener, heartbeat
│   │   │   │   ├── security/      # Hash mật khẩu, AES/RSA, mã hóa nhóm, quản lý khóa
│   │   │   │   └── llm/           # Client/gợi ý LLM
│   │   │   ├── server/            # Chat server: router, presence, offline queue
│   │   │   ├── admin/             # Phân hệ quản trị (entry: AdminApp.java -> admin.jar)
│   │   │   │   ├── controller/    # 1 controller / màn hình
│   │   │   │   ├── dto/           # DTO cho bảng thống kê
│   │   │   │   ├── service/
│   │   │   │   ├── repository/    # DAO
│   │   │   │   └── component/     # ChartPane, FilterBar, SortableTableView
│   │   │   └── user/              # Phân hệ người dùng (entry: UserApp.java -> user.jar)
│   │   │       ├── controller/
│   │   │       ├── model/         # View model cho danh sách/chat
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── component/     # ChatBubble, ContactListItem, AvatarView...
│   │   │       └── util/          # Navigator, popup
│   │   └── resources/
│   │       ├── fxml/admin/        # 16 màn hình admin
│   │       ├── fxml/user/         # 18 màn hình user
│   │       ├── css/
│   │       ├── i18n/
│   │       ├── images/{avatars,icons}/
│   │       ├── config.properties  # cấu hình mặc định đóng gói trong jar
│   │       └── logback.xml
│   └── test/{java,resources}/
├── database/                      # Thiết kế CSDL + script gốc (đã có sẵn)
│   └── sql/chat_app_schema.sql
├── script/                        # Theo yêu cầu nộp: script tạo CSDL
├── release/                       # Theo yêu cầu nộp: 2 jar + 1 file cấu hình
├── git/                           # Theo yêu cầu nộp: ảnh commit của từng thành viên
├── docs/                          # Báo cáo, thiết kế, screenshot, hướng dẫn
├── pom.xml
└── .gitignore
```

---

## 2. Ánh xạ màn hình → file

### Phân hệ Admin (`fxml/admin` + `admin/controller`)

| Chức năng | Controller | FXML |
|---|---|---|
| Đăng nhập admin | `AdminLoginController` | `admin_login.fxml` |
| Trang tổng hợp | `AdminDashboardController` | `admin_dashboard.fxml` |
| 1. Quản lý người dùng (lọc/sắp xếp, thêm/sửa/xóa, khóa/mở, đổi mk, lịch sử đăng nhập, bạn bè) | `UserManagementController`, `UserFormController`, `UserLoginHistoryController`, `UserFriendsController` | `user_management.fxml`, `user_form.fxml`, `user_login_history.fxml`, `user_friends.fxml` |
| 2. Lịch sử đăng nhập | `LoginHistoryController` | `login_history.fxml` |
| 3. Danh sách nhóm chat (thành viên/admin) | `GroupListController`, `GroupMembersController`, `GroupAdminsController` | `group_list.fxml`, `group_members.fxml`, `group_admins.fxml` |
| 4. Báo cáo spam | `SpamReportsController` | `spam_reports.fxml` |
| 5. Người dùng đăng ký mới theo khoảng thời gian | `NewUsersController` | `new_users.fxml` |
| 6. Biểu đồ đăng ký mới theo năm | `RegistrationChartController` | `registration_chart.fxml` |
| 7. Số lượng bạn bè (trực tiếp + bạn của bạn) | `FriendCountsController` | `friend_counts.fxml` |
| 8. Người dùng hoạt động theo khoảng thời gian | `ActiveUsersController` | `active_users.fxml` |
| 9. Biểu đồ người hoạt động theo năm | `ActivityChartController` | `activity_chart.fxml` |

### Phân hệ User (`fxml/user` + `user/controller`)

| Chức năng | Controller | FXML |
|---|---|---|
| 1. Đăng ký | `RegisterController` | `register.fxml` |
| 2. Cập nhật thông tin | `ProfileController` | `profile.fxml` |
| 3. Khởi tạo lại mật khẩu (gửi email) | `ForgotPasswordController` | `forgot_password.fxml` |
| 4. Đổi mật khẩu | `ChangePasswordController` | `change_password.fxml` |
| 5. Đăng nhập | `LoginController` | `login.fxml` |
| 6. Danh sách bạn bè (online/offline, kết bạn, hủy, block) | `FriendsController` | `friends.fxml` |
| 7. Yêu cầu kết bạn | `FriendRequestsController` | `friend_requests.fxml` |
| 8. Bạn bè online → chat/tạo nhóm | `OnlineFriendsController` | `online_friends.fxml` |
| 9. Tìm người (username/tên, ẩn người đã block mình) | `SearchUsersController` | `search_users.fxml` |
| 10. Báo cáo spam | `SpamReportController` | `spam_report.fxml` |
| 11. Chat (realtime/offline/history/xóa/tìm kiếm/LLM) | `ChatController`, `ChatSearchController`, `LlmAssistController` | `chat.fxml`, `chat_search.fxml`, `llm_assist.fxml` |
| 12. Nhóm chat (tạo/đổi tên/thêm/gán admin/xóa/chat/mã hóa) | `GroupListController`, `CreateGroupController`, `GroupChatController`, `GroupSettingsController` | `group_list.fxml`, `create_group.fxml`, `group_chat.fxml`, `group_settings.fxml` |

---

## 3. Quy ước nộp bài (theo đề)

- **Phiên bản 1 (20%)** — giao diện + CSDL: `src`, `script`, `git`, tập tin báo cáo.
- **Phiên bản 2 (80%)** — toàn bộ hệ thống: `src`, `release` (≥2 jar + 1 file cấu hình), `script`, `git`, báo cáo + báo cáo tự đánh giá.

Xem chi tiết nội dung báo cáo tại [`docs/README.md`](docs/README.md).

---

## 4. Ghi chú

- Cơ sở dữ liệu thiết kế sẵn nằm ở `database/sql/chat_app_schema.sql` và `database/sql/README.md`.
- `script/` là thư mục nộp bài; các file SQL ở đây là placeholder, có thể tham chiếu/copy từ `database/sql/`.
- `release/config.properties` là file cấu hình ngoài: chỉ cần sửa thông tin kết nối là chạy được jar.
- Không commit file build (`target/`), jar thật, hay ảnh chụp màn hình cá nhân vào `src/`.
