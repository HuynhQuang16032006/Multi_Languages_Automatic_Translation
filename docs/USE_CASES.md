# TÀI LIỆU ĐẶC TẢ CA SỬ DỤNG KÈM BIỂU ĐỒ CHI TIẾT (USE CASE SPECIFICATIONS & DIAGRAMS)

---

## 🏛️ THÔNG TIN TỔNG QUAN

* **Hệ thống:** Hệ thống Thuyết minh Audio Tour Tự Động Đa Ngôn Ngữ (*Multi Languages Automatic Translation*)
* **Môn học:** Công nghệ Phần mềm (Software Engineering) – Khoa CNTT, Trường Đại học Sài Gòn (SGU)
* **Quy chuẩn đặc tả:** Chuẩn RUP (Rational Unified Process) kết hợp UML 2.5 với **biểu đồ tuần tự (Sequence Diagram) và biểu đồ hoạt động (Activity Diagram) chi tiết cho từng Use Case**.

---

## 📐 1. SƠ ĐỒ CA SỬ DỤNG TỔNG THỂ & PHÂN RÃ PHÂN HỆ

### 1.1. Sơ đồ Ca Sử Dụng Tổng Thể (System Use Case Diagram)

```mermaid
graph LR
    actorTourist((Du khách))
    actorAdmin((Ban Quản Lý))
    actorDevice((Thiết bị / GPS))

    subgraph AudioTourSystem [Hệ thống Audio Tour Thông Minh]
        UC01(UC-01: Đồng bộ danh sách POI Geofencing)
        UC02(UC-02: Tự động phát thuyết minh GPS ngoài trời)
        UC03(UC-03: Quét mã QR nhận thông tin địa điểm)
        UC04(UC-04: Tải trọn gói âm thanh Offline ZIP)
        UC05(UC-05: Ghi nhận nhật ký nghe thuyết minh)
        UC06(UC-06: Xem báo cáo thống kê Dashboard & Heatmap)
        UC07(UC-07: Xem lộ trình tuyến Tour tham quan)
        UC08(UC-08: Tra cứu khu vực con bên trong di tích)
    end

    actorTourist --> UC01
    actorTourist --> UC03
    actorTourist --> UC04
    actorTourist --> UC07
    actorTourist --> UC08

    actorDevice --> UC02
    actorDevice --> UC05

    actorAdmin --> UC06
    actorAdmin --> UC07

    UC02 -.->|<<include>>| UC05
    UC03 -.->|<<extend>>| UC04
    UC03 -.->|<<extend>>| UC08
```

---

### 1.2. Phân rã theo 3 Phân hệ Chức năng (Sub-systems)

```mermaid
graph TD
    subgraph Sub1 [Phân hệ 1: Quản lý Vị trí & Định vị Geofencing]
        UC01_sub[UC-01: Đồng bộ POI Geofencing]
        UC02_sub[UC-02: Tự động phát GPS ngoài trời]
        UC08_sub[UC-08: Tra cứu phòng ban con]
    end

    subgraph Sub2 [Phân hệ 2: Kích hoạt QR Code & Tải Offline]
        UC03_sub[UC-03: Quét mã QR địa điểm]
        UC04_sub[UC-04: Đóng gói ZIP âm thanh Offline]
    end

    subgraph Sub3 [Phân hệ 3: Phân tích Dữ liệu & Tuyến Tour]
        UC05_sub[UC-05: Ghi nhận nhật ký nghe]
        UC06_sub[UC-06: Báo cáo Dashboard & Heatmap]
        UC07_sub[UC-07: Xem lộ trình tuyến Tour]
    end
```

---

## 👥 2. DANH SÁCH TÁC NHÂN (ACTORS)

| Tác nhân | Loại tác nhân | Vai trò và Trách nhiệm trong hệ thống |
| :--- | :---: | :--- |
| **Du khách (Tourist)** | Primary Actor | Người dùng cuối trực tiếp tương tác với ứng dụng di động: quét mã QR, chọn ngôn ngữ nghe, tải gói offline và trải nghiệm tham quan. |
| **Thiết bị / Cảm biến GPS (Mobile Sensor)** | System Actor | Hệ thống cảm biến vị trí trên thiết bị di động, tự động đối chiếu tọa độ người dùng với các POI và phát tín hiệu kích hoạt âm thanh ngầm. |
| **Ban Quản Lý (Admin / Tour Operator)** | Primary Actor | Người quản trị truy cập Web CMS để theo dõi mật độ du khách, bản đồ nhiệt Heatmap, báo cáo số liệu và thiết lập các tuyến Tour. |

---

## 📋 3. DANH MỤC TỔNG HỢP CA SỬ DỤNG

| Mã UC | Tên Ca Sử Dụng | Tác nhân chính | Mức độ ưu tiên | Endpoint API Backend |
| :---: | :--- | :---: | :---: | :--- |
| **UC-01** | Đồng bộ danh sách POI Geofencing | Du khách / Mobile App | **Cao (High)** | `GET /api/locations/pois` |
| **UC-02** | Tự động phát thuyết minh GPS ngoài trời | Thiết bị / GPS Sensor | **Cao (High)** | Logic Mobile + `POST /api/analytics/logs` |
| **UC-03** | Quét mã QR nhận thông tin địa điểm | Du khách | **Cao (High)** | `GET /api/locations/by-qr/{qrCode}` |
| **UC-04** | Tải trọn gói âm thanh Offline dạng ZIP | Du khách | **Cao (High)** | `GET /api/packages/offline` |
| **UC-05** | Ghi nhận nhật ký nghe thuyết minh | Thiết bị Mobile App | **Trung bình (Medium)** | `POST /api/analytics/logs` |
| **UC-06** | Xem thống kê Dashboard & Heatmap | Ban Quản Lý | **Trung bình (Medium)** | `GET /api/analytics/dashboard` |
| **UC-07** | Xem danh sách và lộ trình tuyến Tour | Du khách / Ban Quản Lý | **Thấp (Low)** | `GET /api/tours`, `GET /api/tours/{id}` |
| **UC-08** | Tra cứu khu vực con bên trong di tích | Du khách | **Thấp (Low)** | `GET /api/locations/{id}/sub-locations` |

---

## 📝 4. ĐẶC TẢ CHI TIẾT TỪNG USE CASE KÈM BIỂU ĐỒ

---

### 🔹 UC-01: Đồng bộ danh sách POI Geofencing

#### 1. Mục đích
Cung cấp toàn bộ danh sách điểm quan tâm (POI) gồm tọa độ GPS, bán kính kích hoạt và độ ưu tiên về ứng dụng di động để hỗ trợ phát hiện vị trí ngoại tuyến.

#### 2. Thông tin chung
* **Tác nhân:** Du khách (thực thi tự động qua ứng dụng di động khi khởi động).
* **Tiền điều kiện:** Ứng dụng di động có kết nối Internet khi mở lần đầu.
* **Hậu điều kiện:** Dữ liệu POI được lưu trữ cục bộ trong bộ nhớ cache hoặc SQLite của điện thoại.

#### 3. Biểu đồ Tuần tự (Sequence Diagram - UC-01)

```mermaid
sequenceDiagram
    autonumber
    actor Tourist as Du khách (Mobile App)
    participant LC as LocationController
    participant LS as LocationService
    participant LR as LocationRepository
    participant DB as MySQL Database

    Tourist->>LC: GET /api/locations/pois
    activate LC
    LC->>LS: getAllLocations()
    activate LS
    LS->>LR: findAll()
    activate LR
    LR->>DB: SELECT * FROM locations
    activate DB
    DB-->>LR: Danh sách tất cả POI (lat, lng, radius, priority)
    deactivate DB
    LR-->>LS: List<Location>
    deactivate LR
    LS-->>LC: List<Location>
    deactivate LS
    LC-->>Tourist: HTTP 200 OK (JSON List POI)
    deactivate LC
    Note over Tourist: Mobile lưu danh sách vào SQLite phục vụ Geofencing Offline
```

#### 4. Luồng sự kiện
* **Luồng chính:**
  1. Ứng dụng khởi động, tự động gửi `GET /api/locations/pois`.
  2. `LocationController` gọi `LocationService.getAllLocations()`.
  3. `LocationRepository` truy vấn CSDL MySQL nạp danh sách địa điểm.
  4. Hệ thống trả về mảng JSON chứa các trường `id`, `name`, `latitude`, `longitude`, `radiusInMeters`, `priority`.
  5. Ứng dụng nạp dữ liệu vào Geofencing Service chạy nền.
* **Luồng ngoại lệ:** Nếu mất mạng, ứng dụng sử dụng dữ liệu đã lưu từ phiên trước trong SQLite.

---

### 🔹 UC-02: Tự động phát thuyết minh GPS ngoài trời (Geofence Trigger)

#### 1. Mục đích
Tự động kích hoạt bài thuyết minh khi du khách bước vào bán kính vùng phủ sóng định vị của một địa điểm (ví dụ: xe buýt tới trạm Khánh Hội).

#### 2. Thông tin chung
* **Tác nhân:** Thiết bị di động / Cảm biến GPS.
* **Tiền điều kiện:** Đã hoàn thành UC-01; thiết bị đã cấp quyền vị trí (Location Permission).
* **Hậu điều kiện:** Âm thanh thuyết minh tự động phát; gọi tự động **UC-05** ghi nhận log.

#### 3. Biểu đồ Hoạt động (Activity Diagram - UC-02)

```mermaid
flowchart TD
    Start([Bắt đầu di chuyển]) --> ReadGPS[Đọc tọa độ GPS hiện tại từ thiết bị]
    ReadGPS --> CalcDist[Tính khoảng cách Haversine tới các POI]
    CalcDist --> CheckRadius{Khoảng cách <= Bán kính radiusInMeters?}
    
    CheckRadius -- Không --> Wait[Chờ chu kỳ quét GPS tiếp theo]
    Wait --> ReadGPS
    
    CheckRadius -- Có --> CheckConflict{Có nhiều hơn 1 POI thỏa mãn?}
    CheckConflict -- Có --> SelectPriority[Chọn POI có thuộc tính priority cao nhất]
    CheckConflict -- Không --> TargetPOI[Xác định POI kích hoạt]
    SelectPriority --> TargetPOI
    
    TargetPOI --> PlayAudio[Tự động phát âm thanh thuyết minh]
    PlayAudio --> ListenFinish[Du khách nghe xong bài thuyết minh]
    ListenFinish --> TriggerLog[Gọi UC-05: Gửi log phát về máy chủ]
    TriggerLog --> End([Kết thúc chu trình])
```

#### 4. Biểu đồ Tuần tự (Sequence Diagram - UC-02)

```mermaid
sequenceDiagram
    autonumber
    actor GPS as Cảm biến GPS
    participant App as Mobile App Logic
    participant Storage as SQLite / Audio Local
    participant AC as AnalyticsController
    participant DB as MySQL Database

    loop Mỗi chu kỳ định vị (5 giây)
        GPS->>App: Cung cấp tọa độ (lat, lng)
        App->>App: Tính khoảng cách tới POIs
    end
    Note over App: Khoảng cách <= radiusInMeters & Priority cao nhất!
    App->>Storage: Lấy file âm thanh bài thuyết minh
    Storage-->>App: Stream âm thanh MP3
    App->>App: Tự động phát âm thanh ra loa/tai nghe
    Note over App: Du khách nghe hết bài thuyết minh
    App->>AC: POST /api/analytics/logs (triggerType = GEOFENCE)
    activate AC
    AC->>DB: Lưu nhật ký nghe vào playback_logs
    AC-->>App: HTTP 200 OK
    deactivate AC
```

---

### 🔹 UC-03: Quét mã QR nhận thông tin địa điểm

#### 1. Mục đích
Cho phép du khách quét mã QR dán cố định tại trạm dừng hoặc phòng ban di tích để lấy thông tin chi tiết và danh sách bài thuyết minh.

#### 2. Thông tin chung
* **Tác nhân:** Du khách.
* **Tiền điều kiện:** Du khách đứng trước mã QR và cấp quyền Camera.
* **Hậu điều kiện:** Thông tin địa điểm hiển thị trên màn hình.

#### 3. Biểu đồ Tuần tự (Sequence Diagram - UC-03)

```mermaid
sequenceDiagram
    autonumber
    actor Tourist as Du khách
    participant Camera as Camera Mobile
    participant App as Mobile App
    participant LC as LocationController
    participant LS as LocationService
    participant LR as LocationRepository
    participant DB as MySQL Database

    Tourist->>Camera: Quét mã QR tại điểm tham quan (ví dụ: QR-DINH-DOC-LAP-01)
    Camera-->>App: Trả về chuỗi mã "QR-DINH-DOC-LAP-01"
    App->>LC: GET /api/locations/by-qr/QR-DINH-DOC-LAP-01
    activate LC
    LC->>LS: getLocationByQrCode("QR-DINH-DOC-LAP-01")
    activate LS
    LS->>LR: findByQrCode("QR-DINH-DOC-LAP-01")
    activate LR
    LR->>DB: SELECT * FROM locations WHERE qr_code = ?
    activate DB
    DB-->>LR: Bản ghi Location (kèm subLocations)
    deactivate DB
    LR-->>LS: Optional<Location>
    deactivate LR
    
    alt Tìm thấy mã QR
        LS-->>LC: Đối tượng Location
        LC-->>App: HTTP 200 OK (Chi tiết địa danh, ảnh, phòng con)
        App-->>Tourist: Hiển thị giao diện di tích & nút chọn ngôn ngữ
    else Không tìm thấy mã QR
        LS-->>LC: Ném ngoại lệ 404 Not Found
        LC-->>App: HTTP 404 (Mã QR không tồn tại)
        App-->>Tourist: Báo lỗi "Mã QR không hợp lệ"
    end
    deactivate LS
    deactivate LC
```

---

### 🔹 UC-04: Tải trọn gói âm thanh Offline dạng ZIP

#### 1. Mục đích
Nén động file âm thanh MP3 thực tế và file văn bản kịch bản `content.txt` thành gói `tour-offline.zip` để du khách lưu về máy và nghe ngoại tuyến khi không có mạng.

#### 2. Thông tin chung
* **Tác nhân:** Du khách.
* **Tiền điều kiện:** Du khách đang ở trang chi tiết địa danh (hoàn thành UC-03).
* **Hậu điều kiện:** File ZIP được tải về và giải nén trong máy khách.

#### 3. Biểu đồ Hoạt động Nén Luồng I/O (Activity Diagram - UC-04)

```mermaid
flowchart TD
    Start([Du khách bấm Tải Offline]) --> CheckParam{Kiểm tra locationId và languageCode}
    CheckParam -- Thiếu/Sai --> Return404[Trả về lỗi 404 Not Found]
    
    CheckParam -- Hợp lệ --> QueryDB[Truy vấn Narration & File MP3 tương ứng]
    QueryDB --> OpenStream[Khởi tạo ByteArrayOutputStream & ZipOutputStream]
    
    OpenStream --> AddText[Tạo entry content.txt: Ghi nội dung kịch bản văn bản]
    AddText --> FindMP3{File MP3 có tồn tại trong uploads/audio?}
    
    FindMP3 -- Có --> AddMP3[Tạo entry audio.mp3: Đọc file vật lý nạp vào ZipEntry]
    FindMP3 -- Không --> FallbackAudio[Bỏ qua hoặc nén file văn bản cảnh báo]
    
    AddMP3 --> CloseZip[Đóng ZipOutputStream và xuất mảng byte]
    FallbackAudio --> CloseZip
    CloseZip --> SetHeaders[Thiết lập Header: application/zip, Content-Disposition: attachment]
    SetHeaders --> SendResponse[Trả về luồng byte ZIP cho Client]
    SendResponse --> End([Mobile giải nén và lưu trữ Offline])
```

#### 4. Biểu đồ Tuần tự (Sequence Diagram - UC-04)

```mermaid
sequenceDiagram
    autonumber
    actor Tourist as Du khách (Mobile App)
    participant APC as AudioPackageController
    participant APS as AudioPackageService
    participant NR as NarrationRepository
    participant FS as Local File Storage (uploads/audio)

    Tourist->>APC: GET /api/packages/offline?locationId=4&languageCode=vi
    activate APC
    APC->>APS: createOfflinePackage(4L, "vi")
    activate APS
    APS->>NR: findByLocationIdAndLanguageCode(4L, "vi")
    activate NR
    NR-->>APS: Đối tượng Narration (text, audio_url = "dinh-doc-lap-vi.mp3")
    deactivate NR
    APS->>FS: Đọc file âm thanh "uploads/audio/dinh-doc-lap-vi.mp3"
    FS-->>APS: Binary stream của file MP3
    APS->>APS: Khởi tạo ZipOutputStream đóng gói content.txt + MP3
    APS-->>APC: byte[] (Mảng byte nén định dạng ZIP chuẩn 0x50 0x4B)
    deactivate APS
    APC-->>Tourist: HTTP 200 OK (Content-Type: application/zip, filename="tour-offline.zip")
    deactivate APC
    Note over Tourist: Du khách giải nén cục bộ và có thể nghe khi mất mạng hoàn toàn
```

---

### 🔹 UC-05: Ghi nhận nhật ký nghe thuyết minh

#### 1. Mục đích
Ghi nhận ẩn danh các lượt nghe từ ứng dụng di động để đánh giá mức độ hấp dẫn của nội dung và vẽ bản đồ nhiệt Heatmap.

#### 2. Biểu đồ Tuần tự (Sequence Diagram - UC-05)

```mermaid
sequenceDiagram
    autonumber
    actor App as Mobile App
    participant AC as AnalyticsController
    participant AS as AnalyticsService
    participant LR as LocationRepository
    participant PLR as PlaybackLogRepository
    participant DB as MySQL Database

    Note over App: Du khách nghe thuyết minh xong (hoặc đạt ngưỡng nghe tối thiểu)
    App->>AC: POST /api/analytics/logs (PlaybackLogRequest JSON)
    activate AC
    AC->>AS: recordPlaybackLog(request)
    activate AS
    AS->>LR: findById(request.locationId)
    activate LR
    LR-->>AS: Location
    deactivate LR
    AS->>AS: Tạo thực thể PlaybackLog (gán location, deviceId, triggerType, lat, lng)
    AS->>PLR: save(playbackLog)
    activate PLR
    PLR->>DB: INSERT INTO playback_logs VALUES (...)
    activate DB
    DB-->>PLR: Saved PlaybackLog
    deactivate DB
    PLR-->>AS: PlaybackLog entity
    deactivate PLR
    AS-->>AC: PlaybackLog entity
    deactivate AS
    AC-->>App: HTTP 200 OK (Chi tiết Log đã lưu)
    deactivate AC
```

---

### 🔹 UC-06: Xem báo cáo thống kê Dashboard & Heatmap

#### 1. Mục đích
Cung cấp bảng số liệu Business Intelligence tổng thể cho Ban Quản Lý di tích/du lịch (Tổng lượt nghe, thời gian nghe TB, tỷ lệ QR vs GPS, Top 5 POI và tọa độ Heatmap).

#### 2. Biểu đồ Tuần tự (Sequence Diagram - UC-06)

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Ban Quản Lý (Web CMS)
    participant AC as AnalyticsController
    participant AS as AnalyticsService
    participant PLR as PlaybackLogRepository
    participant DB as MySQL Database

    Admin->>AC: GET /api/analytics/dashboard
    activate AC
    AC->>AS: getDashboardData()
    activate AS
    
    par Truy vấn tổng lượt nghe
        AS->>PLR: count()
        PLR->>DB: SELECT COUNT(*) FROM playback_logs
        DB-->>PLR: totalListens
    and Truy vấn thời gian trung bình
        AS->>PLR: getAverageListenDuration()
        PLR->>DB: SELECT AVG(duration) FROM playback_logs
        DB-->>PLR: avgDuration
    and Phân loại theo hình thức kích hoạt
        AS->>PLR: countListensByTriggerType()
        PLR->>DB: SELECT trigger_type, COUNT(*) GROUP BY trigger_type
        DB-->>PLR: List<TriggerCount>
    and Xếp hạng Top 5 POI
        AS->>PLR: getTopListenedLocations(PageRequest.of(0, 5))
        PLR->>DB: SELECT loc_id, loc_name, COUNT(*) GROUP BY loc_id ORDER BY COUNT(*) DESC LIMIT 5
        DB-->>PLR: List<TopLocation>
    and Gom nhóm tọa độ Heatmap
        AS->>PLR: getHeatmapPoints()
        PLR->>DB: SELECT lat, lng, COUNT(*) GROUP BY lat, lng
        DB-->>PLR: List<HeatmapPoint>
    end

    AS->>AS: Đóng gói thành DashboardResponse DTO
    AS-->>AC: DashboardResponse
    deactivate AS
    AC-->>Admin: HTTP 200 OK (JSON Dashboard Data)
    deactivate AC
    Note over Admin: Web CMS vẽ biểu đồ tròn, bảng xếp hạng và bản đồ nhiệt Google Maps
```

---

### 🔹 UC-07: Xem danh sách và lộ trình tuyến Tour tham quan

#### 1. Mục đích
Cung cấp lộ trình tham quan được quy hoạch sẵn theo thứ tự địa danh cho du khách (ví dụ: Tuyến xe buýt lịch sử Quận 4, Tuyến khám phá Dinh Độc Lập).

#### 2. Biểu đồ Tuần tự (Sequence Diagram - UC-07)

```mermaid
sequenceDiagram
    autonumber
    actor User as Du khách / Quản trị
    participant TC as TourController
    participant TS as TourService
    participant TR as TourRepository
    participant DB as MySQL Database

    User->>TC: GET /api/tours/{id} (ví dụ: id = 1)
    activate TC
    TC->>TS: getTourById(1L)
    activate TS
    TS->>TR: findById(1L)
    activate TR
    TR->>DB: SELECT * FROM tours JOIN tour_locations ON ... WHERE id = 1
    activate DB
    DB-->>TR: Dữ liệu Tour kèm danh sách điểm dừng (locations)
    deactivate DB
    TR-->>TS: Optional<Tour>
    deactivate TR
    TS-->>TC: Tour Entity
    deactivate TS
    TC-->>User: HTTP 200 OK (Chi tiết tour: tên, mô tả, danh sách điểm dừng theo lộ trình)
    deactivate TC
```

---

### 🔹 UC-08: Tra cứu khu vực con bên trong di tích (Hierarchical POIs)

#### 1. Mục đích
Cho phép du khách xem danh sách các phòng ban/khu vực con bên trong một di tích cấp cao (ví dụ: xem Phòng Khánh Tiết, Phòng Đại Yến bên trong Dinh Độc Lập).

#### 2. Biểu đồ Tuần tự (Sequence Diagram - UC-08)

```mermaid
sequenceDiagram
    autonumber
    actor Tourist as Du khách
    participant LC as LocationController
    participant LS as LocationService
    participant LR as LocationRepository
    participant DB as MySQL Database

    Tourist->>LC: GET /api/locations/4/sub-locations (Dinh Độc Lập ID = 4)
    activate LC
    LC->>LS: getSubLocations(4L)
    activate LS
    LS->>LR: findByParentLocationId(4L)
    activate LR
    LR->>DB: SELECT * FROM locations WHERE parent_id = 4
    activate DB
    DB-->>LR: Danh sách các phòng ban con (Phòng Khánh Tiết, Phòng Đại Yến)
    deactivate DB
    LR-->>LS: List<Location>
    deactivate LR
    LS-->>LC: List<Location>
    deactivate LS
    LC-->>Tourist: HTTP 200 OK (Danh sách phòng con)
    deactivate LC
    Note over Tourist: Du khách bấm chọn từng phòng để nghe bài thuyết minh riêng của phòng đó
```

---

## 📌 5. MA TRẬN TRUY VẾT YÊU CẦU TOÀN DIỆN (REQUIREMENTS TRACEABILITY MATRIX - RTM)

| Mã UC | Tên Ca Sử Dụng | Loại Biểu Đồ Minh Họa | Lớp Controller | Lớp Service | Lớp Repository | Bảng CSDL |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- |
| **UC-01** | Đồng bộ POI Geofencing | Sequence Diagram | `LocationController` | `LocationService` | `LocationRepository` | `locations` |
| **UC-02** | Thuyết minh GPS tự động | Activity + Sequence Diagram | Mobile App logic | `AnalyticsService` | `PlaybackLogRepository` | `playback_logs` |
| **UC-03** | Quét mã QR địa điểm | Sequence Diagram | `LocationController` | `LocationService` | `LocationRepository` | `locations` |
| **UC-04** | Tải trọn gói Offline ZIP | Activity + Sequence Diagram | `AudioPackageController` | `AudioPackageService` | `NarrationRepository` | `narrations`, `languages` |
| **UC-05** | Ghi nhận log nghe | Sequence Diagram | `AnalyticsController` | `AnalyticsService` | `PlaybackLogRepository` | `playback_logs` |
| **UC-06** | Dashboard & Heatmap | Sequence Diagram (Parallel) | `AnalyticsController` | `AnalyticsService` | `PlaybackLogRepository` | `playback_logs` |
| **UC-07** | Lộ trình tuyến Tour | Sequence Diagram | `TourController` | `TourService` | `TourRepository` | `tours`, `tour_locations` |
| **UC-08** | Tra cứu phòng ban con | Sequence Diagram | `LocationController` | `LocationService` | `LocationRepository` | `locations` |
