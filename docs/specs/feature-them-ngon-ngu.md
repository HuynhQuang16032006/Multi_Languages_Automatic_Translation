# Đặc tả: Admin thêm ngôn ngữ mới (dịch máy + TTS)

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Đã chốt, chưa triển khai |
| Ngày | 2026-10-24 |
| Liên quan | ADR 0004 (TTS), ADR 0005 (thêm ngôn ngữ) |

## Vấn đề

Hệ thống có sẵn CRUD `Language` và TTS, nhưng thêm ngôn ngữ mới vẫn là việc của dev: phải viết tay nội dung cho từng POI rồi gọi API sinh audio. Dashboard không có chức năng này, cũng không có đăng nhập.

## Kết quả mong muốn

Admin đăng nhập dashboard, chọn một ngôn ngữ trong danh sách hỗ trợ, bấm một nút. Hệ thống dịch 6 narration `vi`, sinh 6 file MP3, và du khách chọn được ngôn ngữ mới ngay trên player.

## Phạm vi

Trong phạm vi:

- Nút "Thêm ngôn ngữ" trên dashboard, kèm đăng nhập admin.
- Endpoint dịch + sinh audio cho một ngôn ngữ.
- Danh sách ngôn ngữ hỗ trợ đọc từ cấu hình.
- `player.html` và `demo.html` lấy ngôn ngữ động từ API thay vì hardcode.

Ngoài phạm vi:

- Sửa hoặc xoá ngôn ngữ trên dashboard (khoá ngoại narration chặn xoá).
- Sửa bản dịch bằng tay (chưa có CRUD narration, backlog B-01).
- Dịch lại khi thêm POI mới. Làm sau bằng cách gọi lại endpoint cho narration thiếu.
- Dịch từ `en`/`zh`, hoặc dịch ngược về `vi`.
- Sinh audio tự động khi du khách nghe.

## Luồng

1. Admin mở `dashboard.html`, đăng nhập bằng `POST /api/auth/login`, token giữ trong `sessionStorage`.
2. Dashboard gọi `GET /api/languages/supported`, đổ vào dropdown. Mỗi mục hiện tên và mã.
3. Admin chọn ngôn ngữ, bấm "Thêm". Dashboard gọi `POST /api/languages/{code}/translate` và hiện trạng thái chờ.
4. Server kiểm tra mã nằm trong danh sách hỗ trợ và chưa tồn tại. Sau đó lấy 6 narration `vi` làm nguồn.
5. Server gọi DeepL cho từng narration. Chỉ khi cả 6 bản dịch thành công mới ghi `Language` và 6 `Narration` mới trong một transaction. Lỗi ở bất kỳ bản nào thì không ghi gì.
6. Sau khi commit, server gọi `TtsService` cho từng narration mới. `TtsService` ghi file rồi cập nhật `audioUrl`, đúng ADR 0004.
7. Server trả 201 kèm số bản dịch và số file audio sinh được. Dashboard làm mới danh sách ngôn ngữ.
8. Du khách mở player, chọn ngôn ngữ mới, nghe bình thường. POI không có narration cho ngôn ngữ đó hiện "Chưa có bản thuyết minh cho ngôn ngữ này".

Request giữ kết nối 20 đến 30 giây vì sinh 6 file tuần tự. Chấp nhận được với thao tác của admin.

## API

### `GET /api/languages/supported` (công khai)

Trả danh sách mã và tên ngôn ngữ lấy từ cấu hình, không đọc DB.

```json
[
  { "code": "ja", "name": "日本語" },
  { "code": "ko", "name": "한국어" }
]
```

### `POST /api/languages/{code}/translate` (cần token)

Không có body. `code` là mã trong danh sách hỗ trợ.

Thành công 201:

```json
{
  "language": { "id": 4, "code": "ja", "name": "日本語" },
  "translations": 6,
  "audioGenerated": 6
}
```

Lỗi:

| Mã | Khi nào | Ghi chú |
| --- | --- | --- |
| 400 | `code` không có trong danh sách hỗ trợ, hoặc chưa có narration `vi` để dịch | |
| 409 | `Language` đã tồn tại | Kiểm tra trước khi gọi DeepL để không tốn credit |
| 503 | Thiếu `app.mt.api-key` hoặc DeepL lỗi | Không ghi gì vào DB |

Khi TTS lỗi giữa đường, request vẫn trả 201 với `audioGenerated` nhỏ hơn `translations`. Sinh audio là side effect không rollback được (file đã ghi, credit đã tiêu), nên báo số lượng thay vì giả vờ hỏng cả thao tác. Admin gọi lại `POST /api/narrations/{id}/audio` cho từng narration thiếu; endpoint hiện có đã làm được việc này.

## Dữ liệu

Mỗi ngôn ngữ mới sinh 6 `Narration`:

- `location`: POI tương ứng với narration `vi` nguồn.
- `language`: `Language` vừa tạo.
- `textContent`: bản dịch từ DeepL.
- `audioUrl`: `null` trước khi sinh audio, sau đó là `<locationId>-<code>.mp3` theo quy tắc sẵn có của `TtsService`.
- `durationInSeconds`: `textContent.length() / 15`, giữ nguyên ước lượng đã ghi ở ADR 0004.

Nguồn dịch cố định là 6 narration `vi`. POI không có bản `vi` thì bỏ qua.

## Cấu hình

```yaml
app:
  mt:
    api-key: ${DEEPL_API_KEY:}
    base-url: https://api-free.deepl.com
  languages:
    supported:
      - code: ja
        name: 日本語
      - code: ko
        name: 한국어
```

Danh sách hỗ trợ là giao của hai tập: ngôn ngữ `eleven_flash_v2_5` đọc được và ngôn ngữ DeepL dịch được. Chép từ tài liệu hai nhà cung cấp lúc triển khai, không tự suy ra.

DeepL nhận mã chữ hoa. `zh` gửi thành `ZH-HANS`; các mã còn lại chỉ cần `toUpperCase()`.

`vi` chỉ chạy trên model next-gen của DeepL. Trước khi viết code, gọi thử một câu `vi` sang `ja` bằng key thật. Nếu free tier từ chối, thêm tham số chọn model hoặc chuyển sang Azure Translator F0.

## Giao diện

`dashboard.html`:

- Form đăng nhập nhỏ, gọi `POST /api/auth/login`, lưu token trong `sessionStorage`, đính `Authorization: Bearer` cho mọi request ghi.
- Khối "Thêm ngôn ngữ": dropdown từ `/api/languages/supported`, nút thêm, spinner trong lúc chờ, thông báo kết quả.

`player.html`:

- Bỏ hai nút `vi`/`en` hardcode. Render nút từ `GET /api/languages`.
- Khi `GET /api/narrations?locationId=&languageCode=` trả rỗng, hiện thông báo thay vì phát im lặng.

`demo.html`:

- Đổ `<option>` từ `GET /api/languages`.

`index.html` đã lấy ngôn ngữ động, không sửa.

## Kiểm thử chấp nhận

1. DB sạch, thêm `ja`: 201, DB có 4 `Language`, 6 `Narration` mới, `uploads/audio` có 6 file `<locationId>-ja.mp3`, player phát được.
2. Gọi lại `ja`: 409, không sinh thêm file.
3. Mã `xx` ngoài danh sách: 400.
4. Tắt mạng hoặc dùng key DeepL sai: 502, DB không có `ja`.
5. Xoá một file MP3 của `ja`: player hiện thông báo, không treo.
6. Không token: `POST /api/languages/ja/translate` trả 401; `GET /api/languages/supported` trả 200.
7. 115 test hiện có vẫn xanh, thêm test cho `TranslationService` và luồng thêm ngôn ngữ.

## Ước lượng

| Việc | Thời gian |
| --- | --- |
| `TranslationService`, cấu hình, endpoint, xử lý lỗi | 2 giờ |
| Đăng nhập + khối thêm ngôn ngữ trên dashboard | 1 giờ |
| `player.html`, `demo.html` lấy ngôn ngữ động | 45 phút |
| Test + chạy thử end-to-end | 1 giờ |

Tổng khoảng 5 giờ, chia 4 lần commit theo Conventional Commits.

## Rủi ro

- Bản dịch không có người soát. Muốn sửa phải sửa DB trực tiếp cho tới khi có CRUD narration.
- DeepL free tier không có DPA và có thể dùng dữ liệu để cải thiện dịch vụ. Không dán nội dung nhạy cảm.
- Một ngôn ngữ mới tốn khoảng 1.600 ký tự TTS, tức 16% quota tháng. Thêm 3 ngôn ngữ là gần nửa quota.
