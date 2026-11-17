# Thiết kế cơ sở dữ liệu — placeholder

> Chi tiết thiết kế đã có tại `../../database/sql/README.md`.
> Script tạo bảng: `../../database/sql/chat_app_schema.sql`.

## Nhóm bảng (tóm tắt)

| Nhóm | Bảng |
|---|---|
| Tài khoản & phân quyền | `users`, `roles`, `user_roles`, `password_reset_tokens` |
| Đăng nhập & phiên | `login_history`, `user_sessions` |
| Bạn bè & chặn | `friend_requests`, `friendships`, `user_blocks` |
| Hội thoại | `conversations`, `direct_conversations`, `chat_groups`, `conversation_members` |
| Tin nhắn | `messages`, `message_hidden_for`, `conversation_clear_markers` |
| Mã hóa nhóm | `conversation_key_envelopes` |
| Báo cáo & log | `spam_reports`, `llm_usage_logs` |

## Công việc còn lại khi implement

- [ ] Export ERD (PNG/SVG) vào `erd/`.
- [ ] Đối chiếu tên entity Java với tên cột trong script.
- [ ] Tạo view thống kê cho 4 màn hình báo cáo của admin.
