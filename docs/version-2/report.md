# Báo cáo Phiên bản 2 (80%) — Toàn bộ hệ thống

> Template — chưa điền nội dung.

## 1. Thông tin nhóm

| Mã SV | Họ tên | Email | Điện thoại |
|---|---|---|---|
| | | | |

## 2. Danh sách công việc từng thành viên

| Thành viên | Công việc |
|---|---|
| | |

## 3. Bảng đóng góp (tổng = 100)

| Thành viên | Phần trăm |
|---|---|
| | |
| **Tổng** | **100** |

## 4. Chức năng / kỹ thuật nổi bật, làm thêm ngoài yêu cầu

| Tính năng | Mô tả | Người phụ trách |
|---|---|---|
| Mã hóa nhóm kiểu Facebook | AES-256-GCM, xoay khóa khi đổi thành viên | |
| Chat realtime + offline queue | Socket TCP, heartbeat, lưu tin nhắn offline | |
| LLM gợi ý/chỉnh sửa tin nhắn | Gọi API, log token | |
| Tìm kiếm trong lịch sử chat | Tìm 1-1 + toàn bộ, nhảy tới đoạn chat | |

## 5. Thiết kế hệ thống

- Sơ đồ kiến trúc: `docs/design/architecture.md` + `docs/design/erd/`.
- Mô tả luồng: đăng nhập → kết nối server → phân tin nhắn DIRECT/GROUP.

## 6. Thiết kế cơ sở dữ liệu

- Xem `database/sql/README.md`.

## 7. Hướng dẫn sử dụng (có hình)

- Ảnh: `docs/version-2/user-guide/images/`.
- Các bước: import script → sửa `release/config.properties` → chạy server → chạy `admin.jar` / `user.jar`.

## 8. Link demo & repo

- YouTube demo: `<chưa cập nhật>`
- GitHub: `<chưa cập nhật>`
