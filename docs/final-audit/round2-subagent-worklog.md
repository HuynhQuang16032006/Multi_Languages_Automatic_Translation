# Worklog subagent vòng 2

| Agent | Nhiệm vụ | File sửa | Test / bằng chứng | Trạng thái |
|---|---|---|---|---|
| UniqueDb | V5 unique narration, duplicate/NULL/concurrent | `src/main/resources/db/migration/V5__narration_location_language_unique.sql`, `src/test/java/vn/edu/sgu/audiotour/AdminNarrationWriteIntegrationTest.java` | Orchestrator chạy Maven 184/184; MySQL index và V5 xác nhận | Hoàn thành trên DB test; preflight DB ứng dụng còn lại |
| DeviceValidation | Giới hạn deviceId tại HTTP | `DeviceHeartbeatRequest.java`, `PlaybackLogRequest.java`, `AnalyticsController.java`, `AudioPackageController.java`, `AnalyticsControllerValidationTest.java` | Orchestrator chạy Maven 184/184 | Hoàn thành |
| ExternalAudit | Audit nội bộ TTS/DeepL | Không | Source/tests; không gọi upstream, không chạy suite riêng | Hoàn thành audit; upstream BLOCKED_EXTERNAL |
| RuntimeAudit | Khảo sát cấu hình runtime | Không | Source/docs, không kiểm tra runtime trực tiếp; kết luận lịch sử cũ không được dùng | Hoàn thành; orchestrator xác minh runtime thực |
| BrowserE2E | Admin, tourist, QR/audio/dashboard, responsive | Không | Browser test trên 18081, DB test IDs 25/26, fixture POI 238; chưa camera/refresh/dedup | PARTIAL |
| ZipRuntime | Kiểm tra ZIP API | Không | 200, ZIP/text/audio khớp file seed; 404 thiếu POI/ngôn ngữ | PASS phạm vi API; orchestrator kiểm tra UI download |

Các agent không chạy build/test giữa chừng, không commit hoặc push. Orchestrator chạy full suite/package và frontend suite sau thay đổi, kiểm tra MySQL và browser ZIP. Không xuất token/credentials.
