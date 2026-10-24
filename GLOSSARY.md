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
Bài thuyết minh của một POI trong một ngôn ngữ, gồm nội dung văn bản và file âm thanh (bản thu tay hoặc bản sinh tự động).
_Avoid_: Audio, script, bài giảng

**Language**:
Ngôn ngữ mà hệ thống cung cấp bản thuyết minh cho du khách, gồm mã và tên hiển thị. Một Language chỉ có nội dung tại những POI đã có Narration tương ứng; các POI còn lại hiện là chưa có bản thuyết minh.
_Avoid_: locale; "ngôn ngữ TTS" (đó là tập ngôn ngữ nhà cung cấp TTS đọc được, rộng hơn Language)

**Preferred Language** (ngôn ngữ ưa thích):
Ngôn ngữ mà du khách đã chủ động chọn hoặc hệ thống tự động nhận diện từ thiết bị, được lưu giữ qua các lượt nghe và các lần quét mã để tự động áp dụng cho các điểm tham quan kế tiếp.
_Avoid_: Selected language (đó chỉ là giá trị tạm thời trên giao diện hiện tại), guest language, default language (đó là ngôn ngữ fallback của hệ thống)

**Machine Translation** (MT):
Bước tạo nội dung cho Narration của một Language mới bằng dịch vụ dịch tự động, lấy bản `vi` làm nguồn. Kết quả là Narration mới; bản nguồn không đổi.
_Avoid_: auto-translate, dịch tự động (khi nói về khái niệm)

**Generated Audio** (bản thu tự sinh):
File MP3 do hệ thống tạo từ `textContent` của một Narration bằng TTS, lưu cùng thư mục với bản thu tay và tái sử dụng ở các lần gọi sau.
_Avoid_: Audio tự động, file ảo

**TTS** (Text-to-Speech):
Dịch vụ chuyển văn bản thành giọng nói; trong hệ thống này là ElevenLabs, sinh ra Generated Audio cho một Narration.
_Avoid_: Đọc văn bản, voice bot

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
