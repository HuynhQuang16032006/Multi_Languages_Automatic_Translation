# TÀI LIỆU ĐẶC TẢ LƯỢC ĐỒ HOẠT ĐỘNG (UML 2.5 ACTIVITY DIAGRAMS)

---

## 🏛️ THÔNG TIN TÀI LIỆU

* **Tên hệ thống:** Hệ thống Thuyết minh Audio Tour Tự Động Đa Ngôn Ngữ (*Smart Multi-Languages Automatic Audio Tour System*)
* **Môn học:** Công nghệ Phần mềm (*Software Engineering*) – Khoa Công nghệ Thông tin, Trường Đại học Sài Gòn (SGU)
* **Phiên bản tài liệu:** v2.1 (Chuẩn hóa theo kiến trúc Backend 3 lớp, CSDL MySQL Flyway và quy chuẩn UML 2.5)
* **Mục đích tài liệu:** Mô hình hóa các luồng quy trình nghiệp vụ cốt lõi, các điểm quyết định rẽ nhánh (*Decision Nodes*), luồng xử lý song song (*Fork/Join*) và sự phối hợp giữa các phân hệ (*Swimlanes*) trong môi trường vận hành thực tế.

---

## 📐 1. QUY ƯỚC KÝ HIỆU UML 2.5 ACTIVITY DIAGRAM

| Thành phần UML | Ký hiệu | Ý nghĩa trong Hệ thống |
| :--- | :---: | :--- |
| **Điểm khởi đầu (Initial Node)** | `((●))` | Điểm bắt đầu của một quy trình nghiệp vụ khi có tác nhân kích hoạt. |
| **Hoạt động (Action / Activity)** | `[Hoạt động]` | Một bước xử lý nghiệp vụ nguyên tử tại Client, Application Tier hoặc Data Tier. |
| **Nút quyết định (Decision / Merge)** | `{Điều kiện?}` | Kiểm tra điều kiện rẽ nhánh (`[Đúng]` / `[Sai]`) dựa trên trạng thái dữ liệu hoặc ràng buộc nghiệp vụ. |
| **Thanh đồng bộ (Fork / Join Bar)** | `===` | Tách luồng thực thi song song (ví dụ: vừa phát âm thanh vừa gửi tín hiệu Telemetry) và hợp nhất luồng. |
| **Điểm kết thúc (Activity Final Node)** | `(((◉)))` | Điểm kết thúc hoàn tất một quy trình nghiệp vụ. |

---

## 🗺️ 2. LƯỢC ĐỒ HOẠT ĐỘNG 1: QUY TRÌNH ĐỊNH VỊ ĐA TẦNG & KÍCH HOẠT THUYẾT MINH TỰ ĐỘNG GEOFENCING (AD-01)

* **Mô tả nghiệp vụ:** Mô tả quy trình từ khi du khách bật tính năng dẫn đường rảnh tay (*Hands-free Audio Tour*), hệ thống thu thập tọa độ qua cơ chế định vị 3 tầng, tính toán khoảng cách địa lý Haversine, phân giải xung đột ưu tiên theo cấu trúc phân cấp Cha – Con, đến khi tải gói âm thanh ngoại tuyến và phát thuyết minh.

```mermaid
flowchart TD
    subgraph ClientTier ["Làn 1: Ứng dụng Du khách (Client Tier)"]
        Start((●)) --> A1["Khởi tạo bản đồ số & Nạp danh sách POI"]
        A1 --> A2["Kích hoạt dịch vụ định vị 3 tầng<br/>(GPS phần cứng ➔ Mạng Wi-Fi/Cell ➔ IP Geolocation)"]
        A2 --> A3["Cập nhật tọa độ người dùng (lat, lng)<br/>& Tính khoảng cách Haversine tới các POI"]
        A3 --> D1{"Khoảng cách d <= Bán kính R<br/>của ít nhất 1 POI?"}

        D1 -- "[Không]" --> A4["Hiển thị khoảng cách tới các POI gần nhất<br/>& Tiếp tục lắng nghe sự kiện di chuyển"]
        A4 --> A3

        D1 -- "[Có]" --> A5["Tập hợp danh sách các POI đang giao vùng phủ sóng"]
        A5 --> A6["Phân giải độ ưu tiên kích hoạt:<br/>1. Ưu tiên điểm phân cấp con (parent_id != null)<br/>2. Ưu tiên bán kính vùng phủ nhỏ hơn (chi tiết hơn)<br/>3. Ưu tiên trọng số priority cao hơn & khoảng cách gần tâm nhất"]
        A6 --> D2{"Điểm POI được chọn<br/>đang phát hoặc trong thời gian Cooldown?"}

        D2 -- "[Đang Cooldown]" --> A3
        D2 -- "[Hợp lệ kích hoạt]" --> D3{"Gói thuyết minh (POI, Ngôn ngữ)<br/>đã có trong Bộ nhớ đệm thiết bị?"}
    end

    subgraph AppTier ["Làn 2: Tầng Nghiệp vụ & Dữ liệu (Backend 3-Tier)"]
        D3 -- "[Chưa có Cache]" --> B1["Gửi yêu cầu GET /api/packages/offline<br/>(locationId, languageCode, deviceId)"]
        B1 --> B2["LocationService & NarrationService:<br/>Truy xuất kịch bản văn bản & tệp âm thanh MP3"]
        B2 --> B3["PackageDownloadRepository:<br/>Ghi nhận lượt tải gói ngoại tuyến vào CSDL"]
        B3 --> B4["NarrationService (ZipOutputStream):<br/>Đóng gói luồng động content.txt + audio.mp3"]
    end

    subgraph PlaybackFlow ["Làn 3: Giải nén & Phát Đa Phương Tiện"]
        B4 --> P1["Giải nén gói ZIP trong bộ nhớ thiết bị<br/>& Lưu vào bộ nhớ đệm cục bộ (Offline Cache)"]
        D3 -- "[Đã có Cache]" --> P2["Truy xuất trực tiếp kịch bản & âm thanh từ bộ nhớ đệm"]
        P1 --> P3["Khởi chạy trình phát âm thanh (Audio Player)<br/>& Hiển thị kịch bản thuyết minh đa ngôn ngữ"]
        P2 --> P3
        P3 --> End(((◉)))
    end
```

---

## 📱 3. LƯỢC ĐỒ HOẠT ĐỘNG 2: QUY TRÌNH QUÉT MÃ QR & ĐỒNG BỘ PHIÊN TRỰC TUYẾN THỜI GIAN THỰC (AD-02)

* **Mô tả nghiệp vụ:** Mô tả quy trình du khách tiếp cận điểm di tích thông qua mã QR định danh tại hiện trường, cơ chế duy trì nhịp tim (*Stateful Heartbeat*) với `LiveAudience` và tự động ghi nhận nhật ký nghe (`PlaybackLog`) khi kết thúc phiên.

```mermaid
flowchart TD
    Start((●)) --> Q1["Du khách quét mã QR tại điểm tham quan"]
    Q1 --> Q2["Gửi yêu cầu tra cứu GET /api/locations/by-qr/{qrCode}"]

    Q2 --> D1{"Mã QR tồn tại &<br/>Địa điểm đang hiển thị (visible = true)?"}
    D1 -- "[Không hợp lệ / Đang ẩn]" --> Q3["Trả về HTTP 404 Not Found<br/>& Hiển thị thông báo không tìm thấy địa điểm"]
    Q3 --> EndErr(((◉)))

    D1 -- "[Hợp lệ]" --> Q4["Trả về thông tin chi tiết POI & Danh sách khu vực con"]
    Q4 --> Q5["Khởi tạo mã định danh phiên ẩn danh (UUID)<br/>& Gửi tín hiệu Heartbeat khởi tạo (action = JOIN)"]

    Q5 --> Q6["LiveAudience đăng ký phiên vào ConcurrentHashMap<br/>& Cập nhật bộ đếm khách trực tuyến (activeUsers)"]

    Q6 --> ForkBar["=== Bắt đầu 2 luồng song song ==="]

    ForkBar --> L1["Luồng 1 (Trình phát):<br/>Tải gói thuyết minh theo ngôn ngữ đã chọn<br/>& Phát âm thanh cho du khách"]
    ForkBar --> L2["Luồng 2 (Telemetry):<br/>Định kỳ mỗi 3 giây gửi Heartbeat (action = PING)<br/>kèm thời lượng đã nghe (listenDurationSeconds)"]

    L1 --> JoinCheck{"Du khách đóng ứng dụng (LEAVE)<br/>hoặc mất kết nối > 8 giây?"}
    L2 --> JoinCheck

    JoinCheck -- "[Đang trong phiên nghe]" --> L2
    JoinCheck -- "[Kết thúc phiên / Timeout > 8s]" --> Q7["LiveAudience thu hồi phiên khỏi bộ nhớ<br/>& Giảm số lượng khách đang trực tuyến"]

    Q7 --> D2{"Thời lượng nghe thực tế > 0 giây?"}
    D2 -- "[Không]" --> End(((◉)))
    D2 -- "[Có]" --> Q8["PlaybackLogRecorder kiểm tra toàn vẹn dữ liệu<br/>& Lưu bản ghi vào bảng playback_logs"]
    Q8 --> End
```

---

## 🔐 4. LƯỢC ĐỒ HOẠT ĐỘNG 3: QUY TRÌNH XÁC THỰC QUẢN TRỊ, BẢO VỆ BRUTE-FORCE & QUẢN LÝ ĐỊA ĐIỂM PHÂN CẤP (AD-03)

* **Mô tả nghiệp vụ:** Mô tả quy trình bảo mật đăng nhập bằng Stateless JWT kết hợp bộ giới hạn tần suất `LoginRateLimiter`, thu hồi Token khi đổi mật khẩu, và cơ chế khóa đồng bộ (*Pessimistic Locking*) khi cập nhật cây phân cấp địa điểm Cha – Con.

```mermaid
flowchart TD
    Start((●)) --> M1["Quản trị viên gửi yêu cầu đăng nhập<br/>POST /api/auth/login (username, password)"]
    M1 --> D1{"LoginRateLimiter:<br/>Số lần sai từ IP hiện tại >= Ngưỡng khóa?"}

    D1 -- "[Đã vượt ngưỡng]" --> M2["Từ chối xử lý & Trả về HTTP 429 Too Many Requests"]
    M2 --> EndBlock(((◉)))

    D1 -- "[Trong giới hạn]" --> M3["AuthService truy vấn bảng admin_auth<br/>& Kiểm tra mật khẩu băm BCrypt"]
    M3 --> D2{"Thông tin xác thực chính xác?"}

    D2 -- "[Sai thông tin]" --> M4["LoginRateLimiter tăng bộ đếm lỗi của IP<br/>& Trả về HTTP 401 Unauthorized"]
    M4 --> EndUnauth(((◉)))

    D2 -- "[Hợp lệ]" --> M5["Xóa bộ đếm lỗi của IP & Ký JWT Token (HS256)<br/>kèm chữ ký đại diện của password_hash hiện hành"]
    M5 --> M6["Quản trị viên gửi yêu cầu thêm/cập nhật POI<br/>kèm Header Authorization: Bearer <token>"]

    M6 --> D3{"JwtAuthenticationFilter:<br/>Token hợp lệ & khớp password_hash?"}
    D3 -- "[Token hết hạn / Đã đổi mật khẩu]" --> M4

    D3 -- "[Xác thực thành công]" --> M7["LocationService kích hoạt khóa đồng bộ:<br/>SELECT id FROM location_hierarchy_lock FOR UPDATE"]
    M7 --> D4{"Kiểm tra ràng buộc nghiệp vụ:<br/>1. Mã QR không trùng lặp<br/>2. Không tạo chu trình Cha - Con<br/>3. Không vi phạm quy tắc Ẩn/Hiện Cha - Con"}

    D4 -- "[Vi phạm ràng buộc]" --> M8["Rollback giao dịch & Trả về HTTP 409 Conflict"]
    M8 --> EndConflict(((◉)))

    D4 -- "[Thỏa mãn toàn bộ ràng buộc]" --> M9["LocationRepository lưu thực thể vào bảng locations<br/>& Giải phóng khóa giao dịch"]
    M9 --> End(((◉)))
```

---

## 🌐 5. LƯỢC ĐỒ HOẠT ĐỘNG 4: QUY TRÌNH TỰ ĐỘNG DỊCH MÁY (DEEPL) & TỔNG HỢP GIỌNG ĐỌC AI (ELEVENLABS TTS) (AD-04)

* **Mô tả nghiệp vụ:** Mô tả quy trình mở rộng ngôn ngữ tự động trong `LanguageOnboardingService`, đảm bảo tính nguyên tử của dữ liệu CSDL và tính toàn vẹn của tệp âm thanh vật lý thông qua kỹ thuật `ATOMIC_MOVE`.

```mermaid
flowchart TD
    Start((●)) --> T1["Quản trị viên yêu cầu thêm ngôn ngữ mới<br/>POST /api/languages/{code}/translate"]
    T1 --> D1{"Mã ngôn ngữ nằm trong danh sách hỗ trợ<br/>(app.languages.supported)?"}

    D1 -- "[Không hỗ trợ]" --> T2["Trả về HTTP 400 Bad Request"] --> End1(((◉)))

    D1 -- "[Hợp lệ]" --> D2{"LanguageRepository:<br/>Mã ngôn ngữ đã tồn tại trong CSDL?"}
    D2 -- "[Đã tồn tại]" --> T3["Trả về HTTP 409 Conflict<br/>(Ngắt sớm trước khi gọi API dịch thuật để bảo toàn hạn mức)"] --> End2(((◉)))

    D2 -- "[Chưa tồn tại]" --> T4["Truy xuất toàn bộ kịch bản gốc Tiếng Việt (vi)<br/>từ bảng narrations"]
    T4 --> T5["TranslationService gửi văn bản tới DeepL API<br/>để dịch sang ngôn ngữ đích"]

    T5 --> D3{"Toàn bộ bản dịch<br/>thành công?"}
    D3 -- "[Lỗi kết nối / Quota DeepL]" --> T6["Không ghi vào CSDL & Trả về HTTP 503 Service Unavailable"] --> End3(((◉)))

    D3 -- "[Thành công]" --> T7["LanguageService (@Transactional):<br/>Lưu bản ghi Language mới và toàn bộ Narrations đã dịch vào MySQL"]
    T7 --> T8["Duyệt qua từng bản ghi Narration mới<br/>& Gọi TtsService.generateAudio()"]

    T8 --> T9["Gửi kịch bản tới ElevenLabs API nhận luồng byte MP3<br/>& Ghi vào tệp tạm tts-*.part"]
    T9 --> T10["Thực hiện đổi tên nguyên tử (Files.move ATOMIC_MOVE)<br/>sang tệp .mp3 chính thức trong uploads/audio/"]
    T10 --> T11["Cập nhật đường dẫn audio_url & thời lượng vào bảng narrations"]
    T11 --> T12["Trả về HTTP 201 Created kèm thống kê số bản dịch & số file audio"]
    T12 --> End(((◉)))
```
