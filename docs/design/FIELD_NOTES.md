# Field Notes — bản duyệt A, phiên bản 0.2

## Mở bản thiết kế

Mở `field-notes-prototype.html` bằng Chrome hoặc Edge. File độc lập, không cần backend hay npm. Thanh điều hướng chỉ chuyển giữa bốn màn hình thiết kế; đây không phải điều hướng production. Thu nhỏ cửa sổ để xem bố cục mobile.

## Quyết định thiết kế đề xuất

- Nền giấy `#F7F4ED`, mặt nội dung `#FFFEFA`, chữ chính `#173B34`, chữ phụ `#62685F`, CTA `#A6402D` với chữ trắng, đường phân cách `#D8D3C8`.
- Heading dùng Playfair Display 500; UI dùng Inter 400/500/600. Kiểm tra dấu tiếng Việt bằng nội dung trong prototype. Font fallback: Georgia/Arial.
- Heading mobile 36–46px; desktop 40–70px. Nội dung production tối thiểu 16px; metadata 12px. Prototype dùng một số chữ nhỏ hơn để ghi chú thiết kế, không phải chuẩn body production.
- Spacing 4/8/12/16/24/32/48/64px. Gutter mobile 20px, desktop 48px. Chiều rộng ngoài tối đa 1328px.
- Control chính cao tối thiểu 48px; focus viền 3px; motion 150–250ms, tắt với reduced motion. Không parallax, waveform giả hoặc tô sáng transcript khi chưa có timestamp.
- Home: lời giới thiệu và ảnh lớn, sau đó danh sách địa điểm. Explore: search, sort, danh sách, mini-player. Detail: ảnh đơn, thông tin, CTA nghe, văn bản và khu vực con. Player: nền xanh tối, điều khiển riêng, transcript sáng để đọc.

## Phạm vi tương tác bản duyệt

Chuyển bốn màn hình, tìm kiếm trên ba tên minh họa, sắp xếp A–Z, tua thanh tiến trình và đổi trạng thái play/pause minh họa. Không có audio thật hoặc hẹn giờ phát. Các card đều mở cùng mẫu chi tiết Dinh Độc Lập và ghi rõ điều đó.

Camera, GPS, bản đồ, chọn ngôn ngữ và tải ZIP chưa tích hợp trong prototype. Không coi các nút disabled là chức năng đã hoàn thành. Bản chính thức phải dùng API thật và giữ tính năng cũ.

Ảnh từ Unsplash chỉ phục vụ bố cục; các ảnh hiện tại không khớp tên địa điểm, không được đưa vào production như ảnh xác thực. Cần chọn ảnh đúng địa điểm và quyền sử dụng trước khi duyệt hình ảnh cuối. Google Fonts và ảnh cần mạng.

## Cơ sở ui-ux-pro-max

Đã tra design system `travel editorial discovery`, sau đó tra lại `editorial grid magazine` vì gợi ý Aurora/gradient không phù hợp hướng đã duyệt. Chọn editorial grid và bộ chữ Playfair Display/Inter; không dùng Aurora hoặc scroll storytelling cho điều hướng. Tra `keyboard focus sticky navigation` cho focus và phần bù nội dung khi có thanh cố định.

## Kiểm tra đã chạy

Chrome headless qua DevTools Protocol: bốn màn hình ở 375×1100 và 1440×1100. Sau sửa min-width của grid, không phát hiện tràn ngang ở tám trường hợp. Search không khớp trả thông báo rỗng. Đã chụp tám ảnh; xem trực quan ảnh Home desktop, Explore desktop và Player mobile trong quá trình làm.

Chưa audit đầy đủ accessibility, tương phản, tab order, console/network, thiết bị thật hoặc production build. Đây là bản duyệt đầu, chưa phải nghiệm thu Giai đoạn A. Cần duyệt ảnh, bố cục và chi tiết bốn trang; bổ sung loading/error/offline trước khi khóa thiết kế triển khai.

## Quy tắc nghiệp vụ đã chốt cho giai đoạn sau

Từ chối xóa địa điểm nếu có địa điểm con, bài thuyết minh hoặc lịch sử nghe. Bổ sung ẩn/hiện, không thay xóa bằng xóa dây chuyền. Không thay đổi backend hoặc schema trong bản duyệt A.

## Bổ sung GPS / Nearby Discovery

Phần này là yêu cầu thiết kế và nghiệm thu mới. HTML phiên bản 0.1 chưa thể hiện các bố cục này; không coi cập nhật tài liệu là đã tích hợp GPS hoặc bản đồ.

### Home

Đặt mục **Địa điểm gần bạn** sau hero và trước danh sách giới thiệu. Trước khi có vị trí, hiển thị giải thích: “Dùng vị trí của bạn để tìm các địa điểm gần nhất. Khoảng cách tính theo đường chim bay.” Kèm nút **Dùng vị trí của tôi**, không tự bật hộp thoại quyền lúc vào trang.

Khi có vị trí và POI thật, hiển thị tối đa 5 địa điểm gần nhất, sắp xếp tăng dần theo khoảng cách. Nếu chỉ có 1–2 địa điểm hợp lệ thì hiển thị đúng số lượng, không bù địa điểm giả. Mỗi card có tên, ảnh đúng nếu có, địa chỉ và nhãn khoảng cách đường chim bay. Link **Xem tất cả địa điểm gần bạn** mở Explore với bộ lọc Gần tôi và dùng lại vị trí còn hợp lệ.

### Explore

Nút **Gần tôi** bật sắp xếp khoảng cách Haversine tăng dần cho tập kết quả đang tìm kiếm. Khi chưa cấp quyền, người dùng đọc giải thích trước khi chọn tiếp tục. Có cách bỏ bộ lọc và trở lại thứ tự trước đó. Vị trí cập nhật không được làm mất từ khóa hoặc trạng thái lọc; tránh đảo thứ tự liên tục khi GPS dao động. Hiển thị trạng thái đang lấy vị trí và thời điểm vị trí cập nhật khi cần.

POI thiếu tọa độ hợp lệ vẫn có thể xuất hiện ở danh sách khám phá thông thường, nhưng không được gán khoảng cách hoặc tham gia xếp hạng Nearby.

### Explore Map: bố cục Field Notes

- Mobile: thanh tìm kiếm gọn ở trên, Leaflet ở giữa, nút **Vị trí của tôi** tách khỏi control zoom; bottom sheet nền giấy chứa danh sách gần nhất hoặc địa điểm đang chọn. Marker dùng xanh mực, trạng thái chọn dùng đất nung; vị trí người dùng dùng ký hiệu riêng và vòng độ chính xác.
- Bottom sheet có trạng thái gọn/mở rộng và nút mở/thu gọn tối thiểu 48px; không bắt người dùng chỉ kéo bằng ngón tay. Không che attribution, control bản đồ, mini-player hoặc thanh điều hướng.
- Chọn marker làm nổi bật marker và mở thẻ tên, địa chỉ, khoảng cách đường chim bay nếu có vị trí, cùng nút **Xem địa điểm**. Chọn một hàng trong danh sách chọn cùng địa điểm trên bản đồ. Không tự phát audio chỉ vì chọn marker.
- Desktop: danh sách và thẻ chi tiết cạnh bản đồ, không kéo rộng bottom sheet mobile thành một panel phủ toàn màn hình. Duy trì cùng dữ liệu và lựa chọn giữa hai bố cục.
- Người dùng vẫn xem được marker khi từ chối GPS. Không coi tâm bản đồ mặc định là vị trí của họ. Khi họ đang kéo bản đồ, không tự kéo tâm về vị trí mới; chỉ căn lại khi chọn **Vị trí của tôi**.

### Quyền vị trí và trạng thái lỗi

Trạng thái cần thiết: chưa yêu cầu, đang lấy vị trí, có vị trí, từ chối, timeout, không khả dụng, trình duyệt không hỗ trợ, kết nối không an toàn và độ chính xác thấp. Với quyền bị từ chối, hướng dẫn bật lại trong trình duyệt và cho phép tiếp tục khám phá; không gọi xin quyền lặp. Timeout/không khả dụng có nút **Thử lại**. Dừng theo dõi khi người dùng tắt tính năng cần GPS và không còn tính năng khác dùng vị trí.

Dùng Geolocation API; không dùng IP location, GPS mô phỏng hoặc tọa độ mặc định để hiển thị kết quả “gần bạn”. Mô tả đúng dữ liệu vị trí mà heartbeat/log thực tế gửi lên server; không hứa “vị trí không rời thiết bị” khi cơ chế thống kê chưa bảo đảm điều đó.

### Khoảng cách, dữ liệu và API

Tái sử dụng `GET /api/locations/pois`, các tọa độ, `radiusInMeters`, `priority` và API đọc chi tiết hiện có. Tính Haversine phía client; không cần dịch vụ routing hoặc API Nearby riêng cho tập POI hiện tại. Sau khi bổ sung ẩn/hiện, API công khai phải loại địa điểm ẩn ở cả danh sách, chi tiết và tra QR theo quy tắc thống nhất; admin vẫn quản lý được dữ liệu đó.

Khoảng cách dùng mét/km và luôn có chú thích rõ “đường chim bay”; không đổi thành thời gian đi bộ, tuyến đường hay khoảng cách di chuyển. Không biểu diễn số lẻ như phép đo GPS chính xác tuyệt đối. Khi vị trí quá cũ hoặc độ chính xác thấp, hiển thị cảnh báo và không kích hoạt geofence từ phép đo không đáng tin cậy. Ngưỡng tuổi/độ chính xác phải chốt bằng kiểm tra hành vi cũ và test thiết bị trước triển khai.

Nếu API lỗi hoặc không có POI hợp lệ, hiển thị trạng thái thật và cách thử lại; không thay bằng địa điểm minh họa trong prototype. POI lấy từ cache phải có nhãn dữ liệu đã lưu và thời điểm cập nhật; không cam kết tile bản đồ hoạt động offline.

### Geofence và audio

Tách **dùng vị trí để khám phá** khỏi **bật thuyết minh tự động**. Có điều khiển bật/tắt riêng và giải thích trước khi bật; xử lý giới hạn autoplay của trình duyệt bằng thao tác người dùng khởi tạo audio. Không hứa theo dõi GPS nền khi trình duyệt/OS đình chỉ trang.

Giữ quy tắc ưu tiên hiện tại sau khi đọc chính xác mã nguồn và bổ sung test hồi quy. Khóa chuyển bài khi một bài đang phát hoặc người dùng chủ động tạm dừng; POI mới có thể tạo gợi ý thay vì giành quyền phát. Không phát lại trên mỗi GPS fix. Dùng trạng thái vào/ra vùng, hysteresis và cooldown đã kiểm chứng để chống dao động ở biên; định nghĩa thời điểm cho phép phát lại sau khi rời rồi quay lại. Chọn một POI theo thứ tự xác định khi nhiều vùng chồng nhau. Không ghi trùng lượt nghe khi cập nhật vị trí hoặc đổi route.

### Thứ tự và điều kiện nghiệm thu bổ sung

1. **Giai đoạn A:** cập nhật bố cục Home/Explore, thiết kế Explore Map mobile/desktop và trạng thái quyền/lỗi. Giữ điểm duyệt thiết kế trước code production. Bản trình diễn có dữ liệu phải gọi API thật; màn hình chưa kết nối chỉ hiển thị trạng thái chưa tải, không POI giả.
2. **Giai đoạn C, lát cắt Nearby:** viết test Haversine, tọa độ không hợp lệ, kết quả ít hơn 3 và sắp xếp; tích hợp Home/Explore bằng API thật. Kiểm tra grant/deny/timeout, tìm kiếm kết hợp Nearby và chuyển route không xin quyền lặp.
3. **Giai đoạn C, lát cắt Map:** Leaflet, marker thật, vị trí/vòng accuracy và bottom sheet; kiểm tra chọn marker/hàng danh sách, mở chi tiết đúng ID, control cảm ứng và bàn phím, không che nội dung ở 375px và desktop.
4. **Giai đoạn C, lát cắt geofence:** test GPS dao động, vùng chồng nhau, pause thủ công, đang phát bài khác, rời/quay lại, mất GPS và đổi ngôn ngữ. Dùng chuỗi tọa độ kiểm thử trong test, không giả lập thành GPS thật trên giao diện nghiệm thu.
5. Mỗi nhóm phải chụp và xem ảnh trình duyệt mobile/desktop, sửa lỗi bố cục trước khi đi tiếp. GPS/camera/autoplay cần kiểm tra thêm trên điện thoại với HTTPS. Chưa qua các kiểm tra này thì chưa đánh dấu hoàn thành.

Tra cứu ui-ux-pro-max cho bổ sung này: `location permission explanation denial` trả kết quả breadcrumb không phù hợp, đã bỏ; truy vấn lại `permission denied recovery` cho hướng dẫn phục hồi lỗi. `dragging movements` yêu cầu nút thay thế thao tác kéo cho bottom sheet. Các quyết định Geolocation/Haversine/geofence ở trên là yêu cầu sản phẩm và hướng dẫn kỹ thuật đề xuất, không gán cho kết quả tra cứu không liên quan.
