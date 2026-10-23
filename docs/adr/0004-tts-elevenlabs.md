# ADR 0004: Sinh audio thuyết minh bằng ElevenLabs TTS, lưu file để tái sử dụng

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Accepted |
| Ngày | 2026-10-24 |
| Nhánh | `main` |

## Bối cảnh

Đề tài mang tên "Multi Languages Automatic Translation" nhưng chưa có cơ chế sinh audio: file MP3 được copy tay vào `uploads/audio`, `DataInitializer` seed `audioUrl` trỏ tới tên file. Hệ quả là 6/6 POI có bản `vi` nhưng chỉ 1/6 POI có bản `en`; muốn phủ đủ phải ghi âm tay 5 bản nữa.

Ba ràng buộc của môi trường:

- Không có API CRUD cho Narration (backlog B-01), nên không có chỗ nào upload file qua HTTP.
- Mọi thứ phía sau đều đọc file từ đĩa: `AudioPackageService` stream MP3 và nén ZIP offline; thiếu file là lỗi 400 theo `FR-PKG-03`.
- Đồ án phải chạy được trên máy sinh viên, CI không có mạng và không có API key.

Phạm vi đã chốt với chủ đồ án: làm TTS trước, dịch máy (MT) để sau.

## Quyết định

1. **TTS sinh ra file MP3 thật trong `uploads/audio`**, không stream trực tiếp cho client. Nhờ vậy stream, ZIP offline, `audioUrl`, `durationInSeconds` giữ nguyên hợp đồng.
2. **Kích hoạt bằng `POST /api/narrations/{id}/audio`** (cần token admin theo rule non-GET `/api/**` sẵn có). Không tự sinh lúc khởi động, không fallback ngầm trong lúc stream: sinh audio là hành động tốn tiền, phải do người vận hành gọi.
3. **File đã có là cache.** Gọi lại mặc định trả về file cũ, không gọi API, không tốn credit. `force=true` mới sinh lại.
4. **Ghi file tạm rồi move**, chỉ cập nhật DB sau khi file tồn tại — không bao giờ để lại MP3 dở dang cho endpoint stream/ZIP đọc.
5. **Giữ tên file cũ nếu `audioUrl` đã có**; nếu rỗng thì `<locationId>-<lang>.mp3`. Lấy `getFileName()` để chặn đường dẫn kiểu `../..`.
6. **`durationInSeconds` ước lượng bằng `textContent.length() / 15`** thay vì thêm thư viện đọc metadata MP3.
7. **ElevenLabs + `eleven_flash_v2_5`**, **một voice ID dùng chung cho mọi ngôn ngữ** (voice ElevenLabs không gắn ngôn ngữ; đổi giọng chỉ cần sửa `app.tts.voice-id`). Free tier 10.000 ký tự/tháng, có tiếng Việt, xác thực chỉ bằng một API key.
8. **Không thêm dependency Maven**: dùng `RestClient` của Spring, key đọc từ env, thiếu key thì app vẫn boot và endpoint trả 503.

## Các lựa chọn đã cân nhắc và lý do loại

| Lựa chọn | Lý do loại |
| --- | --- |
| J2Team Text to Speech | App desktop đóng gói, không có API; chỉ dùng được bằng tay hoặc scrape web tool giới hạn 200 ký tự/lần. |
| Endpoint Google Translate TTS không chính thức | Trái ToS, giới hạn 200 ký tự/request, có thể chết giữa buổi demo. |
| Azure AI Speech F0 | 500.000 ký tự/tháng miễn phí (gấp 40 lần) nhưng yêu cầu thẻ để đăng ký. |
| `edge-tts` self-host | Miễn phí nhưng phải thêm service Python vào Docker và vẫn là endpoint không chính thức. |
| Sinh audio on-the-fly khi du khách bấm nghe | Request đầu chậm 2–5 giây, phá luật "thiếu MP3 là lỗi" của `FR-PKG-03`, và không dùng được cho gói offline. |
| TTS phía client (Android TextToSpeech) | Backend không còn gì để chấm, gói ZIP offline mất MP3. |
| `eleven_multilingual_v2` | Chất lượng cao nhất cho audio dài nhưng **không hỗ trợ tiếng Việt** (29 ngôn ngữ, không có `vi`). |

## Hệ quả

Tích cực:

- Không sửa `AudioPackageService`, endpoint stream, ZIP offline, schema DB hay `SecurityConfig`.
- Một lệnh `curl` là có file; chạy lại không tốn thêm credit.
- Đổi nhà cung cấp/giọng đọc chỉ cần sửa `application.yml`, không sửa code.

Tiêu cực:

- Phụ thuộc mạng và quota 10.000 ký tự/tháng của free tier — đủ phủ toàn bộ seed (~5.000 ký tự) một lần, không đủ để sinh lại nhiều lần.
- Free tier **chặn voice thư viện qua API** (lỗi 402 `paid_plan_required`), kể cả voice mặc định như Rachel; phải dùng voice do tài khoản tự tạo bằng Voice Design (3 slot miễn phí) hoặc nạp tối thiểu $5.
- Free tier không có giấy phép thương mại và bắt buộc ghi công `elevenlabs.io` khi công bố audio.
- `durationInSeconds` là ước lượng, sai số khoảng ±15%.
- Chất lượng giọng phụ thuộc `voice-id`; dùng chung một voice cho cả `vi`/`en`/`zh` nên ngôn ngữ khác tiếng mẹ đẻ của voice sẽ bị accent (chấp nhận được ở mức đồ án, không dùng cho sản phẩm thương mại).

## Khi nào xem lại quyết định

- Có thẻ/ngân sách: chuyển sang Azure F0 hoặc ElevenLabs Starter (có giấy phép thương mại).
- Cần audio cho POI chưa có văn bản: phải làm bước dịch máy trước (backlog B-03).
- Cần `durationInSeconds` chính xác: đọc metadata MP3 hoặc dùng batch API trả duration.

## Tham chiếu

- `GLOSSARY.md`: TTS, Generated Audio.
- `src/main/java/vn/edu/sgu/audiotour/service/TtsService.java`
- `src/main/java/vn/edu/sgu/audiotour/config/TtsProperties.java`
- `src/main/java/vn/edu/sgu/audiotour/controller/NarrationController.java`
- `src/main/resources/application.yml` (`app.tts`)
