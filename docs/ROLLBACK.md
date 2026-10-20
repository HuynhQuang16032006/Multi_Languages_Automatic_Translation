# Chiến lược Rollback

Tài liệu này mô tả cách quay lui khi bản deploy mới gặp sự cố. Nguyên tắc: **sao lưu trước khi deploy**, rollback phải nhanh và có thể lặp lại.

## 1. Rollback phiên bản ứng dụng

Ứng dụng là 1 file JAR / 1 Docker image, không có state, nên rollback chỉ là đưa lại phiên bản cũ.

### Cách A — Deploy lại image tag cũ (khuyến nghị)
Mỗi lần build, image được gắn tag theo commit. Rollback = chạy lại tag trước đó.

```bash
# Xem các tag đang có
docker image ls ghcr.io/<owner>/multi_languages_automatic_translation

# Chạy lại đúng phiên bản trước khi lỗi
docker compose down
docker compose up -d --no-build   # sau khi đã sửa service app dùng image tag cũ
```

Nếu deploy bằng image trực tiếp:
```bash
docker pull ghcr.io/<owner>/multi_languages_automatic_translation:<commit-cu>
docker stop audiotour-app && docker rm audiotour-app
docker run -d --name audiotour-app -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=... -e JWT_SECRET=... \
  ghcr.io/<owner>/multi_languages_automatic_translation:<commit-cu>
```

### Cách B — `git revert`
Dùng khi lỗi nằm trong source và cần build lại:

```bash
git log --oneline -10          # tìm commit gây lỗi
git revert <commit_loi>        # tạo commit đảo ngược, giữ lịch sử
git push origin main
# CI sẽ build và push lại image mới
```

Sau khi có bản mới: kiểm tra `GET /actuator/health` trả `UP` và đăng nhập `/api/auth/login` thành công.

## 2. Rollback khi migration Flyway sai

Flyway **không tự rollback** — nếu migration đã chạy thì phải phục hồi từ backup.

### Bắt buộc trước mỗi lần deploy có migration
```bash
mysqldump -u root -p --single-transaction --routines --triggers \
  audiotour_db > backup_audiotour_$(date +%Y%m%d_%H%M%S).sql
```

### Phục hồi
```bash
# 1. Dừng app để không có ghi mới
docker compose stop app

# 2. Phục hồi dữ liệu
mysql -u root -p audiotour_db < backup_audiotour_20260101_120000.sql

# 3. Khởi động lại app
docker compose start app
```

### Sửa lịch sử Flyway (`flyway_schema_history`)
Sau khi restore, bảng `flyway_schema_history` **có thể lệch** với file migration trong code (ví dụ DB đã có version 2 nhưng file V2 sai và đã bị sửa).

- Nếu migration lỗi (failed) và cần chạy lại:
  ```sql
  DELETE FROM flyway_schema_history WHERE success = 0;
  ```
- Nếu muốn để DB coi như đã ở version mong muốn mà không chạy lại migration:
  ```sql
  DELETE FROM flyway_schema_history WHERE version = '2';
  ```
  sau đó deploy lại để Flyway chạy V2 đã sửa.
- Chỉ sửa tay bảng này khi đã hiểu rõ trạng thái DB. Nên backup lại lần nữa trước khi sửa.

Kiểm tra trạng thái:
```sql
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history ORDER BY installed_rank;
```

## 3. Checklist nhanh
- [ ] Backup `audiotour_db` trước khi deploy.
- [ ] Deploy bản mới, chạy smoke test: `/actuator/health`, `/api/auth/login`, `GET /api/v1/locations` kèm Bearer token.
- [ ] Nếu lỗi: rollback image tag cũ trước (nhanh nhất).
- [ ] Nếu migration sai: restore backup rồi xử lý `flyway_schema_history`.
- [ ] Ghi lại nguyên nhân và thời điểm rollback.
