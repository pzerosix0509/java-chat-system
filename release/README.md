# release/ — Tập tin phát hành (Phiên bản 2)

Yêu cầu: **ít nhất 2 file jar + 1 file cấu hình**.

```
release/
├── admin.jar            # entry: com.chatapp.admin.AdminApp
├── user.jar             # entry: com.chatapp.user.UserApp
├── config.properties    # cấu hình DB/server/email/LLM — sửa là chạy được
└── .gitkeep
```

## Hướng dẫn build (khi đã implement)

```bash
mvn clean package
# copy target/*.jar vào đây và đổi tên thành admin.jar / user.jar,
# hoặc dùng maven-shade-plugin với 2 execution có Main-Class riêng.
```

## Hướng dẫn chạy

```bash
# 1. Import script/ (hoặc database/sql/chat_app_schema.sql) vào MySQL 8
# 2. Sửa release/config.properties
# 3. Chạy:
java -jar admin.jar
java -jar user.jar
```

> Chat realtime cần chạy thêm server: `java -cp user.jar com.chatapp.server.ServerMain`
> (sẽ tách thành `server.jar` nếu nhóm muốn).

File jar thật bị ignore bởi `.gitignore` cho đến khi sẵn sàng nộp.
