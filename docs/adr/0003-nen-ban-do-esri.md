# ADR 0003: Nền bản đồ dùng Esri thay OpenStreetMap

| Trường | Nội dung |
| --- | --- |
| Trạng thái | Accepted |
| Ngày | 2026-10-23 |
| Nhánh | `main` |

## Bối cảnh

ADR 0001 chọn OpenStreetMap làm nguồn tile duy nhất cho bản đồ khách. Khi demo trên điện thoại nối cùng router với laptop, DNS của điện thoại chặn `tile.openstreetmap.org`: Leaflet và trang vẫn tải, nhưng khung bản đồ trắng vì không có tile. Laptop thì tải tile bình thường (HTTP 200), nên đây là chặn ở phía thiết bị, không phải lỗi code.

## Quyết định

Đổi nguồn tile sang **Esri World Street Map**:

```
https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}
```

Kèm attribution của Esri. Không cần API key, chỉ một lớp bản đồ nền sáng duy nhất (đã bỏ lớp "Tối" đảo màu bằng CSS filter).

## Các lựa chọn đã cân nhắc và lý do loại

| Lựa chọn | Lý do loại |
| --- | --- |
| Giữ OSM, sửa DNS điện thoại | Vẫn là cách rẻ nhất, nhưng không kiểm soát được thiết bị của người demo; mạng khác có thể chặn tiếp. |
| CARTO | Chính sách hiện tại yêu cầu API key cho basemap. |
| Google Maps tile | Trái điều khoản sử dụng, cần API key. |
| Tự dựng tile offline | Nặng hơn nhiều so với nhu cầu demo; phải chuẩn bị dữ liệu theo từng khu vực. |

## Hệ quả

Tích cực:

- Bản đồ hiện trên điện thoại dù DNS của điện thoại chặn OSM.
- Không thêm dependency, không thêm key, chỉ đổi URL và attribution.

Tiêu cực:

- Phụ thuộc Esri; nếu Esri đổi chính sách hoặc bị chặn thì lại phải đổi nguồn.
- Bản đồ nền có phong cách khác OSM và sáng hơn giao diện tối của trang; đã bỏ lớp "Tối" nên không còn chế độ bản đồ tối.
- Chưa kiểm chứng Esri trên mọi mạng; mới test từ mạng demo.

## Khi nào xem lại quyết định

- Mạng demo chặn luôn `server.arcgisonline.com`.
- Cần bản đồ offline hoặc cần kiểm soát hoàn toàn dữ liệu nền.
- Esri thay đổi điều khoản sử dụng hoặc yêu cầu key.

## Tham chiếu

- ADR 0001: giao diện khách dùng HTML tĩnh + Leaflet.
- `src/main/resources/static/index.html`: `TILE_URL`, `TILE_ATTRIBUTION`.
