# ADR 0002: QR theo toà, chọn phòng trên trang chủ, không phụ thuộc GPS

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Accepted |
| Ngày | 2026-10-23 |
| Nhánh | `main` |

## Bối cảnh

Mã QR cũ trỏ thẳng vào `player.html?qr=<mã POI>`, nên mỗi mã khoá du khách vào đúng một POI: quét mã ở phòng nào thì chỉ nghe được phòng đó, không thấy các phòng khác. Yêu cầu mới: quét QR mở giao diện máy chủ, du khách chọn phòng hiện tại hoặc phòng khác, và hệ thống đề xuất điểm gần nhất.

Ba ràng buộc của môi trường:

- `navigator.geolocation` chỉ chạy trong secure context (HTTPS hoặc `localhost`). Cách demo hiện tại là `http://<IP LAN>:8080`, nên trình duyệt trên điện thoại không cấp vị trí.
- GPS trong nhà sai số 20–50 m, trong khi các phòng của Dinh Độc Lập trong dữ liệu mẫu cách nhau khoảng 14 m. GPS không phân biệt được phòng.
- Dữ liệu đã có cấu trúc cha–con: Venue là POI gốc, phòng là POI con. Không cần bảng mới.

## Quyết định

1. **QR mang Venue, không mang phòng.** Một mã cho mỗi toà: `http://<IP>:8080/?venue=<id POI gốc>`. Không phải dán và quét lại mã ở từng phòng.
2. **Trang chủ (`index.html`) là màn chọn.** Mở từ QR, trang hiện banner "Bạn đang ở <Venue>", danh sách phòng của toà lên trước, các POI khác vẫn chọn được. `player.html` chỉ còn là trang phát.
3. **GPS chỉ là phần nâng cấp, không phải điều kiện.** Khi có vị trí và sai số ≤ 30 m, banner ghi "Gần nhất: <phòng> · <khoảng cách>" kèm nút nghe. Sai số lớn hơn thì chỉ nói tới mức Venue.
4. **Không có GPS thì vẫn dùng được.** Danh sách sắp theo: cùng Venue trước, rồi `priority` giảm dần, rồi tên A–Z; banner ghi rõ chưa xác định được vị trí.
5. **Tham số sai không làm hỏng trang.** `?venue=` trỏ vào phòng con thì dùng Venue cha; id không tồn tại thì bỏ qua và hiện toàn bộ danh sách.
6. **Link cũ vẫn chạy.** `player.html?qr=...` giữ nguyên hành vi phát thẳng. `player.html` không có `locationId` lẫn `qr` thì chuyển về trang chọn thay vì phát POI mặc định.
7. **Nguồn kích hoạt ghi đúng.** Đến từ QR thì `triggerType = QR_CODE`, tự vào trang rồi chọn thì `MANUAL`; heartbeat mang giá trị này xuống playback log.

## Các lựa chọn đã cân nhắc và lý do loại

| Lựa chọn | Lý do loại |
| --- | --- |
| QR riêng cho từng phòng | Bắt du khách quét lại khi đi sang phòng khác; không giải quyết được yêu cầu "chọn phòng khác tuỳ ý". |
| Chỉ dựa vào GPS để biết đang ở đâu | Không chạy trên HTTP LAN; trong nhà còn sai số lớn hơn khoảng cách giữa các phòng. |
| Bắt buộc HTTPS + GPS | Phải cài chứng chỉ trên từng điện thoại demo, và vẫn không phân biệt được phòng trong nhà. |
| Thêm bảng/cột `venue` riêng | Dữ liệu cha–con sẵn có đã đủ; thêm cột là thêm trạng thái phải đồng bộ. |
| Tự động phát thuyết minh ngay khi quét | Trình duyệt di động chặn autoplay; du khách mất quyền chọn phòng. |

## Hệ quả

Tích cực:

- Một mã QR cho mỗi toà, dán một lần; du khách đổi phòng không cần quét lại.
- Hoạt động đầy đủ trên HTTP LAN — đúng môi trường demo — vì không phụ thuộc GPS.
- Không thêm bảng, không thêm dependency; `venue` chỉ là POI gốc đã có.
- Playback log phân biệt được khách đến từ QR và khách tự chọn.

Tiêu cực:

- Trên HTTP, gợi ý "gần nhất" dừng ở mức toà; muốn gợi ý phòng phải chạy HTTPS.
- GPS trong nhà vẫn có thể chọn nhầm phòng dù sai số ≤ 30 m; ngưỡng này là con số hợp lý trên giấy, chưa đo trên thiết bị thật.
- Banner chỉ hiện khi mở bằng link có `?venue=`; khách gõ tay địa chỉ máy chủ sẽ thấy trang bình thường.

## Khi nào xem lại quyết định

- Có HTTPS ổn định trong môi trường demo, hoặc dùng được định vị trong nhà (BLE beacon, WiFi fingerprint).
- Một Venue có nhiều tầng và cần phân biệt phòng theo tầng.
- Số phòng trong một toà vượt quá mức danh sách cuộn được, cần tìm kiếm/lọc riêng.

## Tham chiếu

- `GLOSSARY.md`: Venue, Current Room.
- `src/main/resources/static/index.html`: banner Venue, gợi ý theo khoảng cách.
- `src/main/resources/static/player.html`: nhận `trigger`, chuyển về trang chọn khi thiếu tham số.
- `src/main/resources/static/dashboard.html`: sinh QR theo Venue đang chọn.
- `src/main/java/vn/edu/sgu/audiotour/service/LiveAudience.java`: ghi `triggerType` vào playback log.
