# ADR 0005: Admin tự thêm ngôn ngữ, dịch bằng DeepL rồi sinh audio ngay

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Accepted |
| Ngày | 2026-10-24 |
| Nhánh | `main` |

## Bối cảnh

Tên đề tài là "Multi Languages Automatic Translation" nhưng việc thêm một ngôn ngữ vẫn cần dev: viết tay nội dung cho từng POI, gọi API sinh audio, rồi sửa danh sách hardcode trong `player.html` và `demo.html`. Ba trang đó chưa thống nhất: `index.html` đọc ngôn ngữ từ `/api/languages`, hai trang còn lại hardcode `vi`/`en`.

Dữ liệu hiện tại: 6 POI có narration `vi`, một POI có thêm `en`, một POI có thêm `zh`. Không có API CRUD cho `Narration`, nên nội dung chỉ vào được DB qua seed hoặc SQL tay.

Dashboard chưa có đăng nhập. Nó chỉ gọi hai endpoint công khai. `POST /api/languages` và `POST /api/narrations/{id}/audio` đều nằm sau rule `/api/**` yêu cầu token.

Chủ đồ án chốt: admin bấm nút thêm ngôn ngữ, hệ thống tự dịch và tự sinh audio, không cần dev.

## Quyết định

1. **Admin thêm ngôn ngữ qua dashboard, không mở endpoint ghi cho khách.** Dashboard có form đăng nhập nhỏ gọi `POST /api/auth/login`, giữ token trong `sessionStorage`. Mở `POST /api/languages` cho người lạ là lỗ hổng ghi dữ liệu, không đáng đổi lấy 20 dòng code.
2. **Một request làm trọn việc thêm ngôn ngữ**: dịch 6 narration `vi`, lưu, rồi sinh 6 file MP3. Endpoint `POST /api/languages/{code}/translate`.
3. **DeepL Free làm dịch vụ dịch.** 500.000 ký tự/tháng, không cần thẻ, có `vi` cả hai chiều. Một ngôn ngữ mới tốn khoảng 1.600 ký tự.
4. **Nguồn dịch cố định là `vi`.** Đây là bản đầy đủ nhất và là ngôn ngữ gốc của đề tài. Không dịch chéo từ `en`/`zh`.
5. **Dịch all-or-nothing.** Dịch đủ 6 bản rồi mới ghi `Language` và 6 `Narration` trong một transaction. Lỗi một bản thì không ghi gì, trả 502. Nửa ngôn ngữ có nội dung, nửa không, tệ hơn là không có ngôn ngữ.
6. **Sinh audio sau khi commit, không rollback được.** Gọi TTS từng narration qua `TtsService` hiện có. DeepL lỗi thì chưa ghi gì. TTS lỗi giữa đường thì các file trước đã ghi và đã tốn credit; response trả `audioGenerated` để admin biết còn thiếu bao nhiêu, và gọi lại `POST /api/narrations/{id}/audio` cho phần thiếu.
7. **Sinh audio ngay, không lazy.** Du khách không có token nên không tự kích hoạt được TTS, và endpoint stream công khai không tự sinh file. Để trống audio nghĩa là du khách gặp lỗi khi bấm nghe. Ngôn ngữ vừa thêm thì kiểu gì cũng demo, nên tiết kiệm quota bằng cách bắt admin bấm thêm một nút là tiết kiệm giả.
8. **Danh sách ngôn ngữ hỗ trợ nằm trong cấu hình**, là giao của ngôn ngữ `eleven_flash_v2_5` đọc được và ngôn ngữ DeepL dịch được. Endpoint công khai `GET /api/languages/supported` trả danh sách này cho dashboard. Một nguồn duy nhất, không hardcode hai nơi.
9. **Không thêm dependency Maven.** Dùng `RestClient` của Spring như ADR 0004. `TtsService` giữ nguyên, chỉ gọi thêm.
10. **`player.html` và `demo.html` lấy ngôn ngữ động** từ `GET /api/languages`. Sau thay đổi này, thêm ngôn ngữ chỉ còn là thao tác dữ liệu.

## Các lựa chọn đã cân nhắc và lý do loại

| Lựa chọn | Lý do loại |
| --- | --- |
| Google Cloud Translation | 500.000 ký tự/tháng miễn phí nhưng bắt buộc tài khoản billing có thẻ. |
| Azure Translator F0 | 2 triệu ký tự/tháng, không cần thẻ, nhưng phải tạo resource trên portal và cấu hình nặng hơn. Giữ làm phương án dự phòng nếu DeepL chặn `vi`. |
| LLM (Gemini, GPT) để dịch | Chất lượng tốt nhưng kết quả không ổn định giữa các lần, free tier giới hạn request, và cần thêm một SDK. |
| Dịch tay rồi seed | Không scale, mỗi ngôn ngữ là một lần sửa code, đúng cái đang muốn bỏ. |
| LibreTranslate tự host | Miễn phí và offline nhưng thêm service Python vào Docker, chất lượng `vi` thấp hơn DeepL. |
| Dịch tại thời điểm du khách nghe | Request chậm, tiêu quota ngầm, và trái quyết định ở ADR 0004 là không sinh ngầm trong lúc stream. |
| Chỉ tạo `Language`, để audio cho sau | Du khách thấy ngôn ngữ mới nhưng bấm vào là lỗi. Đúng cái Q8 muốn tránh. |
| Sinh audio bằng job nền (queue) | Đúng cho hệ thống thật, nhưng thêm bảng job, trạng thái, retry cho một thao tác 30 giây của admin. |

## Hệ quả

Tích cực:

- Thêm ngôn ngữ không cần dev, không cần sửa code, không cần khởi động lại app.
- `player.html` và `demo.html` hết hardcode, khớp với `index.html`.
- Dịch và sinh audio dùng lại hạ tầng có sẵn: `Language` CRUD, `TtsService`, quy tắc đặt tên file.

Tiêu cực:

- Phụ thuộc thêm một nhà cung cấp ngoài (DeepL) và một API key nữa.
- DeepL free tier không có DPA, có thể dùng dữ liệu để cải thiện dịch vụ.
- Bản dịch không có người soát. Chưa có CRUD narration nên sửa bản dịch phải sửa DB.
- Request giữ 20 đến 30 giây. Mạng đứt giữa chừng thì bản dịch đã lưu, audio có thể thiếu.
- Quota TTS 10.000 ký tự/tháng chỉ đủ cho khoảng 6 ngôn ngữ mới nếu không sinh lại.
- `vi` của DeepL chỉ chạy trên model next-gen. Phải kiểm tra bằng key thật trước khi triển khai.

## Khi nào xem lại quyết định

- Có CRUD narration: cho admin sửa bản dịch ngay trên dashboard, bỏ việc sửa DB tay.
- Thêm POI mới sau khi đã có nhiều ngôn ngữ: cần endpoint dịch bổ sung cho các narration thiếu.
- Quota TTS hoặc DeepL cạn: chuyển sang Azure F0, hoặc chỉ sinh audio khi admin chọn.
- Cần thêm ngôn ngữ ngoài giao của hai nhà cung cấp: đổi model TTS hoặc thêm nhà cung cấp dịch.

## Tham chiếu

- `docs/specs/feature-them-ngon-ngu.md`
- `docs/adr/0004-tts-elevenlabs.md`
- `GLOSSARY.md`: Language, Narration, Machine Translation, Generated Audio.
- `src/main/java/vn/edu/sgu/audiotour/service/TtsService.java`
- `src/main/java/vn/edu/sgu/audiotour/controller/LanguageController.java`
- `src/main/java/vn/edu/sgu/audiotour/config/SecurityConfig.java`
