# TÀI LIỆU SƠ ĐỒ LUỒNG DỮ LIỆU (DATA FLOW DIAGRAMS - DFD)

---

## 🏛️ THÔNG TIN TỔNG QUAN

* **Hệ thống:** Hệ thống Thuyết minh Audio Tour Tự Động Đa Ngôn Ngữ (*Smart Multi-Languages Automatic Audio Tour System*)
* **Môn học:** Công nghệ Phần mềm (Software Engineering) – Khoa Công nghệ Thông tin, Trường Đại học Sài Gòn (SGU)
* **Phiên bản tài liệu:** v2.1 (Đồng bộ kiến trúc Backend 3 lớp, CSDL 4 bảng chuẩn hóa, 2 chế độ nghe POI & Real-time Dashboard)
* **Phương pháp luận:** Phân tích và Thiết kế Hệ thống Hướng cấu trúc (*Structured Systems Analysis and Design*), kết hợp ký hiệu chuẩn **Yourdon - DeMarco** & **Gane - Sarson**.

---

## 📐 1. QUY ƯỚC KÝ HIỆU & THÀNH PHẦN DFD (DFD NOTATIONS & COMPONENTS)

### 1.1. Quy ước biểu diễn trên sơ đồ Mermaid

| Thành phần DFD | Ký hiệu chuẩn | Ý nghĩa trong Hệ thống Audio Tour |
| :--- | :---: | :--- |
| **Tác nhân ngoài (External Entity)** | Hình chữ nhật `[Tác nhân]` | Đối tượng phát sinh hoặc tiếp nhận dữ liệu nằm ngoài phạm vi phần mềm (Du khách, Cảm biến định vị, Ban Quản Lý, Dịch vụ AI Cloud). |
| **Tiến trình (Process)** | Hình tròn / Bo tròn `((P1.0 ...))` | Chức năng biến đổi, kiểm tra hoặc điều phối luồng dữ liệu (tương ứng các Controller & Service trong Spring Boot). |
| **Kho dữ liệu (Data Store)** | Hình trụ `[(D1: ...)]` | Nơi lưu trữ dữ liệu tĩnh hoặc trạng thái hệ thống (4 bảng MySQL, thư mục file MP3, bộ nhớ phiên trực tuyến). |
| **Luồng dữ liệu (Data Flow)** | Mũi tên có nhãn `-->|Dữ liệu|` | Đường di chuyển của gói tin (HTTP Request/Response, SQL Query, File Stream) giữa các thành phần. |

---

### 1.2. Danh mục Tác nhân ngoài (External Entities)

| Mã | Tên Tác Nhân Ngoài | Mô Tả Chi Tiết Vai Trò Giao Tiếp Dữ Liệu |
| :---: | :--- | :--- |
| **E1** | **Du khách (Tourist)** | Người dùng cuối sử dụng Web Du khách (`index.html`): chọn ngôn ngữ, bấm ghim bản đồ nghe thủ công, quét mã QR, tải gói ZIP offline. |
| **E2** | **Hệ thống Định vị 3 Tầng (GPS / Network / IP Sensor)** | Cung cấp tọa độ địa lý thực tế `(latitude, longitude)` của thiết bị để vẽ hình người đi bộ (🚶) và tính khoảng cách Haversine kích hoạt Geofence tự động. |
| **E3** | **Ban Quản Lý / Admin (Site Administrator)** | Quản trị viên sử dụng Web Quản trị (`dashboard.html`): đăng nhập JWT, theo dõi khách đang online, xem máy đang nghe, phân tích Heatmap và thêm ngôn ngữ mới. |
| **E4** | **Dịch vụ AI Dịch thuật & TTS (DeepL & ElevenLabs / Edge-TTS)** | Hệ thống đám mây bên thứ ba nhận văn bản gốc tiếng Việt để dịch sang ngôn ngữ đích và tổng hợp giọng đọc nhân tạo thành tệp âm thanh `.mp3`. |

---

### 1.3. Danh mục Kho dữ liệu (Data Stores)

| Mã Kho | Tên Kho Dữ Liệu | Ánh Xạ Vật Lý (Database / Storage) | Nội Dung Lưu Trữ Chính |
| :---: | :--- | :--- | :--- |
| **D1** | **Kho Ngôn ngữ** | Bảng MySQL `languages` | Danh mục mã ngôn ngữ chuẩn ISO (`id`, `code`, `name`: `vi`, `en`, `zh`, `ja`,...). |
| **D2** | **Kho Địa điểm POI** | Bảng MySQL `locations` | Thông tin địa danh, tọa độ GPS (`latitude`, `longitude`), bán kính `radius_in_meters`, `priority`, `qr_code`, `parent_id`. |
| **D3** | **Kho Kịch bản Thuyết minh** | Bảng MySQL `narrations` | Nội dung văn bản thuyết minh (`text_content`), tên tệp âm thanh (`audio_url`), thời lượng (`duration_in_seconds`) theo từng cặp `(location_id, language_id)`. |
| **D4** | **Kho Nhật ký Nghe** | Bảng MySQL `playback_logs` | Lịch sử các lượt phát âm thanh (`location_id`, `language_code`, `listen_duration_seconds`, `trigger_type`, `latitude`, `longitude`, `device_id`, `created_at`). |
| **D5** | **Kho Tệp Âm thanh MP3** | Hệ thống tệp `uploads/audio/` | Kho lưu trữ vật lý các tệp âm thanh `.mp3` phục vụ phát trực tuyến (Streaming) và nén gói ngoại tuyến (ZIP). |
| **D6** | **Kho Phiên Trực tuyến** | Bộ nhớ RAM `LiveAudience` (ConcurrentHashMap) | Trạng thái nhịp tim (Heartbeat `JOIN`/`PING`/`LEAVE`) của các thiết bị đang hoạt động trong cửa sổ thời gian thực. |

---

## 🌐 2. SƠ ĐỒ DFD MỨC NGỮ CẢNH (CONTEXT DIAGRAM - LEVEL 0)

Sơ đồ mức ngữ cảnh (Level 0) thể hiện toàn bộ **Hệ thống Thuyết minh Audio Tour Tự Động Đa Ngôn Ngữ** như một tiến trình trung tâm duy nhất (`0.0`) tương tác với 4 tác nhân ngoài.

```mermaid
flowchart LR
    E1["👤 E1: Du khách<br/>(Tourist)"]
    E2["🛰️ E2: Hệ thống Định vị 3 Tầng<br/>(GPS / Wifi-Cell / IP)"]
    E3["👨‍💼 E3: Ban Quản Lý / Admin<br/>(Site Operator)"]
    E4["🤖 E4: Dịch vụ AI Ngoại vi<br/>(DeepL MT & ElevenLabs/Edge-TTS)"]

    P0(("0.0<br/>HỆ THỐNG THUYẾT MINH<br/>AUDIO TOUR TỰ ĐỘNG<br/>ĐA NGÔN NGỮ"))

    %% Luồng dữ liệu giữa Du khách và Hệ thống
    E1 -->|"Yêu cầu bản đồ POI, chọn ngôn ngữ,<br/>bấm ghim thủ công, mã QR, tải Offline"| P0
    E1 -->|"Tín hiệu Heartbeat (JOIN/PING/LEAVE)<br/>& Nhật ký lượt nghe (Playback Log)"| P0
    P0 -->|"Bản đồ POI, vị trí hình người 🚶,<br/>luồng Audio MP3 & gói tour-offline.zip"| E1

    %% Luồng dữ liệu giữa Cảm biến định vị và Hệ thống
    E2 -->|"Tọa độ thực tế (latitude, longitude)<br/>& độ chính xác (accuracy)"| P0
    P0 -->|"Yêu cầu lấy tọa độ định vị 3 tầng"| E2

    %% Luồng dữ liệu giữa Ban Quản Lý và Hệ thống
    E3 -->|"Thông tin đăng nhập, yêu cầu Dashboard,<br/>thêm/cập nhật ngôn ngữ & sinh Audio"| P0
    P0 -->|"JWT Token, số khách thực tế đang online,<br/>bảng máy đang nghe, biểu đồ & Heatmap"| E3

    %% Luồng dữ liệu giữa Hệ thống và Dịch vụ AI
    P0 -->|"Văn bản gốc Tiếng Việt & cấu hình giọng đọc"| E4
    E4 -->|"Bản dịch đa ngôn ngữ & luồng byte âm thanh MP3"| P0
```

---

## 🗺️ 3. SƠ ĐỒ DFD MỨC ĐỈNH (TOP-LEVEL DIAGRAM - LEVEL 1)

Sơ đồ DFD Mức 1 phân rã hệ thống thành **6 tiến trình nghiệp vụ cốt lõi** (`1.0` đến `6.0`) kết nối trực tiếp với 6 kho dữ liệu (`D1` đến `D6`) và 4 tác nhân ngoài (`E1` đến `E4`).

```mermaid
flowchart TB
    %% External Entities
    E1["👤 E1: Du khách"]
    E2["🛰️ E2: Hệ thống Định vị 3 Tầng"]
    E3["👨‍💼 E3: Ban Quản Lý / Admin"]
    E4["🤖 E4: Dịch vụ AI (DeepL & TTS)"]

    %% Processes
    P1(("1.0<br/>Quản lý Địa điểm,<br/>Bản đồ & Định vị POI"))
    P2(("2.0<br/>Xử lý Kích hoạt &<br/>Phát Thuyết minh<br/>Đa ngôn ngữ"))
    P3(("3.0<br/>Đóng gói Âm thanh<br/>Ngoại tuyến (ZIP)"))
    P4(("4.0<br/>Thu thập Nhật ký Nghe<br/>& Nhịp tim Thiết bị"))
    P5(("5.0<br/>Phân tích Thống kê<br/>Dashboard & Heatmap"))
    P6(("6.0<br/>Xác thực, Quản lý<br/>Ngôn ngữ & Sinh AI Audio"))

    %% Data Stores
    D1[("D1: languages<br/>(Kho Ngôn ngữ)")]
    D2[("D2: locations<br/>(Kho Địa điểm POI)")]
    D3[("D3: narrations<br/>(Kho Kịch bản)")]
    D4[("D4: playback_logs<br/>(Kho Nhật ký Nghe)")]
    D5[("D5: uploads/audio/<br/>(Kho File MP3)")]
    D6[("D6: LiveAudience<br/>(Kho Phiên Online)")]

    %% Luồng Tiến trình 1.0
    E1 -->|"Yêu cầu danh sách POI / Tra cứu QR / Sub-locations"| P1
    E2 -->|"Tọa độ GPS (lat, lng)"| P1
    D2 -->|"Dữ liệu POI, bán kính Geofence, mã QR"| P1
    D1 -->|"Danh sách ngôn ngữ hỗ trợ"| P1
    P1 -->|"Hiển thị bản đồ POI, hình người 🚶 & khoảng cách"| E1
    P1 -->|"Sự kiện chạm vùng bán kính Geofence / chọn POI"| P2

    %% Luồng Tiến trình 2.0
    E1 -->|"Chọn ngôn ngữ + Bấm ghim (MANUAL) / Quét QR"| P2
    D3 -->|"Kịch bản văn bản & tên file audio_url"| P2
    D5 -->|"Luồng dữ liệu file .mp3"| P2
    P2 -->|"Phát luồng âm thanh audio/mpeg & nội dung thuyết minh"| E1
    P2 -->|"Thông tin kích hoạt phát (GEOFENCE/MANUAL/QR_CODE)"| P4

    %% Luồng Tiến trình 3.0
    E1 -->|"Yêu cầu tải gói Offline (locationId, languageCode)"| P3
    D2 -->|"Tên địa điểm POI"| P3
    D3 -->|"Nội dung văn bản content.txt & audio_url"| P3
    D5 -->|"Tệp âm thanh gốc .mp3"| P3
    P3 -->|"Tệp nén tour-offline.zip"| E1

    %% Luồng Tiến trình 4.0
    E1 -->|"Gửi Heartbeat (JOIN/PING/LEAVE) & PlaybackLog"| P4
    P4 -->|"Cập nhật phiên thiết bị (deviceId, timestamp)"| D6
    P4 -->|"Ghi bản ghi lượt nghe mới"| D4
    D6 -->|"Số lượng khách đang online tức thời"| P4
    P4 -->|"Phản hồi HeartbeatResponse (activeUsers)"| E1

    %% Luồng Tiến trình 5.0
    E3 -->|"Yêu cầu dữ liệu Real-time Dashboard"| P5
    D4 -->|"Lịch sử nghe, thiết bị đang nghe, tọa độ Heatmap"| P5
    D2 -->|"Thông tin tên địa điểm POI"| P5
    D6 -->|"Danh sách phiên thiết bị đang trực tuyến"| P5
    P5 -->|"Số liệu KPI, máy đang nghe, Top POI & Heatmap"| E3

    %% Luồng Tiến trình 6.0
    E3 -->|"Đăng nhập / Thêm ngôn ngữ / Yêu cầu dịch & TTS"| P6
    D3 -->|"Kịch bản gốc Tiếng Việt (vi)"| P6
    P6 -->|"Gửi văn bản cần dịch & tạo giọng đọc"| E4
    E4 -->|"Văn bản đã dịch & dữ liệu âm thanh MP3"| P6
    P6 -->|"Lưu ngôn ngữ mới"| D1
    P6 -->|"Lưu bản thuyết minh mới"| D3
    P6 -->|"Ghi file .mp3 mới"| D5
    P6 -->|"Cấp JWT Token & Kết quả đồng bộ ngôn ngữ"| E3
```

---

## 🔍 4. SƠ ĐỒ DFD MỨC CHI TIẾT (DETAILED DIAGRAMS - LEVEL 2)

Để làm rõ luồng xử lý bên trong từng phân hệ của đồ án, dưới đây là các sơ đồ phân rã **DFD Mức 2** cho cả 6 tiến trình chính.

---

### 📌 4.1. DFD Mức 2 – Tiến trình 1.0: Quản lý Địa điểm, Bản đồ & Định vị POI

Phân rã chi tiết quá trình tải danh mục địa danh, phân cấp cha - con (`parent_id`), tra cứu mã QR và định vị 3 tầng kết hợp tính khoảng cách Haversine.

```mermaid
flowchart LR
    E1["👤 E1: Du khách"]
    E2["🛰️ E2: Hệ thống Định vị 3 Tầng"]
    D1[("D1: languages")]
    D2[("D2: locations")]

    P1_1(("1.1<br/>Nạp Danh mục<br/>Ngôn ngữ & POI"))
    P1_2(("1.2<br/>Tra cứu Địa điểm<br/>theo QR & Phân cấp"))
    P1_3(("1.3<br/>Định vị 3 Tầng &<br/>Vẽ Hình người 🚶"))
    P1_4(("1.4<br/>Tính Khoảng cách<br/>Haversine Geofence"))
    P2(("2.0<br/>Xử lý Phát<br/>Thuyết minh"))

    E1 -->|"Mở trang index.html"| P1_1
    D1 -->|"Danh sách ngôn ngữ"| P1_1
    D2 -->|"Toàn bộ POI (GET /api/locations/pois)"| P1_1
    P1_1 -->|"Hiển thị điểm ghim POI & vòng bán kính"| E1

    E1 -->|"Quét mã QR (?qr=...) / Xem phòng ban con"| P1_2
    D2 -->|"Chi tiết POI theo qr_code / parent_id"| P1_2
    P1_2 -->|"Thông tin địa điểm khớp mã QR"| P2

    E1 -->|"Bật định vị GPS"| P1_3
    E2 -->|"Tầng 1: GPS -> Tầng 2: Wifi/Cell -> Tầng 3: IP"| P1_3
    P1_3 -->|"Vẽ Marker hình người 🚶 & sóng radar"| E1
    P1_3 -->|"Tọa độ thực (userLat, userLng)"| P1_4

    D2 -->|"Tọa độ POI & radius_in_meters, priority"| P1_4
    P1_4 -->|"Khoảng cách <= radiusInMeters (hết Cooldown)"| P2
```

---

### 📌 4.2. DFD Mức 2 – Tiến trình 2.0: Xử lý Kích hoạt & Phát Thuyết minh Đa ngôn ngữ

Phân rã cơ chế kích hoạt thuyết minh kép (**Chế độ 1: Tự động GPS Geofencing**, **Chế độ 2: Bấm ghim bản đồ thủ công**, và **Bổ trợ: Quét mã QR**) cùng luồng phát trực tuyến (`Audio Streaming`).

```mermaid
flowchart LR
    E1["👤 E1: Du khách"]
    P1(("1.0<br/>Định vị & Bản đồ"))
    D3[("D3: narrations")]
    D5[("D5: uploads/audio/")]
    P4(("4.0<br/>Thu thập Nhật ký"))

    P2_1(("2.1<br/>Tiếp nhận Sự kiện<br/>Kích hoạt (Trigger)"))
    P2_2(("2.2<br/>Truy xuất Kịch bản<br/>theo Ngôn ngữ"))
    P2_3(("2.3<br/>Phát Luồng Âm thanh<br/>(Audio Streaming)"))

    P1 -->|"Kích hoạt tự động (GEOFENCE) / QR_CODE"| P2_1
    E1 -->|"Bấm ghim trên bản đồ (MANUAL) + Mã ngôn ngữ"| P2_1

    P2_1 -->|"Cặp (locationId, languageCode)"| P2_2
    D3 -->|"Bản ghi Narration (text_content, audio_url, duration)"| P2_2
    P2_2 -->|"Hiển thị văn bản thuyết minh & tên bài nghe"| E1

    P2_2 -->|"Yêu cầu stream file (audio_url)"| P2_3
    D5 -->|"Đọc Resource file .mp3"| P2_3
    P2_3 -->|"Luồng HTTP audio/mpeg (GET /api/packages/stream)"| E1
    P2_3 -->|"Dữ liệu phiên nghe (locationId, lang, triggerType, duration)"| P4
```

---

### 📌 4.3. DFD Mức 2 – Tiến trình 3.0: Đóng gói Âm thanh Ngoại tuyến (Offline ZIP Packaging)

Phân rã quá trình tạo gói nén động `tour-offline.zip` trực tiếp trong bộ nhớ bằng `ZipOutputStream` mà không sinh tệp rác trên đĩa cứng.

```mermaid
flowchart LR
    E1["👤 E1: Du khách"]
    D2[("D2: locations")]
    D3[("D3: narrations")]
    D5[("D5: uploads/audio/")]

    P3_1(("3.1<br/>Kiểm tra & Truy xuất<br/>Thông tin Địa điểm"))
    P3_2(("3.2<br/>Tạo Tệp Văn bản<br/>content.txt (UTF-8)"))
    P3_3(("3.3<br/>Nạp Tệp Âm thanh<br/>Thuyết minh MP3"))
    P3_4(("3.4<br/>Nén Luồng Động<br/>ZipOutputStream"))

    E1 -->|"GET /api/packages/offline?locationId & languageCode"| P3_1
    D2 -->|"Thông tin Location"| P3_1
    D3 -->|"Bản ghi Narration (text_content, audio_url)"| P3_1

    P3_1 -->|"Tên địa điểm + text_content"| P3_2
    P3_2 -->|"Entry 1: content.txt"| P3_4

    P3_1 -->|"Đường dẫn audio_url"| P3_3
    D5 -->|"Mảng byte tệp .mp3"| P3_3
    P3_3 -->|"Entry 2: file âm thanh .mp3"| P3_4

    P3_4 -->|"Trả về application/zip (tour-offline.zip)"| E1
```

---

### 📌 4.4. DFD Mức 2 – Tiến trình 4.0: Thu thập Nhật ký Nghe & Nhịp tim Thiết bị

Phân rã cơ chế giám sát phiên truy cập thời gian thực qua `LiveAudience` (`POST /api/analytics/heartbeat`) và ghi nhận lịch sử nghe qua `PlaybackLogRecorder` (`POST /api/analytics/logs`).

```mermaid
flowchart LR
    E1["👤 E1: Du khách"]
    P2(("2.0<br/>Phát Thuyết minh"))
    D2[("D2: locations")]
    D4[("D4: playback_logs")]
    D6[("D6: LiveAudience")]

    P4_1(("4.1<br/>Xử lý Nhịp tim<br/>Thiết bị (Heartbeat)"))
    P4_2(("4.2<br/>Dọn dẹp Phiên<br/>Hết hạn (> 8 giây)"))
    P4_3(("4.3<br/>Kiểm tra Hợp lệ &<br/>Ghi Nhật ký Nghe"))

    E1 -->|"POST /api/analytics/heartbeat (JOIN / PING / LEAVE)"| P4_1
    P4_1 -->|"Cập nhật lastSeen / Xóa phiên khi LEAVE"| D6
    D6 -->|"Danh sách phiên hiện hành"| P4_2
    P4_2 -->|"Loại bỏ thiết bị mất kết nối"| D6
    D6 -->|"Tổng số activeUsers"| P4_1
    P4_1 -->|"HeartbeatResponse (status, activeUsers)"| E1

    P2 -->|"Sự kiện phát âm thanh"| P4_3
    E1 -->|"POST /api/analytics/logs (PlaybackLogRequest)"| P4_3
    D2 -->|"Đối chiếu tồn tại Location"| P4_3
    P4_3 -->|"INSERT bản ghi lượt nghe mới"| D4
    P4_3 -->|"Xác nhận lưu PlaybackLog (HTTP 200)"| E1
```

---

### 📌 4.5. DFD Mức 2 – Tiến trình 5.0: Phân tích Thống kê Dashboard & Heatmap

Phân rã quá trình tổng hợp dữ liệu BI cho màn hình Quản trị (`dashboard.html`) được cập nhật tự động định kỳ mỗi 3 giây (`GET /api/analytics/dashboard`).

```mermaid
flowchart LR
    E3["👨‍💼 E3: Ban Quản Lý / Admin"]
    D2[("D2: locations")]
    D4[("D4: playback_logs")]
    D6[("D6: LiveAudience")]

    P5_1(("5.1<br/>Đếm Khách Online &<br/>Máy Đang Nghe"))
    P5_2(("5.2<br/>Tổng hợp Chỉ số KPI<br/>& Tỷ lệ Kích hoạt"))
    P5_3(("5.3<br/>Xếp hạng Top POI &<br/>Tọa độ Heatmap"))
    P5_4(("5.4<br/>Đóng gói JSON<br/>DashboardResponse"))

    E3 -->|"GET /api/analytics/dashboard (Polling 3s)"| P5_1
    D6 -->|"Số thiết bị đang hoạt động"| P5_1
    D4 -->|"Các lượt nghe gần nhất (device_id, ngôn ngữ, giây)"| P5_1

    D4 -->|"Toàn bộ bản ghi playback_logs"| P5_2
    P5_2 -->|"totalListens, avgDuration, tỷ lệ GEOFENCE/MANUAL/QR"| P5_4

    D4 -->|"Tọa độ (lat, lng) & số lượt nghe theo location_id"| P5_3
    D2 -->|"Tên địa điểm POI"| P5_3
    P5_3 -->|"Danh sách Top POI & Điểm nhiệt Heatmap"| P5_4

    P5_1 -->|"activeUsers & danh sách máy đang nghe"| P5_4
    P5_4 -->|"Hiển thị KPI, Bảng Live, Biểu đồ & Heatmap"| E3
```

---

### 📌 4.6. DFD Mức 2 – Tiến trình 6.0: Xác thực, Quản lý Ngôn ngữ & Tự động Dịch + Sinh Audio AI

Phân rã quy trình bảo mật JWT (`AuthService`), quản lý danh mục ngôn ngữ (`LanguageService`) và đường ống tự động dịch thuật văn bản tiếng Việt sang ngôn ngữ mới kết hợp tổng hợp giọng đọc AI (`LanguageOnboardingService`, `TranslationService`, `TtsService`).

```mermaid
flowchart LR
    E3["👨‍💼 E3: Ban Quản Lý / Admin"]
    E4["🤖 E4: Dịch vụ AI<br/>(DeepL & ElevenLabs/Edge-TTS)"]
    D1[("D1: languages")]
    D3[("D3: narrations")]
    D5[("D5: uploads/audio/")]

    P6_1(("6.1<br/>Xác thực Admin &<br/>Cấp phát JWT Token"))
    P6_2(("6.2<br/>Kiểm tra & Khởi tạo<br/>Ngôn ngữ Mới"))
    P6_3(("6.3<br/>Dịch Tự động Văn bản<br/>từ Tiếng Việt (MT)"))
    P6_4(("6.4<br/>Tổng hợp Giọng đọc<br/>AI (TTS) & Lưu MP3"))

    E3 -->|"POST /api/auth/login (username, password)"| P6_1
    P6_1 -->|"JWT Bearer Token"| E3

    E3 -->|"POST /api/languages/{code}/translate (kèm JWT)"| P6_2
    D1 -->|"Kiểm tra trùng mã ngôn ngữ"| P6_2
    P6_2 -->|"INSERT ngôn ngữ mới"| D1

    D3 -->|"Lấy toàn bộ kịch bản gốc Tiếng Việt (vi)"| P6_3
    P6_2 -->|"Yêu cầu dịch sang mã {code}"| P6_3
    P6_3 -->|"Văn bản gốc Tiếng Việt"| E4
    E4 -->|"Văn bản đã dịch chuẩn hóa"| P6_3
    P6_3 -->|"INSERT bản ghi Narration mới"| D3

    P6_3 -->|"Kịch bản mới cần tạo âm thanh"| P6_4
    E3 -->|"POST /api/narrations/{id}/audio"| P6_4
    P6_4 -->|"Gửi text_content & voice_id"| E4
    E4 -->|"Dữ liệu âm thanh MP3"| P6_4
    P6_4 -->|"Lưu tệp {locationId}-{code}.mp3"| D5
    P6_4 -->|"Cập nhật audio_url vào Narration"| D3
    P6_4 -->|"Trả về AddLanguageResponse / NarrationResponse"| E3
```

---

## 📊 5. BẢNG ĐẶC TẢ CHI TIẾT CÁC LUỒNG DỮ LIỆU (DATA FLOW DICTIONARY)

Bảng dưới đây đối chiếu toàn bộ các luồng dữ liệu trên DFD với giao thức vận chuyển, cấu trúc dữ liệu (DTO / Payload) và REST API Endpoint thực tế trong mã nguồn:

| Mã Luồng | Tên Luồng Dữ Liệu | Nguồn | Đích | Giao Thức / REST API Endpoint | Cấu Trúc Dữ Liệu Đi Kèm |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **DF-01** | Truy vấn danh sách POI | `E1: Du khách` | `P1.0` | `GET /api/locations/pois` | Mảng JSON `Location` (`id`, `name`, `latitude`, `longitude`, `radiusInMeters`, `priority`, `qrCode`, `imageUrl`) |
| **DF-02** | Tra cứu địa điểm qua QR | `E1: Du khách` | `P1.0` | `GET /api/locations/by-qr/{qrCode}` | Tham số đường dẫn `qrCode` -> Trả về đối tượng `Location` |
| **DF-03** | Tra cứu khu vực con | `E1: Du khách` | `P1.0` | `GET /api/locations/{id}/sub-locations` | Danh sách các phòng ban/khu vực con thuộc địa điểm cha (`parent_id`) |
| **DF-04** | Tọa độ định vị 3 tầng | `E2: Cảm biến GPS` | `P1.0` | Browser Geolocation API / IP API | `{ latitude, longitude, accuracy, tier }` |
| **DF-05** | Truy xuất bài thuyết minh | `E1: Du khách` | `P2.0` | `GET /api/narrations?locationId=&languageCode=` | `NarrationResponse` (`id`, `locationId`, `languageCode`, `textContent`, `audioUrl`, `durationInSeconds`) |
| **DF-06** | Phát trực tuyến âm thanh | `P2.0` | `E1: Du khách` | `GET /api/packages/stream?locationId=&languageCode=` | Luồng nhị phân `Resource` với Content-Type `audio/mpeg` |
| **DF-07** | Tải gói nén Offline ZIP | `P3.0` | `E1: Du khách` | `GET /api/packages/offline?locationId=&languageCode=` | Mảng `byte[]` định dạng `application/zip` (`tour-offline.zip` gồm `content.txt` + `audio.mp3`) |
| **DF-08** | Tín hiệu nhịp tim thiết bị | `E1: Du khách` | `P4.0` | `POST /api/analytics/heartbeat` | `DeviceHeartbeatRequest` (`deviceId`, `action`: `JOIN`/`PING`/`LEAVE`) -> `HeartbeatResponse` (`activeUsers`) |
| **DF-09** | Ghi nhận nhật ký lượt nghe | `E1` / `P2.0` | `P4.0` | `POST /api/analytics/logs` | `PlaybackLogRequest` (`locationId`, `languageCode`, `listenDurationSeconds`, `triggerType`, `deviceId`, `latitude`, `longitude`) |
| **DF-10** | Truy vấn số liệu Dashboard | `E3: Ban Quản Lý` | `P5.0` | `GET /api/analytics/dashboard` | `DashboardResponse` (Tổng lượt nghe, khách online, máy đang nghe, Top POI, tỷ lệ kích hoạt, tọa độ Heatmap) |
| **DF-11** | Xác thực Quản trị viên | `E3: Ban Quản Lý` | `P6.0` | `POST /api/auth/login` | `LoginRequest` (`username`, `password`) -> `LoginResponse` (`token`, `tokenType`, `expiresIn`) |
| **DF-12** | Tự động dịch & sinh Audio | `E3: Ban Quản Lý` | `P6.0` | `POST /api/languages/{code}/translate` | Mã ngôn ngữ đích `{code}` -> Dịch toàn bộ kịch bản tiếng Việt qua `E4` -> Sinh MP3 -> `AddLanguageResponse` |

---

## ✅ 6. KIỂM TRA TÍNH CÂN BẰNG & TOÀN VẸN CỦA SƠ ĐỒ DFD (DFD BALANCING & VALIDATION)

Sơ đồ DFD của đồ án đảm bảo tuân thủ nghiêm ngặt các nguyên tắc kiểm tra chất lượng trong Kỹ nghệ Phần mềm:

1. **Tính cân bằng giữa các mức (Level Balancing):** Toàn bộ các luồng dữ liệu vào/ra giữa 4 tác nhân ngoài (`E1` – `E4`) ở **DFD Mức 0 (Ngữ cảnh)** đều được bảo toàn và khớp chính xác với tổng hợp các luồng vào/ra ở **DFD Mức 1** và **DFD Mức 2**.
2. **Không có hố đen dữ liệu (No Black Holes):** Mọi tiến trình (`1.0` đến `6.0`) đều có cả luồng dữ liệu đầu vào (Input) và luồng dữ liệu đầu ra (Output) rõ ràng.
3. **Không có luồng dữ liệu tự phát (No Miracles):** Mọi đầu ra của tiến trình đều được tổng hợp hoặc biến đổi từ dữ liệu đầu vào của tác nhân hoặc truy xuất từ kho dữ liệu (`D1` – `D6`).
4. **Đúng quy tắc kết nối kho dữ liệu:** Không có kho dữ liệu nào tự truyền dữ liệu trực tiếp cho kho dữ liệu khác hoặc truyền trực tiếp cho tác nhân ngoài mà không thông qua tiến trình xử lý (`Process`).
