# Thiết kế CSDL ứng dụng chat

**Hệ quản trị giả định:** MySQL 8.0.16 trở lên, InnoDB, `utf8mb4`.

## 1. Phạm vi thiết kế

Cả phân hệ quản trị và phân hệ người dùng dùng chung một CSDL. Không tạo hai bộ bảng riêng vì cả hai cùng đọc/ghi người dùng, bạn bè, nhóm và tin nhắn. Script có `roles`/`user_roles` với hai vai trò mặc định USER và ADMIN; quyền cụ thể vẫn phải được kiểm tra ở tầng Java/service.

## 2. Nhóm bảng và chức năng

| Nhóm | Bảng | Mục đích |
|---|---|---|
| Tài khoản | `users` | Hồ sơ, email, mật khẩu đã hash, trạng thái ACTIVE/LOCKED/DELETED |
| Phân quyền hệ thống | `roles`, `user_roles` | Phân quyền USER/ADMIN cho hai phân hệ; tách biệt admin nhóm chat |
| Khôi phục mật khẩu | `password_reset_tokens` | Token dùng một lần, có hạn sử dụng; không lưu token thô |
| Đăng nhập | `login_history`, `user_sessions` | Nhật ký đăng nhập và phiên/heartbeat để suy ra online |
| Quan hệ bạn bè | `friend_requests`, `friendships`, `user_blocks` | Yêu cầu kết bạn, quan hệ bạn bè không phân biệt chiều, chặn người dùng |
| Hội thoại | `conversations`, `direct_conversations`, `chat_groups` | Hội thoại chung; phân biệt chat 1-1 và nhóm |
| Thành viên | `conversation_members` | Thành viên, vai trò MEMBER/ADMIN, thời điểm tham gia/rời nhóm |
| Tin nhắn | `messages` | Tin nhắn thường hoặc ciphertext, có chỉ mục theo hội thoại/thời gian |
| Xóa lịch sử | `message_hidden_for`, `conversation_clear_markers` | Ẩn một/vài tin hoặc xóa lịch sử riêng cho người dùng hiện tại |
| Mã hóa nhóm | `conversation_key_envelopes` | Bản khóa nhóm đã mã hóa riêng cho từng thành viên và từng phiên bản khóa |
| Báo cáo spam | `spam_reports` | Người báo cáo, người bị báo cáo, tin nhắn liên quan, trạng thái xử lý |
| LLM | `llm_usage_logs` | Ghi nhận lượt dùng/tokens, không mặc định lưu nội dung chat riêng tư |

## 3. Quan hệ chính

- `users` 1-N `login_history`, `user_sessions`, `friend_requests`, `spam_reports`; N-N `roles` qua `user_roles`.
- `friendships` nối hai người bằng cặp `user_low_id < user_high_id`; do đó một tình bạn chỉ được lưu một lần.
- `conversations` là bảng cha. Mỗi hội thoại thuộc loại `DIRECT` hoặc `GROUP`.
- `direct_conversations` lưu cặp người dùng duy nhất cho chat 1-1; `chat_groups` lưu tên và cấu hình mã hóa của nhóm.
- `conversation_members` nối người dùng với hội thoại và lưu vai trò/quãng thời gian tham gia.
- `messages` tham chiếu cặp `(conversation_id, sender_id)` trong `conversation_members`, để người gửi phải từng là thành viên hội thoại.
- `conversation_key_envelopes` lưu khóa phiên bản cho từng thành viên; khóa riêng không lưu ở dạng rõ.

## 4. Các quy ước nghiệp vụ quan trọng

1. **Mật khẩu:** Java tạo hash bằng Argon2id hoặc bcrypt. Không dùng MD5/SHA-1 và không lưu mật khẩu rõ.
2. **Khôi phục mật khẩu:** thiết kế dùng token một lần gửi qua email rồi đặt mật khẩu mới. Cách này an toàn hơn gửi mật khẩu ngẫu nhiên qua email. Nếu đề bài bắt buộc gửi mật khẩu tạm, chỉ lưu hash của mật khẩu tạm và buộc đổi ngay lần đăng nhập kế tiếp.
3. **Xóa tài khoản:** dùng soft delete (`status='DELETED'`, `deleted_at`) để không phá vỡ khóa ngoại và lịch sử chat. Khi xóa cần chấm dứt session và xử lý quan hệ bạn bè/yêu cầu kết bạn trong transaction.
4. **Online/offline:** không nên lưu một cột `is_online` cố định. Client heartbeat cập nhật `user_sessions.last_seen_at`; ví dụ coi online nếu có session chưa kết thúc và heartbeat trong 2 phút gần nhất. Ngưỡng này có thể cấu hình.
5. **Bạn bè:** cặp bạn bè được chuẩn hóa theo ID nhỏ/lớn. Khi chấp nhận yêu cầu, insert vào `friendships` và cập nhật `friend_requests` trong cùng transaction.
6. **Chặn:** khi A chặn B, thêm `(A,B)` vào `user_blocks`, xóa tình bạn nếu có và hủy yêu cầu đang chờ giữa hai người. Khi tìm kiếm/kết bạn, phải kiểm tra block ở cả hai chiều.
7. **Xóa tin nhắn/lịch sử:** mặc định hai bảng xóa lịch sử chỉ ẩn ở tài khoản đang thao tác, không xóa bản sao phía người còn lại. Nếu cần “thu hồi cho mọi người”, bổ sung nghiệp vụ riêng trên `messages.deleted_at` và giới hạn quyền/thời gian thu hồi.
8. **Mã hóa nhóm:** việc mã hóa/giải mã thực hiện phía client. Server chỉ lưu ciphertext, nonce, thuật toán, phiên bản khóa và key envelope. Khi thêm/xóa thành viên, phải xoay khóa và phân phối khóa mới cho thành viên còn lại. Người đã nhận khóa cũ vẫn có thể đọc dữ liệu cũ mà họ đã có; không thể thu hồi bí mật đã được tải về thiết bị. Do server không thấy nội dung rõ, không thể FULLTEXT-search tin nhắn E2EE; client phải giải mã và tìm kiếm cục bộ (hoặc chấp nhận thiết kế searchable-encryption phức tạp hơn).
9. **Số bạn của bạn:** view `v_user_friend_counts` tính số bạn trực tiếp và số người ở cấp bạn-của-bạn (distinct, loại chính người đó và bạn trực tiếp). Cột `network_size_including_friends_of_friends` là tổng hai cấp, không đếm trùng.
10. **Thống kê hoạt động:** truy vấn mẫu định nghĩa `app_open_count` là số phiên bắt đầu trong khoảng; `people_chatted_with` là số người chat trực tiếp phân biệt; `groups_chatted_in` là số nhóm có tin nhắn trong khoảng; `activity_count = app_open_count + messages_sent`. Có thể đổi công thức theo định nghĩa KPI của giảng viên.
11. **LLM:** không cần lưu toàn bộ prompt/response để thực hiện chức năng. Nếu cần audit, chỉ lưu metadata/token; nếu lưu nội dung, cần chính sách riêng về quyền riêng tư và thời hạn lưu.

## 5. Cách chạy

1. Mở MySQL Workbench hoặc CLI.
2. Chạy toàn bộ file `chat_app_schema_mysql8.sql`.
3. Kết nối Java bằng JDBC và dùng `PreparedStatement` cho mọi tham số.
4. Các câu SQL ví dụ cho từng chức năng admin/user được đặt ở cuối file dưới dạng comment để tham khảo và chuyển thành repository/DAO.

## 6. Những điểm tầng Java phải kiểm tra

- Tài khoản có `status='ACTIVE'` trước khi đăng nhập, gửi tin hoặc tạo quan hệ; kiểm tra role ADMIN cho API quản trị.
- Chỉ admin nhóm mới được cấp quyền admin/xóa thành viên; nếu quy định môn học yêu cầu, chỉ admin mới được thêm thành viên.
- Người gửi tin phải đang là thành viên còn hiệu lực (`left_at IS NULL`).
- Người nhận lời mời kết bạn không bị block và chưa là bạn.
- Khi đăng ký, gán role USER; chỉ cấp role ADMIN bằng luồng được bảo vệ hoặc bootstrap thủ công. Khi tạo nhóm phải có người tạo + ít nhất một thành viên khác.
- Khi người dùng bị khóa, chấm dứt các session đang hoạt động.
- Khi report spam, xác minh reporter và người bị report có liên quan đến hội thoại/tin nhắn đó.
- Thao tác nhiều bảng (accept friend request, block, tạo nhóm, đổi khóa) phải nằm trong transaction.
