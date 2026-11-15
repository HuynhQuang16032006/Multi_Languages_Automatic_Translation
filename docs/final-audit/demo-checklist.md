# Checklist demo Audio Tour

Vòng 2: login, tạo POI/narration, phát audio seed, nhập QR, dashboard và tải ZIP đã kiểm tra một phần trên JAR/DB test; chi tiết và giới hạn tại [round2-runtime-report.md](round2-runtime-report.md). Chưa chứng minh demo đầy đủ hoặc camera/GPS thực địa.

## Chuẩn bị

1. Cài Docker Desktop Linux containers; kiểm tra `docker version`. Vòng 2 daemon hoạt động, Compose hiện hữu healthy.
2. Clone đủ repository và `uploads/audio/`; không ghi đè `.env` hoặc xóa volume đang có. Nếu chưa có `.env`, sao chép `.env.example` và điền các giá trị bắt buộc. Không đưa secrets vào báo cáo.
3. Từ thư mục dự án: `docker compose up --build -d`. Kiểm tra `docker compose ps` và `curl.exe --fail http://localhost:8080/actuator/health`. Nếu chưa healthy, xem `docker compose logs app db`. Không chạy `docker compose down -v`.
4. Mở `http://localhost:8080/next/` và `/next/admin/` trên máy chạy Docker. Compose chỉ bind `127.0.0.1`, điện thoại khác không truy cập được theo cấu hình mặc định; demo qua LAN cần cấu hình mạng và HTTPS phù hợp, không tự phơi dịch vụ.
5. Chuẩn bị tài khoản admin đã cấu hình, địa điểm có narration và MP3 thật cho ngôn ngữ muốn demo. Dịch/TTS cần cấu hình nhà cung cấp và chi phí được duyệt; không gọi dịch vụ có phí khi chưa được phép. Không dùng dữ liệu người dùng thật làm fixture.

## Các bước cần trình diễn và ghi kết quả

- Đăng nhập admin; kiểm tra từ chối mật khẩu sai và đăng xuất.
- Tạo địa điểm test, thêm nội dung theo ngôn ngữ; tạo audio bằng TTS nếu có cấu hình và được phép. Xác nhận dữ liệu hiển thị; xóa fixture qua API/UI nếu không còn phụ thuộc.
- Mở trang khám phá, tìm địa điểm, mở chi tiết, đổi ngôn ngữ, phát MP3 thật; quan sát thời gian phát tăng và xử lý lỗi khi thiếu audio.
- Quét QR trên điện thoại hoặc nhập mã trong `/next/qr`; mở địa điểm và phát; xác minh trigger `QR_CODE` trong dữ liệu thống kê. Kiểm tra camera trên HTTPS hoặc localhost.
- Tải ZIP; kiểm tra văn bản và audio thực bên trong. Mở dashboard đối chiếu lượt nghe; không coi request log thủ công là bằng chứng nghe thật.
- Kiểm tra gợi ý geofence tại thiết bị phù hợp; PRD yêu cầu tự phát nhưng kế hoạch chấp nhận gợi ý, cần xác nhận tiêu chí trước khi chấm.

**Trước khi demo:** xác nhận âm thanh khớp văn bản sau mỗi lần sửa narration; kiểm tra camera/GPS trên thiết bị thật; thống nhất yêu cầu GPS tự phát hay gợi ý. TTS thật chưa được kiểm chứng, không dùng fixture gắn audio seed làm bằng chứng TTS.

## Bảo vệ dữ liệu

Sao lưu database và `uploads/audio` cùng nhau trước thay đổi. Không xóa volume, không chỉnh secret hay tài khoản hiện hữu để kiểm thử. Dừng bằng `docker compose down` chỉ khi phù hợp với người đang dùng môi trường. Xem `README.md` và `docs/ROLLBACK.md` để biết cấu hình và phục hồi.
