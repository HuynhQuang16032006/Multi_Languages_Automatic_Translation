# Kế hoạch rebuild Audio Tour

## 1. Mục tiêu và trạng thái phê duyệt

Xây lại toàn bộ UI/UX Audio Tour thành sản phẩm có giao diện và chức năng thực tế. Tourist dùng hướng **Field Notes**, mobile first; Admin dùng **Studio Console**, desktop first. Người dùng đã phê duyệt kế hoạch, hai hướng thiết kế và cho phép bắt đầu code production.

Tài liệu này tập hợp các giai đoạn đã thống nhất trong hội thoại, gồm yêu cầu GPS/Nearby và quy tắc bảo toàn dữ liệu bổ sung. Checkbox thể hiện điều kiện cần hoàn thành, không phải xác nhận chức năng đã chạy. Trạng thái triển khai hiện tại nằm ở mục 10; không suy ra hoàn thành từ việc đã có route hoặc nút trên giao diện.

## 2. Phạm vi 12 trang

| Nhóm | Trang | Phạm vi |
| --- | --- | --- |
| Tourist | Home | Giới thiệu, địa điểm đáng ghé, địa điểm gần người dùng |
| Tourist | Explore | Tìm kiếm, lọc, sắp xếp, Gần tôi |
| Tourist | Place Detail | Ảnh, thông tin, nội dung, khu vực con, mở audio |
| Tourist | Audio Player | Phát, tua, chọn ngôn ngữ, văn bản thuyết minh, tải ZIP |
| Tourist | QR Scanner | Camera, nhập mã thay thế, tra cứu và điều hướng |
| Tourist | Explore Map | Leaflet, marker, vị trí người dùng, khoảng cách, bottom sheet |
| Tourist | About | Giới thiệu, hướng dẫn, giải thích quyền và giới hạn kết nối |
| Admin | Login | JWT, trạng thái đăng nhập và hết phiên |
| Admin | Dashboard | Thống kê, bộ lọc và dữ liệu phiên nghe thật |
| Admin | Place Management | CRUD, ẩn/hiện, quy tắc xóa an toàn |
| Admin | Audio Management | Quản lý nội dung theo ngôn ngữ, audio và TTS |
| Admin | QR Management | Tạo, quản lý, xem trước và tải QR hoạt động thật |

## 3. Nguyên tắc đã chốt

1. Dùng `ui-ux-pro-max` để nghiên cứu, thiết kế và rà soát từng nhóm trang. Kiểm tra mức phù hợp của kết quả tra cứu; không sao chép template chung chung.
2. Xây React + Vite với component dùng chung, routing rõ ràng và hai layout Tourist/Admin. Không kế thừa CSS hoặc layout HTML cũ.
3. Tái sử dụng kiến trúc backend Spring Boot hiện tại. Cho phép mở rộng schema/API để hoàn thành chức năng thật; không viết lại backend.
4. Tích hợp API và dữ liệu thật. Không dùng mock hoặc nút giả để tuyên bố hoàn thành. Dữ liệu kiểm thử chỉ dùng trong test hoặc bản duyệt có nhãn minh họa.
5. Bảo toàn GPS, geofence, Leaflet, QR, audio, TTS, đa ngôn ngữ, thống kê, JWT và khả năng offline đang hoạt động.
6. Làm tuần tự, Tourist trước Admin. Sau mỗi nhóm trang phải mở trình duyệt ở mobile/desktop, phát hiện và sửa lỗi thiết kế trước khi đi tiếp.
7. Giữ frontend cũ hoạt động đến khi chức năng thay thế đã qua đối chiếu và kiểm thử. Không xóa file cũ chỉ vì đã có trang React tương ứng.
8. Không tự mở rộng thành booking, đánh giá, tài khoản Tourist, bản đồ routing hoặc offline web đầy đủ nếu chưa có yêu cầu riêng.

## 4. Kiến trúc triển khai

- Một ứng dụng React có layout Tourist và Admin; Vite tạo static assets.
- Spring Boot phục vụ frontend cùng origin và đóng gói trong JAR. Trong giai đoạn chuyển đổi, React dùng `/next/`, HTML cũ giữ URL hiện tại.
- Dev server proxy API và những đường dẫn HTML cũ cần tiếp tục sử dụng sang backend.
- Bổ sung static routing/security cho React và asset; refresh route trực tiếp phải hoạt động. API ghi và thống kê quản trị vẫn bảo vệ bằng JWT ở server.
- Maven, Docker và CI phải tạo artifact có đúng bản frontend đã kiểm thử. Không coi `vite build` riêng lẻ là hoàn thành tích hợp production.
- Migration chỉ thêm những thay đổi schema cần thiết; không sửa migration đã áp dụng để thay đổi database đang có dữ liệu.
- Duy trì API contract cũ khi mở rộng, trừ thay đổi đã thống nhất để thực thi ẩn/hiện và bảo vệ dữ liệu.

## 5. Giai đoạn A: design system và thiết kế Tourist

### A1. Hoàn thiện Field Notes

- [ ] Chốt palette, cặp màu chữ/nền và tương phản.
- [ ] Chốt font hỗ trợ tiếng Việt, type scale, line-height và độ rộng đọc.
- [ ] Chốt spacing, grid, breakpoint, icon, ảnh, focus và micro-interactions.
- [ ] Xác định component dùng chung và trạng thái loading, empty, error, offline.
- [ ] Định nghĩa biến thể Studio Console trên cùng nền token, chưa thiết kế chi tiết Admin.

### A2. Luồng và thiết kế bốn màn hình trọng tâm

- [ ] Home: hero, danh sách giới thiệu, mục Địa điểm gần bạn và lối vào khám phá/QR.
- [ ] Explore: search, sort, Gần tôi, kết quả rỗng, lỗi và vị trí mini-player.
- [ ] Place Detail: một ảnh nếu API chỉ có ảnh đơn, thông tin, ngôn ngữ, CTA nghe, nội dung và khu vực con nếu có.
- [ ] Audio Player: điều khiển rõ, tiến trình thật khi triển khai, transcript, ngôn ngữ, lỗi thiếu audio và tải ZIP.
- [ ] Mô tả việc giữ phiên nghe khi đổi route và hành vi nút Back.
- [ ] Tạo bản trực quan mobile/desktop để người dùng đánh giá.

### A3. Thiết kế bổ sung GPS và các trang còn lại

- [ ] Thiết kế Explore Map mobile/desktop, marker, vị trí người dùng và bottom sheet.
- [ ] Thiết kế trạng thái quyền GPS, từ chối, timeout, không khả dụng và độ chính xác thấp.
- [ ] Phác QR Scanner và About, có cách thay thế khi không cấp quyền camera.
- [ ] Phân biệt nội dung thật với nội dung minh họa của bản duyệt.

**Điểm duyệt A:** hướng thiết kế đã được người dùng chấp thuận và người dùng đã cho phép bắt đầu production. Những trạng thái và hình ảnh chưa hoàn thiện vẫn phải tiếp tục xử lý; việc cho phép code không thay thế nghiệm thu UI cuối cùng.

Tài liệu chi tiết: [Field Notes](../design/FIELD_NOTES.md). Bản duyệt ban đầu: [prototype HTML](../design/field-notes-prototype.html).

## 6. Giai đoạn B: nền tảng React chạy song song HTML cũ

**Trạng thái:** Hoàn thành theo quyết định của người dùng. Docker, GitHub Actions và các điều kiện chưa có bằng chứng vẫn được theo dõi trong `docs/plans/audio-tour-open-items.md`.

### B1. Ứng dụng và kiểm thử nền

- [ ] Tạo Vite/React, router và layout tái sử dụng.
- [ ] Áp dụng token Field Notes bằng CSS mới, không nhập CSS cũ.
- [ ] API client gọi endpoint thật, xử lý loading/error/retry và dữ liệu không hợp lệ.
- [ ] Thiết lập test và lệnh production build frontend.
- [ ] Giữ đường dẫn quay lại frontend cũ trong thời gian chuyển đổi.

### B2. Tích hợp production

- [ ] Spring Boot phục vụ route và asset React, refresh trực tiếp không bị 401/404.
- [ ] API admin vẫn yêu cầu JWT, không mở quyền API chỉ để phục vụ SPA.
- [ ] Maven đóng gói đúng frontend trong JAR; quy trình tránh thiếu hoặc dùng nhầm asset cũ.
- [ ] Docker và CI build/test frontend trước bước đóng gói backend.
- [ ] Chạy artifact production để kiểm tra, không chỉ dev server.

**Điểm kiểm B:** test đạt, frontend và JAR build được, Home/Explore gọi API thật, navigation/refresh hoạt động, URL cũ còn dùng được. Kiểm tra giao diện mobile/desktop và console/network trước khi chuyển giai đoạn. Ghi rõ gate Docker/CI nào chưa thể chạy.

## 7. Giai đoạn C: Tourist hoàn chỉnh

**Trạng thái:** Hoàn thành theo quyết định của người dùng, chấp nhận geofence hiện chỉ gợi ý, không tự phát. Các điều kiện chưa có bằng chứng vẫn được theo dõi trong `docs/plans/audio-tour-open-items.md`.

Thực hiện từng lát cắt có test và kiểm tra trực quan, không làm toàn bộ logic rồi mới nối API.

### C1. Home và Explore

- [ ] Hiển thị địa điểm thật và link đúng ID.
- [ ] Tìm kiếm/sắp xếp trên dữ liệu thật; bộ lọc chỉ dùng thuộc tính có cơ sở trong dữ liệu.
- [ ] Xử lý ảnh thiếu/lỗi, danh sách rỗng và lỗi API.
- [ ] Nearby trên Home đề xuất tối đa 5 địa điểm gần nhất; chỉ có ít hơn 3 POI hợp lệ thì hiển thị đúng số lượng.
- [ ] Gần tôi trong Explore kết hợp từ khóa, giữ trạng thái khi đổi route và cho phép bỏ sắp xếp khoảng cách.

### C2. GPS và khoảng cách

- [ ] Xin quyền sau hành động liên quan, kèm giải thích mục đích; không bật permission prompt lúc vào Home.
- [ ] Xử lý chưa yêu cầu, đang lấy vị trí, được phép, từ chối, timeout, không khả dụng, thiếu hỗ trợ và kết nối không an toàn.
- [ ] Không dùng IP, GPS mô phỏng hoặc tâm bản đồ làm vị trí thật để đề xuất Nearby.
- [ ] Dùng Haversine, ghi rõ đường chim bay. Không suy thành quãng đường hay thời gian đi bộ.
- [ ] Loại POI thiếu tọa độ hợp lệ khỏi Nearby; không loại khỏi danh sách thường chỉ vì thiếu tọa độ.
- [ ] Không đảo thứ tự liên tục do GPS dao động; xử lý vị trí cũ và độ chính xác thấp.
- [ ] Mô tả trung thực việc heartbeat/log sử dụng vị trí; không hứa vị trí chỉ nằm trên thiết bị nếu dữ liệu thực tế gửi lên server.
- [ ] Tái sử dụng vị trí giữa các tính năng khi còn hợp lệ; dừng theo dõi khi không còn tính năng cần GPS.

### C3. Place Detail

- [ ] Mở được bằng URL trực tiếp, ID không tồn tại có trạng thái rõ.
- [ ] Hiển thị thông tin và khu vực con thật.
- [ ] Lấy văn bản theo địa điểm/ngôn ngữ; không dùng đoạn giới thiệu minh họa làm dữ liệu thật.
- [ ] Mở đúng audio và bản đồ cho địa điểm đang xem.

### C4. Audio Player

- [ ] Phát/pause/tua, tiến trình/thời lượng thật và lựa chọn ngôn ngữ.
- [ ] Giữ trạng thái nghe hợp lý khi điều hướng; không khởi tạo nhiều phiên audio cạnh tranh.
- [ ] Hiển thị transcript từ API, xử lý thiếu bản dịch hoặc audio.
- [ ] Duy trì heartbeat/log, không ghi trùng lượt nghe khi đổi route hoặc cập nhật vị trí.
- [ ] Giữ khả năng tải gói ZIP hiện có.
- [ ] Xử lý giới hạn autoplay bằng thao tác người dùng, không giả trạng thái phát thành công.

### C5. Explore Map

- [ ] Leaflet với marker từ POI thật và attribution nguồn tile.
- [ ] Vị trí người dùng có ký hiệu riêng và vòng độ chính xác.
- [ ] Marker và hàng danh sách chọn cùng một POI, mở thẻ tên/địa chỉ/khoảng cách và link chi tiết đúng ID.
- [ ] Chọn marker không tự phát audio.
- [ ] Mobile có bottom sheet gọn/mở rộng, nút thay thế thao tác kéo và control cảm ứng dễ dùng.
- [ ] Desktop có danh sách cạnh bản đồ; không phủ panel mobile lên toàn màn hình.
- [ ] Sheet không che attribution, control map, mini-player, navigation hoặc focus.
- [ ] GPS cập nhật không giành quyền kéo bản đồ; chỉ căn lại khi người dùng yêu cầu.
- [ ] Từ chối GPS vẫn xem được marker địa điểm.

### C6. Geofence

- [ ] Đọc và kiểm thử quy tắc hiện tại trước khi di chuyển logic.
- [ ] Tách lựa chọn khám phá GPS khỏi bật/tắt thuyết minh tự động.
- [ ] Giữ bán kính và ưu tiên địa điểm, có thứ tự xác định khi vùng chồng nhau.
- [ ] Chống phát lặp trên mỗi GPS fix; kiểm chứng cơ chế vào/ra vùng, hysteresis và cooldown.
- [ ] Không giành quyền phát khi đang nghe hoặc người dùng chủ động tạm dừng; có thể đề xuất điểm mới thay vì chuyển bài.
- [ ] Xác định điều kiện cho phát lại khi rời rồi quay lại.
- [ ] Không kích hoạt bằng vị trí cũ/độ chính xác không đáng tin; không hứa GPS nền khi trình duyệt/OS đình chỉ trang.

### C7. QR Scanner, About và offline hiện có

- [ ] Camera QR có trạng thái quyền/lỗi và cách nhập mã thủ công.
- [ ] Nhận diện/kiểm tra mã hoặc link theo định dạng được hỗ trợ, tra API thật, không điều hướng tùy ý tới URL không tin cậy.
- [ ] Mã không tồn tại có thông báo và cách thử lại.
- [ ] About hướng dẫn sử dụng, quyền GPS/camera, chọn ngôn ngữ và khả năng offline thực tế.
- [ ] Giữ cache POI và tải ZIP đang hoạt động; cache có nhãn/thời điểm khi cần.
- [ ] Không coi tải ZIP là nghe offline trên web, không cam kết tile bản đồ offline nếu chưa xây.

**Điểm kiểm C:** bảy trang Tourist hoạt động với backend thật. Kiểm tra 375px, 768px, 1024px, 1440px; portrait/landscape khi liên quan; bàn phím, screen reader cơ bản, reduced motion, quyền camera/GPS và Back. GPS/camera/autoplay cần kiểm tra thêm trên điện thoại ở HTTPS hoặc môi trường an toàn được trình duyệt cho phép. Chỉ sau điểm kiểm này mới cân nhắc thay đường dẫn Tourist mặc định.

## 8. Giai đoạn D: mở rộng backend quản trị

### D1. Địa điểm và ẩn/hiện

- [ ] Bổ sung DTO, validation, service và endpoint tạo/sửa/xóa theo kiến trúc hiện tại.
- [ ] Kiểm tra tọa độ, bán kính, độ ưu tiên, mã QR duy nhất và quan hệ cha–con hợp lệ.
- [ ] **Từ chối xóa nếu có địa điểm con, bài thuyết minh hoặc lịch sử nghe.** Không xóa dây chuyền để vượt ràng buộc.
- [ ] Thêm ẩn/hiện để bảo toàn dữ liệu. Admin vẫn quản lý địa điểm ẩn; xác định thống nhất hành vi API công khai ở danh sách, chi tiết và tra QR.
- [ ] Không làm mất dữ liệu thống kê khi thay đổi trạng thái địa điểm.

### D2. Nội dung và audio

- [ ] API liệt kê/tạo/sửa/xóa bài theo địa điểm và ngôn ngữ.
- [ ] Tái sử dụng TTS hiện tại; tạo/tạo lại audio có phản hồi thật và xác nhận trước tác vụ tốn credit.
- [ ] Quy định xử lý audio khi văn bản thay đổi, sinh file lỗi hoặc xóa bài.
- [ ] Không xóa nhầm file đang dùng. Nếu thêm upload theo yêu cầu quản lý audio, chốt contract và kiểm tra file/kích thước/đường dẫn trước triển khai.

### D3. QR và migration

- [ ] Tái sử dụng `qrCode` và quan hệ địa điểm; thêm endpoint chỉ khi CRUD hiện tại chưa đáp ứng quy trình.
- [ ] Phân biệt QR địa điểm với QR link địa điểm gốc/tour hiện có; giữ liên kết cũ tương thích.
- [ ] Không thêm bảng QR riêng nếu chưa có nhu cầu vòng đời độc lập.
- [ ] Thêm migration cần thiết cho các thay đổi thật, thử trên database đã có dữ liệu.

**Điểm kiểm D:** API ghi thiếu/sai JWT bị từ chối; tạo/sửa/xóa/ẩn/hiện lưu và đọc lại đúng; lỗi validation/conflict có thông báo rõ; Tourist và lịch sử nghe không hồi quy. Mỗi hành vi mới có test thất bại trước khi viết implementation.

## 9. Giai đoạn E và F: Admin, chuyển đổi, dọn dẹp

### E1. Login và Dashboard

- [ ] Đăng nhập, hết phiên và đăng xuất với JWT thật; xử lý 401, 429 và lỗi server.
- [ ] Thống kê và bộ lọc dùng dữ liệu API hiện có, không vẽ số liệu giả.
- [ ] Giữ đổi mật khẩu và quản lý/ngôn ngữ hiện có ở vị trí phù hợp, không bỏ vì danh sách chỉ có năm trang Admin.

### E2. Ba trang quản lý

- [ ] Place Management: danh sách, CRUD, ẩn/hiện, validation và lỗi quan hệ chặn xóa.
- [ ] Audio Management: nội dung theo ngôn ngữ, trạng thái audio, TTS và lỗi dịch/sinh audio thật.
- [ ] QR Management: chọn địa điểm, tạo/cập nhật mã, xem trước, tải QR và kiểm tra link trên điện thoại.
- [ ] QR không trỏ tới host chỉ truy cập được trên máy phát triển khi người dùng cần quét từ điện thoại.
- [ ] Mọi hành động lưu dữ liệu phải báo đúng kết quả server; không dùng thông báo thành công giả.

**Điểm kiểm E:** năm trang Admin hoạt động với database/JWT thật; kiểm tra desktop/mobile, bàn phím, thông báo lỗi, xác nhận thao tác dữ liệu và hết phiên. Mở trình duyệt kiểm tra sau mỗi nhóm trang.

### F1. Nghiệm thu production

- [ ] Chạy toàn bộ backend test, compile và package.
- [ ] Chạy frontend test và production build; kiểm tra artifact JAR và Docker.
- [ ] Kiểm tra URL trực tiếp/refresh, navigation, API permissions và luồng end-to-end.
- [ ] Đối chiếu chức năng của `index.html`, `player.html`, `dashboard.html`, `demo.html` với UI mới, gồm tải offline và luồng demo còn cần dùng.
- [ ] Ghi kết quả test đã chạy, blocker và giới hạn chưa nghiệm thu, không bỏ/nới test để đạt.

### F2. Chuyển đổi và dọn frontend cũ

- [ ] Chuyển route mặc định sau khi các điểm kiểm tương ứng đạt.
- [ ] Chỉ xóa file cũ khi chức năng thay thế đã qua đối chiếu. Tách thay đổi dọn dẹp để dễ rà soát/khôi phục.
- [ ] Cập nhật ADR về frontend, README và hướng dẫn build/deploy.
- [ ] Kiểm tra diff và xác nhận không xóa hoặc ghi đè thay đổi không thuộc công việc.

**Điểm kiểm F:** 12 trang dùng chức năng/API thật, tính năng cũ được bảo toàn hoặc chuyển vị trí có đối chiếu; artifact production chạy được; mọi giới hạn còn lại có ghi nhận cụ thể.

## 10. Trạng thái tại lúc lập tài liệu

- Người dùng đã phê duyệt Field Notes/Studio Console, kế hoạch và bắt đầu production.
- Có prototype và tài liệu design system; trạng thái quyền/lỗi, thiết kế GPS/map và ảnh vẫn cần hoàn thiện khi triển khai.
- Giai đoạn B đang thực hiện: React có Home/Explore, search/sort và API client thật; chạy qua Spring Boot tại `/next/`.
- Route chi tiết React hiện chỉ dẫn sang player HTML cũ theo ID, **chưa phải Place Detail hoàn chỉnh**. Nearby, Map/Player/QR React và Admin chưa triển khai.
- Đã thêm routing/security và cấu hình đóng gói frontend vào JAR, Dockerfile, CI. Chưa chạy Docker build hoặc GitHub Actions và chưa khởi động trực tiếp JAR đóng gói.
- Kết quả gần nhất: frontend test 3/3; Vite build đạt với warning `use client` từ React Router; `mvnw.cmd package` đạt 142 backend test. JAR có HTML/CSS/JS React.
- Backend/API đã kết nối MySQL và trả 6 POI thật. Chrome Home ở 375px/1440px không tràn ngang. Explore/navigation và console/network còn cần kiểm tra trước khi chốt B.
- Maven local hiện copy `frontend/dist`; phải build frontend trước package, còn rủi ro thiếu/stale asset nếu bỏ bước. Cần hoàn thiện gate build production.
- Frontend HTML cũ chưa bị sửa hoặc xóa; chưa có migration cho CRUD hoặc ẩn/hiện. Các thay đổi chưa commit tại thời điểm bàn giao.

Hướng dẫn chạy và trạng thái lát cắt: [frontend/README.md](../../frontend/README.md). Bàn giao phiên ở `C:\Users\User\AppData\Local\Temp\opencode\audio-tour-handoff.md` chứa thêm tiến trình và công cụ kiểm tra tạm, không phải tài liệu triển khai lâu dài.

## 11. Quy trình kiểm tra cho từng lát cắt

1. Đọc `AGENTS.md`, query graphify trước khi tìm hiểu source, đọc diff trước với file đã thay đổi.
2. Dùng skill phù hợp và xác định phạm vi/callers/test/config bị ảnh hưởng.
3. Với logic/hành vi: viết test, chạy để thấy thất bại đúng lý do, rồi viết thay đổi nhỏ nhất để đạt. Config/docs thuần không cần ép test hành vi.
4. Chạy targeted test, full backend suite và compile theo gate của repo; frontend dùng test/build thực tế đã thiết lập. Không báo đạt lệnh chưa chạy.
5. UI phải có kiểm tra trực quan mobile/desktop, navigation, focus và console/network. GPS, camera và autoplay có thêm kiểm tra thiết bị thật.
6. Tự review diff; cập nhật tài liệu/checkpoint, graph khi thay đổi cấu trúc. Không tự xóa frontend cũ hoặc thay dữ liệu để làm đẹp kết quả.

Skill trọng tâm theo bước: `ui-ux-pro-max`, `test-driven-development`, `incremental-implementation`, `browser-testing-with-devtools`, `security-and-hardening`, `ci-cd-and-automation`, `code-review-and-quality`, `documentation-and-adrs`. Chỉ load skill khớp việc đang làm; không tự sinh subagent.
