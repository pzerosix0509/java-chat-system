# Thiết kế hệ thống — placeholder

> Chưa điền nội dung. Dùng file này để mô tả kiến trúc 3 lớp + server.

## Sơ đồ dự kiến

```
[ JavaFX Admin ]      [ JavaFX User ]           [ Chat Server ]
   AdminApp  ──┐          UserApp ──┐              ServerMain
   controller ├── service ─┤        ├── service ──┤  ChatServer
   dto/component        repository  repository     │  ClientHandler
                            │          │            │  MessageRouter
                            └────┬─────┘            │  PresenceService
                                 ▼                  │  OfflineMessageStore
                           MySQL (chat_app_db) ◄────┘
```

## Luồng chính cần mô tả khi implement

1. Đăng nhập → tạo `user_sessions`, ghi `login_history`, đăng ký heartbeat.
2. Gửi tin DIRECT → server route → lưu `messages` → đẩy realtime hoặc xếp offline.
3. Gửi tin GROUP → client mã hóa bằng khóa nhóm → server lưu ciphertext trong `messages`.
4. Quản trị truy vấn view thống kê → render biểu đồ bằng `ChartPane`.

## Quy ước đặt tên

- Package gốc: `com.chatapp` → `common`, `server`, `admin`, `user`.
- Mỗi màn hình: `XxxController` ↔ `xxx.fxml`.
- DAO: `XxxDao`; Service: `XxxService`.
