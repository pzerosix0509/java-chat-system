# script/ — Script tạo cơ sở dữ liệu

Thư mục nộp bài theo yêu cầu (Phiên bản 1 & 2). Các file `.sql` hiện là **placeholder**.

Nguồn thiết kế CSDL đã có sẵn tại:
- `../database/sql/chat_app_schema.sql` — script đầy đủ (MySQL 8, utf8mb4, InnoDB).
- `../database/sql/README.md` — thuyết minh thiết kế, ràng buộc nghiệp vụ, các query mẫu.

## Cách dùng khi đã implement

1. Chạy `01_create_database.sql` → tạo `chat_app_db`.
2. Chạy `02_create_tables.sql` → tạo bảng.
3. Chạy `03_add_constraints.sql` → khóa ngoại / index bổ sung (nếu tách riêng).
4. `04_sample_data.sql` → dữ liệu mẫu (phục vụ demo/chụp ảnh).
5. `05_views_and_reports.sql` → view phục vụ các màn hình thống kê của admin.
6. `06_utility_queries.sql` → truy vấn tiện ích.

> Có thể gộp lại thành một file duy nhất khi nộp, miễn vẫn nằm trong `script/`.

## Mapping bảng → chức năng

| Nhóm bảng | Phục vụ |
|---|---|
| `users`, `roles`, `user_roles`, `password_reset_tokens` | Đăng ký, đăng nhập, quản lý tài khoản, khởi tạo lại mật khẩu |
| `login_history`, `user_sessions` | Lịch sử đăng nhập, trạng thái online/offline, người dùng hoạt động |
| `friendships`, `friend_requests`, `user_blocks` | Bạn bè, yêu cầu kết bạn, block, số lượng bạn bè ( bạn của bạn ) |
| `conversations`, `direct_conversations`, `chat_groups`, `conversation_members` | Chat 1-1, nhóm chat, thành viên & admin nhóm |
| `messages`, `message_hidden_for`, `conversation_clear_markers` | Lịch sử chat, xóa 1/vài dòng, xóa toàn bộ lịch sử, tìm kiếm |
| `conversation_key_envelopes` | Mã hóa nhóm (Facebook-style), xoay khóa khi thêm/xóa thành viên |
| `spam_reports` | Báo cáo spam + khóa tài khoản từ admin |
| `llm_usage_logs` | Log lượt gọi LLM gợi ý/chỉnh sửa tin nhắn |
