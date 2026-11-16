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

1. Dừng ứng dụng và mọi tiến trình ghi. Xác định thời điểm backup và các giao dịch phát sinh sau đó: phục hồi backup có thể làm mất các giao dịch này; cần kế hoạch bảo toàn hoặc khôi phục riêng trước khi tiếp tục.
2. Phục hồi backup vào một database **trống, cách ly**, không ghi đè database đang chạy. Đối chiếu schema, dữ liệu, ràng buộc và `flyway_schema_history` với backup.
3. Trên bản sao đã phục hồi, thử artifact cũ tương thích với schema và lịch sử migration trong backup. Không khởi động artifact mới trước khi kiểm tra: Flyway có thể chạy lại migration.
4. Chỉ chuyển ứng dụng sang bản phục hồi sau khi xác minh và được người có trách nhiệm phê duyệt. Giữ nguyên database cũ để điều tra và đối chiếu.

Không xóa hoặc chỉnh thủ công dòng `flyway_schema_history` để giả lập rollback: thao tác đó không đảo schema hay dữ liệu. Nếu lịch sử và schema không khớp, dừng triển khai và xác định cách phục hồi phù hợp trên bản sao trước.

Kiểm tra lịch sử trên bản sao:
```sql
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history ORDER BY installed_rank;
```

## 3. Checklist nhanh
- [ ] Backup `audiotour_db` trước khi deploy.
- [ ] Deploy bản mới, chạy smoke test: `/actuator/health`, `/api/auth/login`, `GET /api/v1/locations` kèm Bearer token.
- [ ] Nếu lỗi: rollback image tag cũ trước (nhanh nhất).
- [ ] Nếu migration sai: phục hồi vào database trống cách ly, kiểm tra dữ liệu và lịch sử, thử artifact cũ tương thích trước khi chuyển đổi.
- [ ] Ghi lại nguyên nhân và thời điểm rollback.
