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

**Kiểm tra bổ sung:** Sửa geofence bỏ qua độ chính xác âm và giữ cooldown tại timestamp 0; marker hiển thị tên dạng văn bản và chọn được bằng bàn phím. Sửa vòng đời camera QR, truy cập QR/About khi API danh sách lỗi, và trường hợp trình duyệt chặn localStorage. Player xử lý tua, lỗi và đóng trang khi gửi heartbeat; không dùng bản thuyết minh cũ khi đổi ngôn ngữ, gửi device ID khi tải ZIP. `npm test` đạt 43/43; `npm run build` đạt, còn hai cảnh báo `use client` từ React Router. Trên Vite không có backend, QR và About mở được; QR báo cách nhập tay khi trình duyệt không hỗ trợ camera; Back hoạt động và trang QR không tràn ngang tại 375/768/1024/1440 px. Đây không phải nghiệm thu với backend thật.

Cache React và HTML cũ đều lưu POI trong localStorage; HTML cũ theo dõi online/offline còn React chưa tự cập nhật khi kết nối thay đổi. Chưa xác nhận tương đương mọi tính năng HTML cũ. Heartbeat gửi bất đồng bộ có thể đến sai thứ tự khi đổi ngôn ngữ nhanh. Các checkbox trên vẫn mở: chưa kiểm tra thực địa, điện thoại, ZIP thật, audio/heartbeat với server, screen reader và đầy đủ viewport cho tất cả trang. Không chuyển route mặc định.

### Hướng dẫn kiểm tra Tourist

Ghi phiên bản hoặc commit, môi trường, kết quả và lỗi cho từng mục. Chỉ đánh dấu đạt khi có bằng chứng; giữ nguyên các mục chưa kiểm tra.

#### Chuẩn bị

- [ ] Dùng database thử nghiệm có địa điểm, audio và mã QR. Không chạy backend test với database ứng dụng: lần trước test đã ghi đè tài khoản admin.
- [ ] Chạy backend và frontend bản hiện tại, mở `/next/` trên máy tính và xác nhận danh sách địa điểm tải được.
- [ ] Với điện thoại, dùng địa chỉ HTTPS mà điện thoại truy cập được. `localhost` trên điện thoại không phải máy tính.

#### Audio, heartbeat và ZIP

Chọn địa điểm có audio và ít nhất hai ngôn ngữ; dùng dữ liệu thử nghiệm.

- [ ] Phát, tạm dừng, phát lại, tua tiến và lùi. Âm thanh và thời gian hiển thị khớp; thời gian tua không được tính là thời gian nghe.
- [ ] Đổi ngôn ngữ, kể cả đổi nhanh liên tiếp. Nội dung và audio khớp ngôn ngữ đang chọn, không hiện bản cũ.
- [ ] Chuyển sang Explore rồi quay lại. Audio không tạo nhiều phiên phát cạnh tranh.
- [ ] Đóng trang và mở lại; thử mất mạng hoặc audio lỗi. Kiểm tra thông báo và trạng thái phiên nghe.
- [ ] Trong tab Network, kiểm tra request heartbeat `JOIN`, `PING`, `LEAVE`, ngôn ngữ và thời gian nghe; đối chiếu dữ liệu trên môi trường thử nghiệm. Kiểm tra đổi ngôn ngữ nhanh vì request bất đồng bộ có thể đến sai thứ tự.
- [ ] Tải ZIP, giải nén, mở thử văn bản và audio. ZIP tải được không có nghĩa web hỗ trợ nghe offline.

#### GPS, Map và QR trên điện thoại

- [ ] Từ chối rồi cấp quyền GPS; kiểm tra thông báo, vị trí và vòng độ chính xác. Di chuyển vào, ra và quay lại vùng; thử vùng chồng nhau, hysteresis và cooldown. Geofence chỉ gợi ý, không tự phát.
- [ ] Chọn marker và hàng danh sách Map; kiểm tra đúng địa điểm, thao tác kéo, nút vị trí và giao diện khi từ chối GPS.
- [ ] Quét QR thật, thử mã sai và nhập tay; xác nhận camera dừng khi rời trang. Thử từ chối quyền và trình duyệt không hỗ trợ quét.
- [ ] Kiểm tra phát audio sau thao tác người dùng và khi trình duyệt chặn autoplay.

#### Giao diện và offline

- [ ] Mở từng trang Tourist ở 375, 768, 1024 và 1440 px; kiểm tra portrait/landscape khi liên quan, nội dung tràn và nút bị che.
- [ ] Kiểm tra Tab, Enter, Space, Back, screen reader cơ bản và reduced motion.
- [ ] Tải danh sách khi online, ngắt mạng rồi tải lại; xác nhận cache POI có nhãn thời điểm. Kiểm tra khi có mạng trở lại: React hiện chưa tự cập nhật trạng thái kết nối như HTML cũ. Audio và tile không hỗ trợ offline trên web.
- [ ] Đối chiếu từng chức năng Tourist của HTML cũ với React trước khi cân nhắc chuyển route mặc định.

**Điều kiện chốt:** Các bước bắt buộc đạt hoặc giới hạn còn lại được người dùng chấp nhận rõ ràng; ghi bằng chứng và lỗi vào danh sách. Không cần chuyển route mặc định ngay khi chốt C.

## D. Backend quản trị

- [ ] Đã bổ sung ẩn/hiện: `GET /api/admin/locations` liệt kê cả địa điểm ẩn, `PATCH /api/admin/locations/{id}/visibility` nhận `{"visible":true|false}`. API Tourist lọc địa điểm ẩn; không ẩn cha khi còn con hiện, không hiện con dưới cha ẩn. Khóa database tuần tự hóa cập nhật cây; test tích hợp và test ghi đồng thời chưa chạy.
- [ ] API tạo/sửa/xóa bài và TTS đã có. Sửa văn bản giữ audio; xóa bài giữ file; sinh lại TTS ghi file mới và giữ file cũ. Test đơn vị TTS đạt, nhưng CRUD/TTS chưa kiểm tra tích hợp với database cách ly. Vẫn chỉ cho một bài mỗi địa điểm/ngôn ngữ; chưa có quy tắc chọn bài công khai khi tồn tại nhiều bài.
- [ ] QR mới/sửa được kiểm tra định dạng tương thích scanner, mã cũ không đổi được giữ; chưa kiểm tra tích hợp. Migration `V4__location_visibility.sql` mặc định giữ địa điểm hiện, thêm khóa cây; chưa chạy trên bản sao database có dữ liệu. Hướng dẫn rollback đã sửa trong `docs/ROLLBACK.md`, chưa diễn tập.
- [ ] Chưa xác minh đầy đủ JWT, validation, conflict, dữ liệu Tourist và lịch sử nghe trên database thử. Đã thêm test tích hợp ẩn/hiện và cập nhật cha đồng thời nhưng chưa chạy. Phiên nghe đã mở được phép PING/LEAVE sau khi ẩn; phiên mới tới địa điểm ẩn bị từ chối.

**Kiểm tra bổ sung:** `./mvnw.cmd -Dtest=TestDatabaseIsolationTest,TtsServiceTest,PublicLocationVisibilityTest,AudioPackageControllerTest,NarrationControllerTest -Dskip.npm=true test -q` và `./mvnw.cmd -Dtest=LocationServiceTest,LiveAudienceTest -Dskip.npm=true test -q` đạt 36 test đơn vị, không kết nối database. `./mvnw.cmd compile -q` đạt; frontend `npm test` đạt 44/44 và `npm run build` đạt với hai cảnh báo React Router. Lần Maven đầu bị `npm ci` lỗi Windows `-4048` do Vite kiểm tra cũ còn giữ `esbuild.exe`; đã dừng tiến trình kiểm tra đó và build lại thành công. Không chạy backend suite hay migration vì chưa chứng minh quyền tài khoản test và database cách ly.

### Đề xuất hoàn tất mục D

1. **Cách ly MySQL thử nghiệm:** dùng instance riêng hoặc container nếu có Docker. Tạo `audiotour_test`, `audiotour_prod_profile_test` và tài khoản `audiotour_test` chỉ có quyền trên hai database này. Kiểm tra quyền trước khi khởi động Spring; không dùng credential ứng dụng.
2. **Chạy test tích hợp:** sau khi xác minh cách ly, chạy backend suite; xử lý lỗi thực tế về JWT, QR trùng, ẩn/hiện, dữ liệu Tourist, lịch sử nghe và cập nhật cha–con đồng thời.
3. **Diễn tập migration và rollback:** dùng bản sao database có dữ liệu hoặc dữ liệu thử đại diện, không dùng database ứng dụng. Chạy V4, đối chiếu dữ liệu và lịch sử Flyway; phục hồi backup vào database trống riêng và thử artifact cũ tương thích. Làm theo `docs/ROLLBACK.md`.
4. **Chốt quy tắc nhiều bài:** hiện chỉ có một bài cho mỗi địa điểm/ngôn ngữ, trong khi quy ước là giữ audio cũ và tạo bài mới khi nội dung đổi. Quyết định bài nào được công khai trước khi bỏ ràng buộc; nếu không, Tourist không biết phải phát bản nào.
5. **Nghiệm thu trước triển khai:** ghi kết quả vào mục D; chỉ áp dụng V4 lên database ứng dụng sau khi backup và diễn tập đạt. Nút ẩn/hiện trên giao diện thuộc mục E.

Không chạy test tích hợp trên MySQL hiện tại khi quyền tài khoản test chưa được chứng minh cách ly.

## E. Admin

- [ ] Đã thêm nút ẩn/hiện có xác nhận và báo lỗi từ server; chưa nghiệm thu đầy đủ trên thiết bị và dữ liệu thật.
- [ ] QR Management đã có cập nhật mã qua PATCH chống ghi đè, tạo ảnh QR cục bộ, xem trước và tải PNG. Đã xem ảnh trên JAR với host minh họa; chưa kiểm tra tải/quét bằng điện thoại hoặc host thực tế.
- [ ] React đã thêm đổi mật khẩu, liệt kê và thêm ngôn ngữ qua dịch/TTS thật; có xác nhận chi phí. Chưa kiểm tra dịch/TTS với dịch vụ thật; giữ liên kết dashboard HTML cũ.
- [ ] Kiểm thử Login, Dashboard, Place, Audio và QR với JWT và database thật, gồm 401/429, token hết hạn, validation/conflict, tạo/sửa/xóa và xác nhận thao tác. Có giao diện và một số test client, nhưng chưa có bằng chứng hoàn thành các luồng này. Không tạo hoặc xóa dữ liệu thật chỉ để thử.
- [ ] Kiểm tra thiết kế, điều hướng, focus, console/network, responsive desktop/mobile và thiết bị thật theo điểm kiểm E. Người dùng đã xác nhận đăng nhập và nhận xét biểu đồ tốt, nhưng chưa kiểm thử đầy đủ các trang.
- [ ] Trên JAR mới và database thử, đã đăng nhập và kiểm tra bộ lọc `dd/mm/yyyy`, biểu đồ cột dọc. Chưa đối chiếu với dữ liệu production.
- [ ] Kiểm tra tab Biểu đồ với dữ liệu thật và các trường hợp khoảng ngày, dữ liệu rỗng, phiên đang phát/tạm dừng/rời phiên. Số trực tuyến là số phiên theo thiết bị, không phải số người duy nhất; phiên mất heartbeat hết hạn sau 8 giây, trạng thái lưu trong bộ nhớ từng instance. Không coi giá trị là đếm người chính xác trong mọi trường hợp.
- [ ] Test đã chạy với MySQL riêng tại `127.0.0.1:33316`; `SHOW GRANTS` xác nhận tài khoản test chỉ có quyền trên hai database thử. Không sử dụng database ứng dụng. Cần duy trì cấu hình cách ly trong CI và các lần kiểm thử tiếp theo.
- [ ] Đã bỏ tin `X-Forwarded-For` cho rate limit, từ chối secret/mật khẩu dev trên profile prod, thu hồi JWT cũ khi đổi mật khẩu. Backend suite đạt trên MySQL thử. Sau proxy, các client cùng dùng hạn mức theo địa chỉ proxy; rate limit vẫn riêng từng instance. JWT cũ cần đăng nhập lại sau triển khai; cấu hình thực tế chưa được rà soát.

**Bằng chứng:** `npm test` đạt 48/48; `npm run build` đạt, còn hai cảnh báo React Router; `npm audit --omit=dev` báo 0 lỗ hổng đã biết. Backend `mvnw.cmd package -q` đạt 172 test trên MySQL thử; JAR mới chứa asset frontend hiện tại. Trình duyệt trên JAR đã kiểm tra đăng nhập, trang địa điểm, biểu đồ, QR desktop/mobile, refresh cài đặt và API admin thiếu JWT trả 401. Chưa kiểm tra toàn bộ thao tác, screen reader, dữ liệu thật hoặc thiết bị thật.

### Đề xuất hoàn tất mục E

1. **Kiểm tra luồng quản trị trên database thử:** dùng tài khoản và dữ liệu thử riêng để kiểm tra đăng nhập, 401/429, token hết hạn, đổi mật khẩu và thu hồi token cũ; thử tạo/sửa/xóa, ẩn/hiện địa điểm, validation và conflict. Không tạo hoặc xóa dữ liệu production chỉ để kiểm thử.
2. **Kiểm tra QR và dịch/TTS:** tạo và cập nhật mã thử, tải PNG rồi quét bằng điện thoại với host truy cập được. Kiểm tra đổi mật khẩu, thêm ngôn ngữ, trạng thái lỗi và kết quả dịch/TTS bằng môi trường thử có cấu hình dịch vụ thật; xác nhận chi phí trước khi gọi dịch vụ.
3. **Kiểm tra biểu đồ và phiên:** đối chiếu bộ lọc ngày, khoảng rỗng và biểu đồ với dữ liệu thử đã biết; kiểm tra phiên đang phát, tạm dừng và rời phiên. Chỉ đối chiếu production bằng thao tác đọc an toàn, không tạo dữ liệu giả.
4. **Nghiệm thu giao diện:** kiểm tra từng trang Admin ở desktop/mobile, focus và bàn phím, screen reader cơ bản, console/network và thiết bị thật; ghi phiên bản, bước thử và kết quả.
5. **Rà soát triển khai:** xác nhận secret, credential và cấu hình proxy thực tế. Nếu nhiều người dùng cùng proxy hoặc nhiều instance, thiết kế rate limit theo địa chỉ client đáng tin cậy và bộ đếm dùng chung trước khi coi giới hạn hiện tại phù hợp production.

Giữ checklist E mở đến khi có bằng chứng; không coi test trên database thử là nghiệm thu dữ liệu production.

Nguồn: `docs/plans/audio-tour-rebuild.md`, `frontend/README.md` và handoff giai đoạn D. Các mục mới cần ghi kèm bằng chứng hoặc lý do chưa kiểm tra.

## Lỗi kiểm thử chưa khắc phục

Test backend từng ghi đè tài khoản quản trị do dùng chung database; xem mục E. Theo quy ước người dùng, audio đã sinh được giữ lại kể cả khi sửa văn bản; admin tạo bài mới khi cần nội dung khác. Văn bản và audio có thể khác nhau nếu vẫn sửa bài cũ; API hiện không cho tạo bài thứ hai cùng địa điểm và ngôn ngữ.

## Kết quả kiểm tra giai đoạn F

- [x] Backend suite/package chạy trên instance MySQL riêng `127.0.0.1:33316`; datasource/Flyway test bị khóa vào instance này, tài khoản chỉ có quyền trên hai database thử. `mvnw.cmd package -q` đạt 172 test, không lỗi/skip.
- [x] JAR hiện hành đã tạo và chạy với profile prod trên cổng 18081, kết nối database thử riêng; chứa `index-BdqY_pPb.js` và `index-B7uIPb8B.css` từ build hiện hành. Không chạy với database ứng dụng.
- [ ] Trên JAR đã kiểm tra Home, đăng nhập admin, địa điểm, biểu đồ, QR, refresh cài đặt và API admin thiếu JWT trả 401. Chưa kiểm tra đầy đủ luồng Tourist/Admin, audio và phân quyền trên mọi route.
- [ ] Đã đối chiếu tĩnh HTML cũ với React: React chưa tự cập nhật cache online/offline, chưa nhớ ngôn ngữ ưa thích như player cũ, không có demo/mã mô phỏng tương đương; dashboard React cập nhật online thủ công thay vì polling. Chưa đủ bằng chứng chuyển route hoặc xóa HTML cũ.
- [ ] Docker CLI không có. GitHub Actions run `42759780122` thành công cả Maven và Docker trên commit `9f58926dc19215d2306cd76291adb80bf53cda35`; không chứng minh working tree hiện tại. Chưa push hoặc chạy CI cho thay đổi mới.
- [ ] Chưa kiểm tra điện thoại và HTTPS: GPS thực địa, camera QR, autoplay và host QR truy cập được từ thiết bị.

Chưa nghiệm thu đầy đủ các gate E–F; không chuyển route mặc định.

### Đề xuất hoàn tất mục F

1. **Duy trì cách ly test:** chạy MySQL thử trên cổng `33316` với tài khoản chỉ có quyền trên hai database test; xác minh datasource, Flyway và quyền trước backend suite/package. Không dùng database ứng dụng để thử migration hoặc rollback.
2. **Kiểm tra artifact hiện hành:** build lại frontend và JAR sau thay đổi cuối; chạy JAR với database thử, kiểm tra URL trực tiếp/refresh, điều hướng, JWT và các luồng Tourist/Admin, audio và ZIP. Đối chiếu asset trong JAR với frontend build.
3. **Khôi phục hoặc chốt khác biệt HTML cũ:** xử lý cache online/offline và ngôn ngữ ưa thích; đối chiếu demo, mã mô phỏng và cách cập nhật phiên online. Nếu bỏ chức năng nào, ghi quyết định chấp nhận trước khi chuyển route hoặc xóa HTML cũ.
4. **Kiểm tra Docker và CI:** cài hoặc dùng môi trường có Docker để build và kiểm tra image; chạy GitHub Actions trên commit chứa thay đổi sau khi được phép đưa lên. Kết quả commit cũ không thay thế CI hiện hành.
5. **Kiểm tra thực địa:** mở bản thử HTTPS từ điện thoại, quét QR với host truy cập được, kiểm tra GPS, camera và autoplay. Ghi thiết bị, trình duyệt, phiên bản và kết quả.
6. **Chốt phát hành:** diễn tập migration/rollback theo mục D, lưu bằng chứng và các giới hạn đã chấp nhận; chỉ sau đó cân nhắc chuyển route mặc định.

Không triển khai hoặc chuyển route khi các điều kiện bắt buộc chưa đạt hoặc chưa được người dùng chấp nhận rõ ràng.
