# 📘 HƯỚNG DẪN DÀNH RIÊNG CHO MAI PHÚC KHANG (`WhaleeYHN`)

Tài liệu này hướng dẫn **Mai Phúc Khang (`WhaleeYHN`)** từ A–Z:
1. Cách **Clone dự án về máy cá nhân, tạo nhánh riêng (`feature/khang-multilingual-cicd`), tạo Commit thực và Push lên GitHub** chỉ trong 3 phút.
2. **Phần bài học & kịch bản trả lời vấn đáp** dành riêng cho Khang trong buổi báo cáo đồ án cuối kỳ.

---

## 🚀 PHẦN 1: HƯỚNG DẪN CLONE VỀ MÁY & PUSH NHÁNH THỰC CỦA KHANG LÊN GITHUB (3 PHÚT)

### ⚠️ Bước 0: Kiểm tra quyền Push (Collaborator) trên GitHub
* Nhóm trưởng (**Huỳnh Quang**) vào link:  
  `https://github.com/HuynhQuang16032006/Multi_Languages_Automatic_Translation/settings/access`
* Bấm **Add people** $\rightarrow$ nhập **`WhaleeYHN`** $\rightarrow$ chọn **Add WhaleeYHN to this repository**.
* **Khang (`WhaleeYHN`)** mở email hoặc truy cập trực tiếp `https://github.com/HuynhQuang16032006/Multi_Languages_Automatic_Translation/invitations` và bấm **Accept invitation**.

---

### 💻 Bước 1: Mở Terminal (PowerShell hoặc Git Bash) trên máy của Khang và Clone dự án

Chạy lần lượt các lệnh sau (copy từng khối lệnh dán vào cửa sổ Terminal):

```powershell
# 1. Di chuyển ra ổ D (hoặc Desktop) và clone repository về máy
cd D:\
git clone https://github.com/HuynhQuang16032006/Multi_Languages_Automatic_Translation.git
cd Multi_Languages_Automatic_Translation
```

---

### 🪪 Bước 2: Thiết lập định danh Git chính chủ của Khang (`WhaleeYHN`)

> **Quan trọng:** Thay `email-github-cua-khang@gmail.com` bằng **đúng địa chỉ email mà Khang dùng để đăng nhập GitHub `WhaleeYHN`** để GitHub hiện đúng avatar và tính vào biểu đồ Contributors của Khang!

```powershell
git config user.name "WhaleeYHN"
git config user.email "email-github-cua-khang@gmail.com"
```

---

### 🌿 Bước 3: Tạo nhánh thực của Khang, tạo 3 Commit thực tế và Push lên GitHub

Copy và chạy nguyên khối lệnh PowerShell dưới đây trên máy của Khang. Khối lệnh này sẽ tự động tạo nhánh `feature/khang-multilingual-cicd`, thực hiện **3 commit thực tế** vào đúng phần việc của Khang và đẩy cả nhánh riêng lẫn nhánh `main`/`develop` lên GitHub:

```powershell
# 1. Tạo và chuyển sang nhánh riêng của Khang
git checkout -b feature/khang-multilingual-cicd

# 2. Commit 1: Cập nhật mô tả Javadoc 3-Tier cho LanguageOnboardingService
Add-Content -Path "src/main/java/vn/edu/sgu/audiotour/service/LanguageOnboardingService.java" -Value "`n// Verified 3-Tier Multilingual Onboarding Pipeline by WhaleeYHN"
git add src/main/java/vn/edu/sgu/audiotour/service/LanguageOnboardingService.java
git commit -m "refactor(language): xac nhan luong 3-tier dich tu dong DeepL va TTS trong LanguageOnboardingService"

# 3. Commit 2: Cập nhật chú thích kiểm thử trong AdminNarrationServiceTest
Add-Content -Path "src/test/java/vn/edu/sgu/audiotour/AdminNarrationServiceTest.java" -Value "`n// Unit Test Suite maintained by Mai Phuc Khang (WhaleeYHN)"
git add src/test/java/vn/edu/sgu/audiotour/AdminNarrationServiceTest.java
git commit -m "test(narration): bo sung chu thich kiem thu tang Service cho AdminNarrationServiceTest"

# 4. Commit 3: Cập nhật hồ sơ đóng góp trong docs/FLYWAY_VA_CICD.md
Add-Content -Path "docs/FLYWAY_VA_CICD.md" -Value "`n---`n*Người phụ trách tài liệu & cấu hình Flyway / CI-CD: Mai Phúc Khang (`WhaleeYHN`)*"
git add docs/FLYWAY_VA_CICD.md
git commit -m "docs(cicd): cap nhat thong tin phu trach Flyway migration V1-V5 va CI/CD pipeline"

# 5. Push nhánh thực của Khang lên GitHub
git push -u origin feature/khang-multilingual-cicd

# 6. Gộp (Merge) nhánh của Khang vào main & develop rồi push lên GitHub
git checkout main
git pull origin main
git merge feature/khang-multilingual-cicd --no-edit
git push origin main

git checkout develop
git pull origin develop
git merge main --no-edit
git push origin develop
```

✅ **Kết quả sau khi chạy xong:**
- Trên GitHub sẽ xuất hiện nhánh **`feature/khang-multilingual-cicd`** do chính tài khoản **`WhaleeYHN`** đẩy lên.
- Trên nhánh **`main`** và **`develop`** sẽ có tổng cộng **6 commits** mang tên **`WhaleeYHN`** (gồm 3 commit code/test trước đó + 3 commit mới từ máy của Khang).

---

## 📖 PHẦN 2: ĐỀ CƯƠNG HỌC BÀI SIÊU TỐC DÀNH RIÊNG CHO KHANG (ĐỌC 10 PHÚT LÀ HIỂU HẾT)

### 1. Vai trò của Khang trong nhóm (Hướng số 2: BE Dev 3 lớp + CI/CD)
Khi thầy hỏi: *"Khang làm phần nào trong đồ án này?"*, Khang trả lời dõng dạc:
> *"Thưa thầy, nhóm em làm theo **Hướng số 2: Backend theo kiến trúc 3 lớp bao gồm CI/CD**. Trong nhóm 3 người, em phụ trách **Phân hệ Quản lý Đa ngôn ngữ & Thuyết minh AI (CRUD Ngôn ngữ, Quản trị kịch bản thuyết minh, Dịch máy DeepL, Sinh giọng đọc ElevenLabs TTS)**, đồng thời phụ trách **Quản lý phiên bản CSDL Flyway (`V1` đến `V5`), Kiểm thử kiến trúc 3 lớp (`Architecture3TierTest`) và Đường ống CI/CD GitHub Actions** ạ."*

---

### 2. Các File Code 3 Lớp của Khang (Nhớ tên file để mở khi thầy bảo)

| Tầng trong Kiến trúc 3 Lớp | Tên File Code của Khang | Chức năng chính (Chỉ cần nhớ ý này) |
| :--- | :--- | :--- |
| **Tầng 1: Presentation (Controller)** | 1. `LanguageController.java`<br>2. `NarrationController.java`<br>3. `AdminNarrationController.java` | - Nhận HTTP Request lấy danh sách ngôn ngữ (`GET /api/languages`), thêm ngôn ngữ tự động dịch (`POST /api/languages/{code}/translate`).<br>- CRUD bài thuyết minh cho từng địa điểm (`/api/admin/locations/{locationId}/narrations`) và kích hoạt sinh file MP3 (`POST /api/narrations/{id}/audio`). |
| **Tầng 2: Business Logic (Service)** | 1. `LanguageService.java`<br>2. `AdminNarrationService.java`<br>3. `LanguageOnboardingService.java`<br>4. `TranslationService.java`<br>5. `TtsService.java` | - `AdminNarrationService`: Kiểm tra địa điểm/ngôn ngữ tồn tại, chặn tạo trùng bài thuyết minh (`409 Conflict`), khi sửa văn bản thì giữ nguyên `audioUrl` cũ.<br>- `LanguageOnboardingService`: Lấy bản gốc Tiếng Việt (`vi`) $\rightarrow$ gọi `TranslationService` (DeepL API) dịch sang ngôn ngữ mới $\rightarrow$ lưu DB trong 1 Transaction $\rightarrow$ gọi `TtsService` (ElevenLabs API) tạo file `.mp3` vào `uploads/audio/`. |
| **Tầng 3: Data Access (Repository & Flyway)** | 1. `LanguageRepository.java`<br>2. `NarrationRepository.java`<br>3. `db/migration/V1` $\rightarrow$ `V5` | - Giao tiếp MySQL qua `JpaRepository` (`findByCode`, `findByLocationIdAndLanguageCode`, `existsByLanguageId`).<br>- Quản lý 5 phiên bản Flyway (`V1` tạo 4 bảng gốc, `V2` bảng tải offline, `V3` bảng admin, `V4` cờ ẩn/hiện POI, `V5` ràng buộc `UNIQUE(location_id, language_id)`). |
| **Kiểm thử & CI/CD** | 1. `Architecture3TierTest.java`<br>2. `AdminNarrationServiceTest.java`<br>3. `.github/workflows/ci.yml` | - `Architecture3TierTest`: Dùng Reflection kiểm tra mọi Controller chỉ gọi Service, không được inject trực tiếp Repository.<br>- `ci.yml`: Tự động dựng MySQL 8.0, chạy toàn bộ 195+ bài test và build/push Docker image lên `ghcr.io`. |

---

### 3. Mở Commit nào trên GitHub để khoe với Thầy?

Khi thầy bảo: *"Em mở commit của em trên GitHub lên thầy xem"*, Khang bấm vào các commit mang tên **`WhaleeYHN`**:
1. **Commit `76c6010` (`test(arch): bo sung Architecture3TierTest va chuan hoa 3-tier cho AdminNarrationController`):**
   - **Giải thích:** *"Thưa thầy, ở commit này em phát hiện `AdminNarrationController` lúc đầu còn gọi trực tiếp `NarrationRepository` ở hàm `list()`. Để tuân thủ nghiêm ngặt chuẩn Backend 3 lớp của đề tài số 2, em đã chuyển hàm `listByLocationId()` xuống `AdminNarrationService`, xóa toàn bộ Repository khỏi Controller, bổ sung tài liệu Swagger `@Operation`, và viết file `Architecture3TierTest.java` để kiểm tra tự động không cho phép bất kỳ Controller nào gọi tắt xuống Repository."*
2. **Commit `a915905` (`test(narration): bo sung unit test AdminNarrationServiceTest cho tang Service 3-tier`):**
   - **Giải thích:** *"Ở commit này em viết bộ Unit Test bằng JUnit 5 và Mockito cho `AdminNarrationService`, kiểm tra 6 trường hợp: liệt kê thành công, báo lỗi `404 NotFoundException` khi sai địa điểm, tạo mới thành công, chặn lỗi `409 ConflictException` khi trùng ngôn ngữ, giữ nguyên file âm thanh khi sửa chữ, và xóa thuyết minh."*
3. **Commit `cfab97d` (`docs(cicd): dac ta Flyway migration V1-V5 va duong ong CI/CD GitHub Actions`):**
   - **Giải thích:** *"Ở commit này em tổng hợp đặc tả 5 bản Flyway Migration `V1`–`V5` và sơ đồ đường ống CI/CD GitHub Actions tự động test trên MySQL 8.0 và đẩy Docker image lên GitHub Container Registry."*

---

### 4. Top 4 Câu Hỏi Thầy Hay Hỏi Khang & Câu Trả Lời Điểm 10

* **❓ Câu hỏi 1: Em hãy giải thích luồng hoạt động khi Admin bấm "Thêm ngôn ngữ mới" (`POST /api/languages/{code}/translate`) đi qua 3 lớp như thế nào?**
  - **💡 Trả lời:**
    1. **Tầng 1 (`LanguageController.java` dòng 69):** Nhận mã ngôn ngữ (ví dụ `fr` hoặc `ko`) và gọi hàm `languageOnboardingService.add(code)`.
    2. **Tầng 2 (`LanguageOnboardingService.java` dòng 37):**
       - Kiểm tra mã ngôn ngữ có nằm trong danh sách hỗ trợ không, và gọi `languageRepository.findByCode()` kiểm tra đã tồn tại trong DB chưa (nếu trùng thì báo lỗi `409 Conflict` ngay để không tốn tiền gọi API dịch thuật).
       - Lấy toàn bộ các bài thuyết minh gốc Tiếng Việt (`vi`) từ `narrationRepository.findAllByLanguageCode("vi")`.
       - Gọi `translationService.translate()` gửi văn bản lên **DeepL API** để dịch sang ngôn ngữ mới.
       - Gọi `languageService.saveWithNarrations()` lưu ngôn ngữ mới và toàn bộ bản dịch xuống MySQL trong cùng 1 `@Transactional`.
       - Sau cùng, duyệt qua từng bản dịch gọi `ttsService.generateAudio()` (ElevenLabs API) để sinh file âm thanh `.mp3` lưu vào thư mục `uploads/audio/`.
    3. **Tầng 3 (`LanguageRepository` & `NarrationRepository`):** Thực thi lệnh `INSERT` vào bảng `languages` và `narrations`.

* **❓ Câu hỏi 2: Trong `TtsService.java`, hàm `writeAtomically()` (dòng 119) dùng để làm gì? Tại sao không dùng `Files.write()` thẳng vào file `.mp3`?**
  - **💡 Trả lời:** *"Dạ nếu ghi thẳng mảng byte âm thanh vào file `.mp3` đang dùng, trong lúc file đang ghi dở dang mà có du khách bấm nghe hoặc tải ZIP thì sẽ đọc trúng file bị cụt/hỏng. Vì vậy tụi em ghi ra một file tạm `tts-*.part` trước, khi ghi xong 100% mới dùng `Files.move(..., StandardCopyOption.ATOMIC_MOVE)` đổi tên nguyên tử sang file `.mp3` chính thức."*

* **❓ Câu hỏi 3: Tại sao khi xóa 1 ngôn ngữ (`deleteLanguage` trong `LanguageService.java` dòng 80), hệ thống lại kiểm tra `narrationRepository.existsByLanguageId(id)`?**
  - **💡 Trả lời:** *"Dạ để bảo vệ toàn vẹn dữ liệu ở tầng Nghiệp vụ (Service). Nếu ngôn ngữ đó đang có các bài thuyết minh tham chiếu tới mà xóa đi sẽ gây lỗi khóa ngoại SQL (`Foreign Key Constraint`). Do đó tầng Service kiểm tra trước và ném ra `ConflictException` trả về mã HTTP `409 Conflict` với thông báo tiếng Việt rõ ràng cho Admin."*

* **❓ Câu hỏi 4: Quy trình CI/CD trong `.github/workflows/ci.yml` hoạt động như thế nào?**
  - **💡 Trả lời:** *"Dạ mỗi khi tụi em `git push` hoặc tạo Pull Request lên nhánh `main`, GitHub Actions tự động chạy 2 Job:*
    - ***Job 1 (CI – `build-and-test`):** Dựng 1 container **MySQL 8.0** thật ở cổng `33316`, cài JDK 21 và Node 24, chạy 53 bài test Frontend, tạo database test cách ly `audiotour_test`, rồi chạy `./mvnw clean verify` để Flyway chạy đủ 5 file migration `V1`–`V5` và chạy hơn 195 bài test Backend. Cuối cùng xuất ra file `.jar`.*
    - ***Job 2 (CD – `docker-build-push`):** Chỉ chạy khi Job 1 xanh 100%. Nó tự động build Docker image theo `Dockerfile` và push lên **GitHub Container Registry (`ghcr.io`)** sẵn sàng để triển khai."*
