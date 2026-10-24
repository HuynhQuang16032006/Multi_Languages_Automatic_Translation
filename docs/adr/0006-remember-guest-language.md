# ADR 0006: Ghi nhớ ngôn ngữ ưa thích của du khách và hỗ trợ đổi bất cứ lúc nào

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Accepted |
| Ngày | 2026-10-24 |
| Nhánh | `main` |

## Bối cảnh

Du khách tham quan sử dụng ứng dụng di động / web-app qua hai kênh: quét mã QR dán tại địa điểm/phòng ban hoặc tự truy cập bản đồ trên web. Vì hệ thống hướng tới trải nghiệm không rào cản (không bắt buộc đăng ký tài khoản), du khách hoàn toàn ẩn danh.

Hiện tại:
- Khi khách mở `index.html`, biến `selectedLanguage` bị gán cứng mặc định `vi`. Mỗi lần mở bottom sheet của một POI thì lại bị reset về `vi`.
- Khi khách chuyển sang `player.html` bằng link hoặc quét mã QR, ngôn ngữ chỉ lấy từ tham số `?lang=` hoặc fallback về `vi`. Khi khách đổi ngôn ngữ trên player, giá trị không được lưu lại; nếu khách quay về trang chính hoặc quét một mã QR khác, ngôn ngữ lại bị rơi về `vi`.
- Trang `index.html` chưa có nút đổi ngôn ngữ toàn cục trên thanh điều hướng; khách phải bấm vào một POI bất kỳ rồi mở bottom sheet thì mới thấy nút đổi ngôn ngữ.
- Khi khách đổi ngôn ngữ ngay lúc đang nghe thuyết minh trên `player.html`, bộ đếm thời gian nghe chưa được chốt cho ngôn ngữ cũ, gây sai lệch số liệu thống kê `PlaybackLog` và `AudienceSession` trên dashboard quản trị.

Do đó, cần một thiết kế nhất quán để hệ thống ghi nhớ lựa chọn ngôn ngữ của du khách xuyên suốt hành trình, hỗ trợ đổi ngôn ngữ bất cứ lúc nào, đồng thời xử lý trơn tru các kịch bản chuyển đổi âm thanh và thống kê.

## Quyết định

1. **Khái niệm `Preferred Language` được lưu trữ ở Client (`localStorage`)**:
   - Sử dụng khóa `audiotour_preferred_lang` trong `localStorage` để lưu mã ngôn ngữ ưa thích của du khách (ví dụ `"en"`).
   - Vì du khách ẩn danh, lưu trữ tại `localStorage` giúp backend giữ nguyên tính chất RESTful stateless, không cần session hay cookie phía server.

2. **Thứ tự ưu tiên nhận diện ngôn ngữ (Resolution Hierarchy)**:
   Khi khởi tạo trang (`index.html` hoặc `player.html`), ngôn ngữ được xác định theo thứ tự:
   - **Ưu tiên 1**: Tham số URL (`?lang=...`). Nếu có và hợp lệ, lập tức ghi nhận vào `localStorage` làm `Preferred Language`.
   - **Ưu tiên 2**: Giá trị đã lưu trong `localStorage['audiotour_preferred_lang']`.
   - **Ưu tiên 3**: Nhận diện tự động theo ngôn ngữ trình duyệt của thiết bị (`navigator.language`, ví dụ `en-US` lấy tiền tố `en`), nếu nằm trong danh sách ngôn ngữ hệ thống hỗ trợ.
   - **Ưu tiên 4 (Fallback cuối cùng)**: Ngôn ngữ mặc định hệ thống (`vi`).

3. **Kiểm định động với danh mục hệ thống (Dynamic Validation)**:
   Giá trị ngôn ngữ sau khi nạp luôn được đối chiếu với danh sách trả về từ `GET /api/languages`. Nếu mã ngôn ngữ đã lưu không còn tồn tại trong DB (ví dụ do Admin đã xóa ngôn ngữ đó trên Dashboard), hệ thống tự động rơi về `vi` và làm mới lại `localStorage`.

4. **Điểm chạm đổi ngôn ngữ đa dạng (Touchpoints - Đổi bất cứ lúc nào)**:
   - **Thanh Header của `index.html`**: Bổ sung bộ chọn ngôn ngữ toàn cục (Global Language Picker). Du khách quốc tế khi vừa vào trang chủ có thể đổi ngay sang tiếng mẹ đẻ mà không cần bấm chọn POI.
   - **Bottom Sheet POI trên `index.html`**: Đồng bộ với `Preferred Language`, cho phép xem trước và đổi ngôn ngữ riêng tại điểm. Thao tác đổi tại đây cập nhật ngay `Preferred Language`.
   - **Thanh điều khiển trên `player.html` (`langToggle`)**: Đồng bộ và cho phép chuyển đổi ngay trong khi đang ở màn hình nghe thuyết minh.
   - **Đồng bộ đa tab (Cross-tab Sync)**: Lắng nghe sự kiện `storage` (`window.addEventListener('storage', ...)`) để đồng bộ trạng thái hiển thị thời gian thực nếu du khách mở đồng thời nhiều tab hoặc cửa sổ.

5. **Hành vi chuyển đổi âm thanh và phiên thống kê (Mid-Playback Lifecycle)**:
   - Khi du khách đổi ngôn ngữ trên `player.html`:
     - Nếu âm thanh đang phát (`playing`), nạp stream audio của ngôn ngữ mới, reset vị trí phát về `0:00` và tiếp tục phát; nếu đang tạm dừng (`paused`), nạp stream mới và giữ nguyên trạng thái tạm dừng.
     - Về thống kê: Gửi ngay `POST /api/analytics/heartbeat` với `action: LEAVE` kèm thời lượng nghe đã tích lũy của ngôn ngữ cũ để chốt chính xác `PlaybackLog` của ngôn ngữ đó; sau đó reset bộ đếm thời gian nghe về 0 và bắt đầu chu kỳ `PING` với ngôn ngữ mới.

6. **Xử lý khi POI chưa có bản thuyết minh cho Preferred Language (Missing Narration Fallback)**:
   - Nếu du khách có `Preferred Language` là `en` nhưng di chuyển tới một POI chỉ mới có thuyết minh tiếng `vi`:
     - Hệ thống **không** tự ý ghi đè `localStorage` của khách.
     - Hiển thị thông báo rõ ràng kèm nút chuyển tạm thời sang `vi` cho riêng POI này. Khi du khách di chuyển tiếp sang POI khác có hỗ trợ `en`, bài thuyết minh tiếng `en` sẽ tự động hiển thị lại theo đúng sở thích của khách.
     - Nút "Tải nghe Offline" (ZIP) tại POI đó sẽ tạm thời bị vô hiệu hóa kèm chú thích, tuân thủ đúng quy tắc FR-PKG-03 (không tự ý đóng gói sai ngôn ngữ).

## Các lựa chọn đã cân nhắc và lý do loại

| Lựa chọn | Lý do loại |
| --- | --- |
| Lưu `Preferred Language` trên Backend (Session/DB) | Đòi hỏi du khách phải đăng nhập hoặc server phải duy trì session stateful (cookie), đi ngược lại kiến trúc stateless RESTful và mục tiêu "du khách tiếp cận không rào cản" của PRD. |
| Tự động đổi đè `localStorage` về `vi` khi gặp POI thiếu thuyết minh | Làm mất sở thích ngôn ngữ của du khách nước ngoài trong các điểm tham quan tiếp theo của hành trình. |
| Mở rộng toàn bộ nhãn tĩnh giao diện sang đa ngôn ngữ (Full i18n) | Vượt quá phạm vi cốt lõi của đề tài (thuyết minh audio POI), làm phức tạp hóa mã HTML/JS tĩnh trong khi giao diện vốn rất tối giản và trực quan. |
| Chỉ cho phép đổi ngôn ngữ trên `player.html` | Du khách nước ngoài dùng bản đồ và danh sách POI ở `index.html` sẽ không biết trang web có hỗ trợ tiếng của mình cho đến khi mở màn hình nghe. |

## Hệ quả

- Du khách có trải nghiệm mượt mà, chuyển đổi ngôn ngữ linh hoạt tại mọi màn hình mà không bị mất thiết lập.
- Số liệu thống kê thời lượng nghe trên Dashboard quản trị được phân tách chính xác theo từng ngôn ngữ.
- Không phát sinh thêm API backend, không phát sinh thêm thư viện ngoài, giữ nguyên kiến trúc HTML tĩnh + vanilla JS.
