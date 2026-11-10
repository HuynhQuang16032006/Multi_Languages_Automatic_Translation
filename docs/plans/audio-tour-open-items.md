# Các mục cần sửa và kiểm tra sau giai đoạn F

Danh sách này ghi nhận các mục chưa đạt hoặc chưa có bằng chứng nghiệm thu tại thời điểm ghi. Sau giai đoạn F, dùng danh sách này cùng kế hoạch gốc để lập giai đoạn sửa và kiểm tra A–F. Không xem mục chưa kiểm tra là lỗi đã xác nhận.

## A. Thiết kế

- [x] Giai đoạn A hoàn thành theo xác nhận của người dùng. Đã bổ sung thông báo lỗi GPS và bật hiển thị ảnh API; người dùng xác nhận đã kiểm tra tốt và có quyền sử dụng ảnh. Các vùng GPS chồng nhau là chủ ý vì địa điểm có nhiều phòng. Chưa có bằng chứng kiểm tra riêng từng ảnh đúng địa điểm, từng URL ảnh và các trạng thái GPS timeout, độ chính xác thấp; một URL từng trả 404.

## B. Nền tảng và production

**Trạng thái:** Hoàn thành theo quyết định của người dùng. Các mục chưa kiểm tra dưới đây vẫn được giữ lại, không coi là đã đạt.

- [ ] Chạy Docker build và GitHub Actions thực tế; cấu hình đã được đọc nhưng chưa có bằng chứng hai gate này đạt.
- [ ] Rà soát lại các điều kiện điểm kiểm B theo kế hoạch trên artifact hiện hành; kết quả kiểm tra cũ không thay thế nghiệm thu bản cuối.

## C. Tourist

**Trạng thái:** Hoàn thành theo quyết định của người dùng; người dùng chấp nhận geofence hiện chỉ gợi ý. Các mục chưa kiểm tra dưới đây vẫn được giữ lại, không coi là đã đạt.

- [ ] Kiểm tra thực địa ngưỡng GPS, vùng chồng nhau, hysteresis, cooldown và hành vi quay lại vùng. Geofence hiện chỉ gợi ý, không tự phát.
- [ ] Kiểm tra heartbeat khi tua, đổi ngôn ngữ, đóng trang và xử lý lỗi; xác nhận tải ZIP.
- [ ] Kiểm tra marker, responsive và GPS thực địa trên Map; người dùng dự kiến tự kiểm tra GPS.
- [ ] Kiểm tra camera QR trên điện thoại, autoplay và GPS trên HTTPS hoặc môi trường an toàn.
- [ ] Kiểm tra đủ viewport 375/768/1024/1440px, portrait/landscape khi liên quan, bàn phím, screen reader cơ bản, reduced motion và Back theo điểm kiểm C.
- [ ] Đối chiếu cache/offline và tính năng HTML cũ trước khi chuyển route mặc định. Web chưa hỗ trợ audio hoặc tile offline.

## D. Backend quản trị

- [ ] Hoàn thiện ẩn/hiện nhất quán giữa API công khai và admin. API tạo/sửa địa điểm đã có test tích hợp cho validation, QR trùng và quan hệ cha–con; chưa kiểm thử cập nhật cha đồng thời, có nguy cơ tạo chu trình khi hai giao dịch cùng ghi.
- [ ] Hoàn thiện tạo/sửa/xóa nội dung, vòng đời file và TTS. Đã có API liệt kê bài theo địa điểm cho admin và API TTS hiện có; chưa hoàn thiện các thao tác còn lại.
- [ ] Kiểm tra QR, migration trên database đã có dữ liệu và rollback an toàn khi có thay đổi schema.
- [ ] Xác minh điểm kiểm D đầy đủ, gồm JWT, validation, conflict, dữ liệu Tourist và lịch sử nghe. Lát cắt xóa địa điểm đã có test tích hợp; chưa kiểm thử tình huống ghi đồng thời.

## E. Admin

- [ ] Hoàn thiện ẩn/hiện địa điểm. Backend chưa có trạng thái và API tương ứng; giao diện hiện thông báo giới hạn thay vì cung cấp thao tác giả.
- [ ] Hoàn thiện QR Management: tạo/cập nhật mã theo quy trình, tạo ảnh QR, xem trước và tải; kiểm tra link từ điện thoại với host có thể truy cập. Hiện trang chỉ liệt kê mã, việc sửa mã đi qua Place Management.
- [ ] Đối chiếu và bổ sung đổi mật khẩu, quản lý ngôn ngữ và dịch trong giao diện React. Hiện còn liên kết đến dashboard HTML cũ; chưa xác nhận tính năng thay thế đầy đủ.
- [ ] Kiểm thử Login, Dashboard, Place, Audio và QR với JWT và database thật, gồm 401/429, token hết hạn, validation/conflict, tạo/sửa/xóa và xác nhận thao tác. Có giao diện và một số test client, nhưng chưa có bằng chứng hoàn thành các luồng này. Không tạo hoặc xóa dữ liệu thật chỉ để thử.
- [ ] Kiểm tra thiết kế, điều hướng, focus, console/network, responsive desktop/mobile và thiết bị thật theo điểm kiểm E. Người dùng đã xác nhận đăng nhập và nhận xét biểu đồ tốt, nhưng chưa kiểm thử đầy đủ các trang.
- [ ] Kiểm tra lại bộ lọc ngày `dd/mm/yyyy` và biểu đồ cột dọc trong trình duyệt sau đăng nhập. Bản mới đã build và được mở qua Vite cổng 5173; bản Spring Boot cổng 8080 từng phục vụ asset cũ. Chưa có bằng chứng bản production hiện tại đã chứa thay đổi mới.
- [ ] Kiểm tra tab Biểu đồ với dữ liệu thật và các trường hợp khoảng ngày, dữ liệu rỗng, phiên đang phát/tạm dừng/rời phiên. Số trực tuyến là số phiên theo thiết bị, không phải số người duy nhất; phiên mất heartbeat hết hạn sau 8 giây, trạng thái lưu trong bộ nhớ từng instance. Không coi giá trị là đếm người chính xác trong mọi trường hợp.
- [ ] Cách ly database test trước khi chạy lại backend suite. `ProdProfileTest` dùng database ứng dụng và khởi tạo Spring đã ghi đè `admin_auth`; đã khôi phục bản ghi trước lần chạy test từ MySQL binary log và người dùng xác nhận đăng nhập được. Cần sửa nguyên nhân và chứng minh test không ghi vào database ứng dụng trước khi chạy lại.
- [ ] Rà soát bảo mật triển khai: `AuthController` tin `X-Forwarded-For` cho rate limit; cấu hình mặc định có credential và JWT secret cố định. Khả năng khai thác phụ thuộc proxy/profile; chưa xác minh môi trường production. JWT cũ cũng chưa được xác nhận thu hồi sau đổi mật khẩu.

Nguồn: `docs/plans/audio-tour-rebuild.md`, `frontend/README.md` và handoff giai đoạn D. Các mục mới cần ghi kèm bằng chứng hoặc lý do chưa kiểm tra.

## Lỗi kiểm thử chưa khắc phục

Test backend từng ghi đè tài khoản quản trị do dùng chung database; xem mục E. Theo quy ước người dùng, audio đã sinh được giữ lại kể cả khi sửa văn bản; admin tạo bài mới khi cần nội dung khác. Văn bản và audio có thể khác nhau nếu vẫn sửa bài cũ; API hiện không cho tạo bài thứ hai cùng địa điểm và ngôn ngữ.

## Kết quả kiểm tra giai đoạn F

- [ ] Cách ly database test và chứng minh không kết nối database ứng dụng trước khi chạy backend suite hoặc package có test. `ProdProfileTest` kích hoạt profile `prod`, nhận datasource từ môi trường và khởi tạo tài khoản admin; chưa có bằng chứng cách ly. Không chạy test backend hay package trong lượt kiểm tra này để tránh lặp lại sự cố dữ liệu.
- [ ] Tạo và chạy JAR production hiện hành sau khi bảo đảm an toàn database và quy trình đóng gói. JAR có sẵn chứa `index-C7N9HH9-.js` và `index-bG_F7b_U.css`; frontend build hiện tại tạo `index-Ca1MQ0Dd.js` và `index-BMwmfsw6.css`. Vì asset khác nhau, JAR có sẵn không chứng minh bản hiện tại. Không khởi động JAR cũ vì có thể tác động database ứng dụng.
- [ ] Kiểm tra trực tiếp trên artifact hiện hành: URL/refresh, navigation, JWT và phân quyền API, luồng Tourist/Admin, bộ lọc ngày và biểu đồ sau đăng nhập. Tại thời điểm kiểm tra, cổng 8080, 8081 và 5173 không có dịch vụ lắng nghe; chỉ thấy MySQL tại 3306. Không có bằng chứng kiểm tra trình duyệt hoặc end-to-end trong lượt này.
- [ ] Đối chiếu đầy đủ `index.html`, `player.html`, `dashboard.html`, `demo.html` với React, gồm cache, ZIP, demo và các chức năng Admin còn dẫn sang HTML cũ. Chưa đủ bằng chứng để chuyển route hoặc xóa frontend cũ.
- [ ] Chạy Docker build và kiểm tra image: `docker version` thất bại vì không tìm thấy lệnh `docker`. GitHub Actions gần nhất, run `42546316841` trên commit `5977d6b40a91e3858bc7d898de09b8acad9b0079`, thất bại ở bước Maven: bốn failure tại `PaginationIntegrationTest.login:30`, tổng 152 test; Docker job bị bỏ qua. Đây là run commit cũ, không chứng minh trạng thái workflow và working tree hiện tại. Cần điều tra nguyên nhân và chạy CI trên thay đổi đã được người dùng cho phép đưa lên.
- [ ] Kiểm tra trên điện thoại và HTTPS: GPS thực địa, camera QR, autoplay và link QR truy cập được từ thiết bị. Chưa kiểm tra trong lượt này.

Các lệnh đã chạy trên working tree hiện tại: `npm test` đạt 36/36; `npm run build` đạt với hai cảnh báo React Router `use client`; `npm audit --omit=dev` báo 0 lỗ hổng đã biết; `mvnw.cmd compile -q` đạt. Compile không thay thế backend test, package hoặc kiểm tra artifact chạy thực tế. Chưa nghiệm thu các gate A–F.
