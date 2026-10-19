# Cấu trúc dự án

Ảnh chụp cây thư mục để đối chiếu kiến trúc 3 lớp.
Sinh bằng PowerShell, quét toàn bộ repo. Đã loại: `.git/`, `target/`, `.idea/`.

- Gốc: `D:\Project\Multi_Languages_Automatic_Translation`
- Cột: `tên file  [kích thước B]   số dòng` (chỉ đếm dòng cho file văn bản)

## Cây thư mục

```text
.gitattributes  [     40 B]
.github/
    workflows/
        ci.yml  [   2324 B]   84 dòng
.gitignore  [    527 B]
.mvn/
    wrapper/
        maven-wrapper.properties  [    171 B]   3 dòng
.vscode/
    settings.json  [     70 B]   3 dòng
AGENTS.md  [    971 B]   20 dòng
CONTRIBUTING.md  [    421 B]   18 dòng
Dockerfile  [    253 B]
docs/
    agents/
        conventions.md  [    233 B]   10 dòng
        project.md  [    343 B]   9 dòng
    BRANCHING.md  [    153 B]   8 dòng
    COMPLETED_REPORT.md  [   2703 B]   89 dòng
    PROJECT_STRUCTURE.md  <-- file này
mvnw  [  11790 B]
mvnw.cmd  [   8481 B]   189 dòng
pom.xml  [   3794 B]   137 dòng
README.md  [   6509 B]   128 dòng
src/
    main/
        java/
            vn/
                edu/
                    sgu/
                        audiotour/
                            AudiotourApplication.java  [    332 B]   13 dòng
                            config/
                                DataInitializer.java  [  11764 B]   182 dòng
                                OpenApiConfig.java  [   1569 B]   28 dòng
                            controller/
                                AnalyticsController.java  [   1832 B]   33 dòng
                                AudioPackageController.java  [   2020 B]   40 dòng
                                LocationController.java  [   3066 B]   54 dòng
                                TourController.java  [   1530 B]   34 dòng
                            dto/
                                DashboardResponse.java  [   1620 B]   51 dòng
                                PlaybackLogRequest.java  [   1476 B]   39 dòng
                            entity/
                                Language.java  [    577 B]   24 dòng
                                Location.java  [   2839 B]   72 dòng
                                Narration.java  [   1076 B]   36 dòng
                                PlaybackLog.java  [   2090 B]   54 dòng
                                Tour.java  [   1777 B]   49 dòng
                                TriggerType.java  [    260 B]   7 dòng
                            exception/
                                GlobalExceptionHandler.java  [   2145 B]   46 dòng
                            repository/
                                LanguageRepository.java  [    451 B]   14 dòng
                                LocationRepository.java  [   1396 B]   36 dòng
                                NarrationRepository.java  [    653 B]   16 dòng
                                PlaybackLogRepository.java  [   1162 B]   25 dòng
                                TourRepository.java  [    602 B]   21 dòng
                            service/
                                AnalyticsService.java  [   3847 B]   82 dòng
                                AudioPackageService.java  [   3377 B]   77 dòng
                                LocationService.java  [   1916 B]   47 dòng
                                TourService.java  [    758 B]   26 dòng
        resources/
            application.yml  [    588 B]   26 dòng
            logback-spring.xml  [    293 B]   10 dòng
    test/
        java/
            vn/
                edu/
                    sgu/
                        audiotour/
                            AnalyticsControllerValidationTest.java  [   2785 B]   71 dòng
                            AnalyticsServiceTest.java  [   3676 B]   91 dòng
                            AudioPackageControllerTest.java  [   1887 B]   49 dòng
                            AudioPackageServiceTest.java  [   4338 B]   118 dòng
                            AudiotourApplicationTests.java  [    228 B]   13 dòng
                            LocationControllerTest.java  [   2881 B]   82 dòng
                            LocationServiceTest.java  [   2630 B]   85 dòng
                            TourServiceTest.java  [   1722 B]   61 dòng
TASK_CHECKLIST.md  [   7641 B]   158 dòng
uploads/
    audio/
        dinh-doc-lap-vi.mp3  [ 248064 B]
        phong-dai-yen-vi.mp3  [ 142080 B]
        phong-khanh-tiet-en.mp3  [ 149184 B]
        phong-khanh-tiet-vi.mp3  [ 213696 B]
        test-sample.mp3  [     29 B]
        tram-khanh-hoi-vi.mp3  [ 147456 B]
        tram-vinh-hoi-vi.mp3  [ 127104 B]
        tram-xom-chieu-vi.mp3  [ 141888 B]
```

## Bản đồ tầng (3-Tier)

| Package | Tầng | Vai trò |
| :--- | :--- | :--- |
| `controller` | Tầng 1 — Presentation | REST endpoint, không chứa nghiệp vụ |
| `dto` | Tầng 1 — Presentation | Contract vào/ra của API |
| `exception` | Tầng 1 — Presentation | Bắt lỗi, trả HTTP status |
| `config` | Ngang tầng | Cấu hình Spring, OpenAPI, seed data |
| `service` | Tầng 2 — Business Logic | Nghiệp vụ, `@Transactional` |
| `repository` | Tầng 3 — Data Access | Spring Data JPA |
| `entity` | Tầng 3 — Data Access | Ánh xạ bảng CSDL |
| `src/test` | Kiểm thử | JUnit 5 + Mockito |

## Đếm theo tầng

| Tầng | Số file | Số dòng |
| :--- | ---: | ---: |
| Controller | 4 | 161 |
| DTO | 2 | 90 |
| Exception | 1 | 46 |
| Config | 2 | 210 |
| Service | 4 | 232 |
| Repository | 5 | 112 |
| Entity | 6 | 242 |
| Test | 8 | 570 |

> `DataInitializer.java` (182 dòng) chiếm phần lớn tầng `config`. Đây là seed data, không phải cấu hình Spring.

## Sinh lại file này

```powershell
$root = 'D:\Project\Multi_Languages_Automatic_Translation'
$skip = '\\(\.git|target|\.idea)(\\|$)'
$ext  = '\.(java|xml|yml|yaml|md|sql|properties|json|txt|sh)$'
Get-ChildItem -Recurse -Force -LiteralPath $root |
  Where-Object { $_.FullName.Substring($root.Length) -notmatch $skip } |
  Sort-Object FullName |
  ForEach-Object {
    $rel = $_.FullName.Substring($root.Length + 1)
    $ind = '    ' * (($rel.ToCharArray() | Where-Object { $_ -eq '\' }).Count)
    if ($_.PSIsContainer) { "$ind$($_.Name)/" }
    else {
      $n = if ($_.Name -match $ext) { "   $(@(Get-Content -LiteralPath $_.FullName).Count) dòng" } else { '' }
      "$ind$($_.Name)  [$('{0,7}' -f $_.Length) B]$n"
    }
  }
```
