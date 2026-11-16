# Audio Tour — Audio fix và nghiệm thu lại local

## 1. Kết luận và phạm vi

**PASS cho bản vá audio và luồng local đã kiểm chứng trong báo cáo này. Không phải chứng nhận production hoặc mọi chức năng của hệ thống.** P1 thiếu audio runtime đã được khắc phục bằng kho file thật trên host. Stream, Range, ZIP, browser decode/playback, analytics và persistence sau recreate app có bằng chứng. Không thay schema, secrets, credentials DB, API thành công hay nghiệp vụ. Báo cáo cũ `local-integration-acceptance.md` giữ nguyên làm baseline FAIL.

## 2. Root Cause Analysis

### P1 — file có trên host nhưng không có trong runtime

- Repo có 79 MP3 tracked, gồm tên seed mô tả địa điểm và các bản tên số/ngôn ngữ. Đối chiếu 24 `narrations.audio_url` hiện tại: cả24 file có trên host, không cần tạo audio giả hay gọi TTS.
- `DataInitializer` chỉ seed metadata. `NarrationService` đọc `Paths.get("uploads", "audio")`; `TtsService` ghi cùng thư mục, tên UUID mới và atomic move. Không có Spring property riêng cho audio root.
- Docker `WORKDIR /app`; image runtime chỉ có JAR, không copy audio, app không có mount. Vì vậy đường dẫn thực `/app/uploads/audio` thiếu. TTS mới nếu ghi vào writable layer cũ cũng không bền qua recreate.
- Audio seed và TTS cùng mô hình file đĩa theo ADR0004. Copy audio động vào image không phù hợp; tạo thêm storage abstraction/property không cần thiết cho lỗi này.

### P2 — error handling/content negotiation

- `resolveAudioPath` đưa absolute path vào RuntimeException; global handler trả400 kèm message. ADR0004 mô tả FR-PKG-03 missing MP3 là400: **giữ400**, không tự đổi sang404.
- Mapping stream chỉ produces audio/mpeg; Accept application/json làm Spring ném HttpMediaTypeNotAcceptableException trước controller. Global catch-all biến nó thành500.
- Error body Map chưa có Content-Type rõ; khi Accept audio/mpeg, serialize lỗi có thể thất bại rồi error dispatch bị security chặn401. Không nới authorization để che lỗi.

## 3. Bản vá và file thay đổi

| File | Thay đổi trong nhiệm vụ này |
|---|---|
| `docker-compose.yml` | App bind RW `./uploads/audio` vào `/app/uploads/audio`, `create_host_path: false`; không đổi mount MySQL |
| `src/main/java/vn/edu/sgu/audiotour/service/NarrationService.java` | Message thiếu file không còn absolute path; giữ400 |
| `src/main/java/vn/edu/sgu/audiotour/exception/GlobalExceptionHandler.java` | Incompatible Accept trả406 không body; JSON error có Content-Type application/json rõ ràng |
| `src/test/java/vn/edu/sgu/audiotour/AudioPackageControllerTest.java` | Regression stream/Range/content negotiation/error, ZIP contract |
| `src/test/java/vn/edu/sgu/audiotour/NarrationServiceTest.java` | Regression message sanitized; fixture @TempDir tránh va chạm asset tracked |
| `README.md` | Cách chạy mount audio, quyền ghi, backup, Docker run riêng |
| `docs/specs/feature-dashboard-admin-login.md` | Sửa trạng thái Chưa triển khai đã lỗi thời, dẫn bằng chứng nghiệm thu |
| Báo cáo này | RCA, kiểm chứng và giới hạn |

README/Compose đã có thay đổi từ mốc trước; không hoàn tác. Không sửa Dockerfile, Spring audio path, SecurityConfig, frontend code, `.env`, schema hoặc seed MP3. Không dependency mới, commit hay push.

### Giải pháp storage

Bind mount dùng đường dẫn tương đối trong repo, tái lập trên máy khác khi clone đủ asset. Thiếu thư mục thì fail thay vì tạo kho rỗng. TTS tiếp tục ghi tên mới vào cùng mount; host giữ file qua restart/recreate. Không tự ghi đè file có sẵn. Cần backup audio cùng DB để giữ ánh xạ tên. Container được quyền ghi thư mục host: chỉ chạy image tin cậy. Không làm mất MySQL và không biến file runtime thành image layer.

## 4. Test trước/sau

### RED

`mvnw.cmd -Dtest=AudioPackageControllerTest,NarrationServiceTest test`

16 tests:3 failures,3 errors. Bằng chứng: incompatible Accept expected406 got500; missing audio message chứa absolute path thay vì message sanitized; lỗi serialize error response theo Accept. Kết quả RED được chạy trước production Java patch.

### GREEN và build

`mvnw.cmd -Dtest=AudioPackageControllerTest,NarrationServiceTest,NarrationControllerTest,LocationControllerTest,AnalyticsControllerValidationTest,TtsServiceTest test`

35 tests,0 failures,0 errors,0 skipped; BUILD SUCCESS. Bao phủ2 audio class và controller validation/TTS liên quan do global error helper thay đổi. `mvnw.cmd compile`: BUILD SUCCESS. Maven cũng build frontend Vite thành công, có warning React Router `use client` bị bỏ qua như trước, không chặn build. Mockito/JDK agent warning còn tồn tại.

Không chạy lại full173 tests: thay đổi hẹp, chạy35 test liên quan và runtime smoke auth/API. Cache npm riêng `target/npm-cache` như môi trường đã xác nhận.

Docker `compose build app`: thành công; image mới `sha256:da579072f0197feeda81d67e60eaaaf9e5ba997dffea951537fccc235d5d2926` (config digest). Recreate chỉ service app, không rebuild/recreate DB.

## 5. Streaming, Range, lỗi và ZIP

| Kiểm tra | Kết quả thực tế |
|---|---|
| Seed1 vi, anonymous Accept */* | 200,147456 bytes, SHA256 khớp MP3 thật trên host |
| MIME / disposition | `audio/mpeg`; `inline; filename="tram-khanh-hoi-vi.mp3"` |
| Range bytes0-1023 | 206; `Content-Range: bytes 0-1023/147456`;1024 byte khớp nguồn |
| Range bytes1024-2047 trên image mới | 206; byte đúng đoạn nguồn |
| Accept application/json | 406, body rỗng; không còn500 |
| Configured missing audio, Accept audio/mpeg và */* | 400 JSON, message `File âm thanh không tồn tại`; không chứa `/app/` hoặc uploads |
| Narration không tồn tại | Regression test404 JSON; không đổi contract |
| Admin dashboard anonymous / authenticated | 401 / 200; không thay security rules |
| ZIP fixture | 200 application/zip; `content.txt` UTF-8 đúng text fixture và MP3 đúng byte nguồn |
| ZIP sau recreate | 200; entries `content.txt`, `audiofix-20261115001355-real.mp3`; audio byte chính xác |

ZIP dùng tên MP3 thực từ metadata, không đổi thành tên cố định audio.mp3 vì implementation/contract hiện hành giữ filename. Test service đã kiểm tra text/audio entries. Không tự động gọi dịch vụ ElevenLabs/DeepL.

## 6. Browser E2E và frontend

Chrome có sẵn, tab/profile kiểm thử, không cài MCP/dependency. Không sửa DOM hay giả currentTime/state.

Fixture `AUDIOFIX-20261115001355`: admin login200; POI10 POST201; narration27 vi POST201. API hiện chưa có upload/link audio; TTS thật thiếu key. Để kiểm tra kỹ thuật playback, chỉ metadata của narration fixture được liên kết có điều kiện bằng SQL đến MP3 seed thật. Văn bản fixture ghi rõ đây là linked existing seed, không phải giọng mới khớp văn bản. Sau đó liên kết bản sao MP3 thật tên riêng để kiểm tra file mới/persistence. Không chỉnh narration/POI seed.

1. Mở `/next/player/10?lang=vi`; thấy loading rồi text và controls.
2. HTMLAudioElement readyState4, duration18.432s; click Phát audio thật. Browser decode thành công, currentTime tăng qua4s; screenshot hiển thị00:04/00:18.
3. Pause khoảng12.119616s, rời trang. MySQL playback9 lưu12 giây, trigger MANUAL, đúng location10. Dashboard tổng7 thành8, offlineDownloads1. Không coi POST thành công đơn thuần là bằng chứng nghe: ở đây có media decoder, currentTime và event UI thực.
4. Trên image Java đã sửa/recreate, chạy browser lại: currentTime3.091171, duration18.432, paused false, không media error. Rời trang tạo playback10 duration5s; tổng dashboard9, offlineDownloads2, active0.
5. ZIP kiểm tra giải nén trong bộ nhớ, text và byte MP3 đúng. Không triển khai UI offline player mới hoặc tuyên bố offline web caching hoàn chỉnh.
6. Missing-file fixture làm UI hiện `Không phát được audio... Thử lại`, không giả lập network success. Sau probe khôi phục tên audio fixture trước ZIP.
7. Search positive: native keystrokes có delay250ms đưa đúng `Dinh`, UI trả1 địa điểm Dinh Độc Lập. Fill nhanh/clipboard trước đó không tin cậy, không dùng timeout làm lỗi sản phẩm. `Khanh` không match tên có dấu không được coi lỗi vì chưa có contract bỏ dấu.
8. Sort Tên A–Z: fixture AUDIOFIX, Dinh Độc Lập, Phòng Đại Yến, Phòng Khánh Tiết, các Trạm… đúng thứ tự hiển thị. QR nhập mã fixture bằng native paced input và nút Tìm: URL thực `/next/places/10`, đúng detail; helper waitForUrl glob timeout không phủ nhận URL/DOM quan sát thực.
9. Player375x812 screenshot controls và text, không horizontal overflow. Desktop1440 screenshot playback. Không đại diện mọi viewport/trang admin.

Screenshot desktop playback: `C:\Users\User\AppData\Local\Temp\omp-sshots-15ce10ba46ead85b.webp`; ảnh mobile đã xuất trong transcript, temp artifact có thể hết hạn. Không cần người dùng nghe qua loa để xác nhận browser decoding; chưa đánh giá chất lượng giọng bằng thính giác.

## 7. Persistence và an toàn dữ liệu

- DB container giữ ID `3bce8a870d8e80b9b1468964046ea63d125fdf0b96f58a5910b3d7efbeecf33a`, mount `audiotour-mysql-local-v5:/var/lib/mysql`, healthy. Không restart/recreate/reset DB trong lần sửa này.
- Volume cũ `multi_languages_automatic_translation_audiotour-db-data` vẫn tồn tại, không mount/ghi/xóa. Không có lệnh prune/down -v.
- Trước recreate: dashboard active0, browser đã đóng; inspect mount DB và parsed Compose xác nhận tên volume. `docker compose up -d --no-deps --no-build --force-recreate app` chỉ app. Cảnh báo volume cũ left untouched không được dùng làm lý do xóa.
- File mới thử nghiệm là bản sao hợp lệ của seed, tên riêng `audiofix-20261115001355-real.mp3`, tạo exclusive, không overwrite. SHA256 trước/sau recreate và trong container đều `0d2c3c23fcd1ef6ac94e5c083690392cbb2a4cb7cce95a70bdfd663fb9b188b8`. Stream/Range/ZIP sau recreate đọc đúng file. Metadata narration27 và playback9 vẫn tồn tại chính xác.
- Persistence của file mới đã được chứng minh; không tuyên bố TTS upstream đã chạy. Code TTS atomic-write dùng chính mount này; TtsServiceTest7 tests đạt.
- Cleanup chỉ fixture: playback IDs9,10 AND location10 AND device browser đúng; deleted2. Package downloads location10 AND device prefix test; deleted2. Narration27 DELETE204, POI10 DELETE204. Xóa đúng bản sao audio sau kiểm tra tên/parent/hash; không xóa MP3 gốc. Không reset AUTO_INCREMENT.
- Số liệu cuối trở lại6 locations,24 narrations,7 playback_logs,0 downloads,4 languages. Health200 UP; app Running, RestartCount0; DB healthy. Log mẫu image mới không có ERROR/Exception, chỉ Spring UserDetailsServiceAutoConfiguration warning, không đổi auth.

## 8. P2 và việc không tự mở rộng

- MySQL root: vẫn là rủi ro least privilege. Đề xuất sau phê duyệt tạo user ứng dụng đúng schema và quyền Flyway/JPA cần, cấu hình credential riêng, test rồi chuyển. **Chưa đổi user/quyền/.env**.
- Ảnh Xóm Chiếu bị Chrome ORB: fallback đã có, nguồn bên ngoài; không tự thay ảnh hoặc seed DB trong bản vá audio.
- SPEC login trạng thái cũ đã chỉnh đúng, không sửa lịch sử ADR.
- Upload audio: không có endpoint trong SPEC triển khai hiện tại; không tự thêm. Link fixture bằng SQL là thao tác test được ghi rõ, không giả thành tính năng UI admin.
- Error helper chung vẫn truyền message của các exception khác theo implementation cũ; bản vá này chỉ loại đường dẫn audio missing và sửa negotiation, không tuyên bố hoàn tất mọi hardening.

## 9. Blocked / Not Tested

- TTS/DeepL thật: thiếu API key; không tiêu credit hoặc tạo giọng giả.
- Chất lượng/độ tương đồng ngôn ngữ/audio, đo thời lượng metadata seed: không đánh giá. MP3 browser đo18.432s khác metadata seed90s, là dữ liệu cũ; fixture duration9 cũng không được dùng thay media duration thực.
- Camera QR/GPS thực địa/near-distance sort, mọi viewport/admin responsive, full accessibility, stress/load/cloud: không kiểm tra.
- Không chạy full suite hoặc password rotation; dùng baseline173 và35 tests liên quan, auth smoke. Không thay API400 missing audio theo ADR.
- Không chứng minh byte-for-byte lịch sử volume cũ trước baseline; chỉ bảo vệ trong các thao tác và xác nhận tồn tại/mount đúng.

## 10. Kết luận nghiệm thu mới

**PASS phạm vi sửa audio local:** P1 được xác nhận/fix; audio thật runtime, stream200/Range206, missing-file400 sạch path, incompatible Accept406, ZIP hợp đồng cũ, browser playback và analytics thực, recreate persistence, backend/DB healthy, fixture cleanup an toàn. Các giới hạn/P2 nêu trên không được ghi Passed và không đồng nghĩa production-ready hoặc hoàn tất tích hợp TTS thật. Muốn chứng nhận toàn hệ thống phải tiếp tục các hạng mục ngoài phạm vi bằng điều kiện phù hợp.
