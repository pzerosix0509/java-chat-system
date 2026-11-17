# docs/ — Báo cáo & tài liệu thiết kế

Các file hiện là **template/checklist**, chưa có nội dung.

```
docs/
├── README.md
├── version-1/
│   ├── report.md                  # Báo cáo phiên bản 1 (20%)
│   └── screenshots/{admin,user}/  # Ảnh chụp các màn hình đã thiết kế
├── version-2/
│   ├── report.md                  # Báo cáo phiên bản 2 (80%)
│   ├── self-assessment.md         # Báo cáo tự đánh giá (theo mẫu của giảng viên)
│   ├── screenshots/               # Ảnh chức năng (bắt buộc)
│   └── user-guide/images/         # Ảnh hướng dẫn sử dụng (bắt buộc)
└── design/
    ├── architecture.md            # Thiết kế hệ thống
    ├── database.md                # Thiết kế CSDL (xref database/sql/)
    └── erd/                       # File ảnh ERD / diagram
```

---

## Checklist nội dung báo cáo Phiên bản 1 (20%)

- [ ] Thông tin nhóm
- [ ] Danh sách công việc từng thành viên
- [ ] Bảng đóng góp từng thành viên (**tổng = 100**)
- [ ] Thiết kế cơ sở dữ liệu của hệ thống
- [ ] Danh sách các màn hình đã thiết kế (**có hình chụp** → `docs/version-1/screenshots/`)

## Checklist nội dung báo cáo Phiên bản 2 (80%)

- [ ] Thông tin nhóm
- [ ] Danh sách công việc từng thành viên
- [ ] Bảng đóng góp từng thành viên (**tổng = 100**)
- [ ] Các chức năng/kỹ thuật tự đánh giá là hay, nổi bật, làm thêm ngoài yêu cầu
- [ ] Thiết kế của hệ thống
- [ ] Thiết kế cơ sở dữ liệu của hệ thống
- [ ] Hướng dẫn sử dụng (**có hình** → `docs/version-2/user-guide/images/`)
- [ ] Link YouTube demo chức năng
- [ ] Link GitHub

## Báo cáo tự đánh giá

- [ ] Điền theo đúng **mẫu của giảng viên** (không có mẫu → 0đ).

---

> Khi nộp, đóng gói đúng cấu trúc: `src`, `release` (VB2), `script`, `git`,
> và tập tin báo cáo. Sai cấu trúc → 0đ theo đề.
