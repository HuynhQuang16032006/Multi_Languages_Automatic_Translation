# Audio Tour

Hệ thống thuyết minh tự động đa ngôn ngữ cho du khách tại TP. Hồ Chí Minh: POI được kích hoạt bằng mã QR hoặc vùng GPS, nội dung thuyết minh phát theo ngôn ngữ du khách chọn.

## Language

**POI** (Point of Interest):
Một điểm tham quan hoặc trạm dừng mà du khách có thể nghe thuyết minh; có thể là điểm gốc hoặc khu vực con của một điểm gốc.
_Avoid_: Location, địa danh (khi nói về khái niệm nghiệp vụ)

**Venue** (toà):
POI cấp cao nhất đại diện cho một toà nhà hoặc khu tham quan, chứa các phòng bên trong. Mã QR dán tại toà trỏ tới Venue, không trỏ tới từng phòng.
_Avoid_: Building, root location, địa điểm cha (khi nói về khái niệm nghiệp vụ)

**Current Room** (phòng hiện tại):
Phòng trong một Venue mà du khách đang đứng; khác với phòng du khách chọn để nghe. Xác định từ Venue trên mã QR và, khi thiết bị cho phép định vị, từ khoảng cách tới các phòng.
_Avoid_: Active room, selected room (đó là phòng du khách chọn, không nhất thiết là nơi đang đứng)

**Narration**:
Bài thuyết minh của một POI trong một ngôn ngữ, gồm nội dung văn bản và bản thu âm.
_Avoid_: Audio, script, bài giảng

**Tour**:
Một tuyến gồm nhiều POI theo thứ tự tham quan.
_Avoid_: Route, itinerary

**Playback Log**:
Ghi nhận ẩn danh một lượt nghe đã kết thúc: POI, ngôn ngữ, thời lượng nghe, cách kích hoạt.
_Avoid_: History, event, record

**Live Audience**:
Tập hợp các phiên nghe đang mở tại một thời điểm; là nguồn duy nhất của con số "đang truy cập".
_Avoid_: Online users, active users

**Heartbeat**:
Tín hiệu định kỳ do thiết bị gửi để báo phiên còn sống, đổi POI hoặc ngôn ngữ, hay kết thúc phiên.
_Avoid_: Signal (PING chỉ là một giá trị của heartbeat)

**Audience Session**:
Một thiết bị đang nghe một POI trong một ngôn ngữ, tính từ lúc join tới khi leave hoặc hết hạn heartbeat.
_Avoid_: Connection, client, device record
