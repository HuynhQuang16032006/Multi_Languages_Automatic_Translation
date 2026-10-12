package vn.edu.sgu.audiotour.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.edu.sgu.audiotour.entity.*;
import vn.edu.sgu.audiotour.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final LanguageRepository languageRepository;
    private final LocationRepository locationRepository;
    private final NarrationRepository narrationRepository;
    private final TourRepository tourRepository;
    private final PlaybackLogRepository playbackLogRepository;

    @Override
    public void run(String... args) {
        if (languageRepository.count() == 0) {
            // 1. Ngôn ngữ hỗ trợ
            Language vi = languageRepository.save(new Language(null, "vi", "Tiếng Việt"));
            Language en = languageRepository.save(new Language(null, "en", "English"));

            // 2. Điểm tham quan & Trạm dừng xe buýt (Quận 4 & Quận 1 theo slide)
            // 2.1. Trạm xe buýt Khánh Hội (Quận 4)
            Location tramKhanhHoi = new Location();
            tramKhanhHoi.setName("Trạm dừng xe buýt Khánh Hội");
            tramKhanhHoi.setAddress("Đường Hoàng Diệu, Phường Khánh Hội, Quận 4, TP.HCM");
            tramKhanhHoi.setLatitude(10.7602);
            tramKhanhHoi.setLongitude(106.7031);
            tramKhanhHoi.setRadiusInMeters(35.0);
            tramKhanhHoi.setPriority(2);
            tramKhanhHoi.setQrCode("QR-KHANH-HOI-01");
            tramKhanhHoi.setImageUrl("https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600");
            tramKhanhHoi = locationRepository.save(tramKhanhHoi);

            // 2.2. Trạm xe buýt Vĩnh Hội (Quận 4)
            Location tramVinhHoi = new Location();
            tramVinhHoi.setName("Trạm dừng xe buýt Vĩnh Hội");
            tramVinhHoi.setAddress("Đường Khánh Hội, Phường Vĩnh Hội, Quận 4, TP.HCM");
            tramVinhHoi.setLatitude(10.7585);
            tramVinhHoi.setLongitude(106.7012);
            tramVinhHoi.setRadiusInMeters(30.0);
            tramVinhHoi.setPriority(1);
            tramVinhHoi.setQrCode("QR-VINH-HOI-01");
            tramVinhHoi.setImageUrl("https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=600");
            tramVinhHoi = locationRepository.save(tramVinhHoi);

            // 2.3. Trạm xe buýt Xóm Chiếu (Quận 4)
            Location tramXomChieu = new Location();
            tramXomChieu.setName("Trạm dừng xe buýt Xóm Chiếu");
            tramXomChieu.setAddress("Đường Đoàn Văn Bơ, Phường Xóm Chiếu, Quận 4, TP.HCM");
            tramXomChieu.setLatitude(10.7631);
            tramXomChieu.setLongitude(106.7075);
            tramXomChieu.setRadiusInMeters(30.0);
            tramXomChieu.setPriority(3);
            tramXomChieu.setQrCode("QR-XOM-CHIEU-01");
            tramXomChieu.setImageUrl("https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=600");
            tramXomChieu = locationRepository.save(tramXomChieu);

            // 2.4. Dinh Độc Lập (Địa điểm gốc có khu vực con)
            Location dinhDocLap = new Location();
            dinhDocLap.setName("Dinh Độc Lập");
            dinhDocLap.setAddress("135 Nam Kỳ Khởi Nghĩa, Bến Thành, Quận 1, TP.HCM");
            dinhDocLap.setLatitude(10.7770);
            dinhDocLap.setLongitude(106.6953);
            dinhDocLap.setRadiusInMeters(100.0);
            dinhDocLap.setPriority(5);
            dinhDocLap.setQrCode("QR-DINH-DOC-LAP-01");
            dinhDocLap.setImageUrl("https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=600");
            dinhDocLap = locationRepository.save(dinhDocLap);

            // Các phòng ban bên trong Dinh Độc Lập
            Location phongKhanhTiet = new Location();
            phongKhanhTiet.setName("Phòng Khánh Tiết");
            phongKhanhTiet.setAddress("Tầng trệt, Dinh Độc Lập");
            phongKhanhTiet.setLatitude(10.7771);
            phongKhanhTiet.setLongitude(106.6954);
            phongKhanhTiet.setRadiusInMeters(15.0);
            phongKhanhTiet.setPriority(2);
            phongKhanhTiet.setQrCode("QR-PHONG-KHANH-TIET");
            phongKhanhTiet.setImageUrl("https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600");
            phongKhanhTiet.setParentLocation(dinhDocLap);
            phongKhanhTiet = locationRepository.save(phongKhanhTiet);

            Location phongDaiYen = new Location();
            phongDaiYen.setName("Phòng Đại Yến");
            phongDaiYen.setAddress("Tầng 1, Dinh Độc Lập");
            phongDaiYen.setLatitude(10.7772);
            phongDaiYen.setLongitude(106.6955);
            phongDaiYen.setRadiusInMeters(15.0);
            phongDaiYen.setPriority(2);
            phongDaiYen.setQrCode("QR-PHONG-DAI-YEN");
            phongDaiYen.setImageUrl("https://images.unsplash.com/photo-1541971875076-8f970d573be6?w=600");
            phongDaiYen.setParentLocation(dinhDocLap);
            locationRepository.save(phongDaiYen);

            // 3. Thuyết minh hoàn chỉnh (Chi tiết văn bản & File âm thanh MP3 thực tế)
            createNarration(dinhDocLap, vi,
                    "Dinh Độc Lập, nay là Di tích Lịch sử Dinh Độc Lập, tọa lạc tại số 135 đường Nam Kỳ Khởi Nghĩa, Quận 1, Thành phố Hồ Chí Minh. Công trình được khởi công xây dựng vào năm 1962 trên nền Dinh Norodom cũ, theo đồ án thiết kế của kiến trúc sư tài hoa Ngô Viết Thụ. Nơi đây là chứng nhân lịch sử đánh dấu thời khắc hòa bình, thống nhất non sông vào trưa ngày 30 tháng 4 năm 1975.",
                    "dinh-doc-lap-vi.mp3", 150);

            createNarration(phongKhanhTiet, vi,
                    "Chào mừng quý khách đến với Phòng Khánh Tiết tại Dinh Độc Lập. Đây là căn phòng có diện tích lớn nhất trong Dinh, được thiết kế trang trọng với sức chứa lên đến hơn năm trăm người. Căn phòng này từng là nơi diễn ra các cuộc họp nội các quan trọng, các buổi lễ tiếp đón nguyên thủ quốc gia và các sự kiện ngoại giao lịch sử trước ngày 30 tháng 4 năm 1975.",
                    "phong-khanh-tiet-vi.mp3", 120);

            createNarration(phongKhanhTiet, en,
                    "Welcome to the Chamber of Reception at the Independence Palace. This is the largest hall in the palace, designed with a capacity of over five hundred people. It historically served as the venue for crucial cabinet meetings and state receptions prior to 1975.",
                    "phong-khanh-tiet-en.mp3", 115);

            createNarration(phongDaiYen, vi,
                    "Phòng Đại Yến tọa lạc tại tầng một của Dinh Độc Lập. Đây là nơi từng tổ chức các buổi chiêu đãi dạ tiệc cấp nhà nước với sức chứa hơn một trăm khách, nổi bật với phong cách kiến trúc kết hợp hài hòa giữa truyền thống phương Đông và hiện đại phương Tây.",
                    "phong-dai-yen-vi.mp3", 100);

            createNarration(tramKhanhHoi, vi,
                    "Chào mừng quý khách đến với trạm dừng xe buýt Khánh Hội, Quận 4. Vùng đất Khánh Hội xưa vốn là một cù lao trù phú bên dòng kênh Bến Nghé, gắn liền với thương cảng Sài Gòn sầm uất và sự giao lưu văn hóa của cư dân Nam Bộ từ thời khẩn hoang.",
                    "tram-khanh-hoi-vi.mp3", 90);

            createNarration(tramVinhHoi, vi,
                    "Trạm dừng xe buýt Vĩnh Hội đưa quý khách đến với không gian giao thương sầm uất của Quận 4. Nơi đây từng là đầu mối vận chuyển hàng hóa đường thủy nhộn nhịp, nay là khu vực phát triển năng động với nhiều nét ẩm thực đường phố đặc sắc.",
                    "tram-vinh-hoi-vi.mp3", 85);

            createNarration(tramXomChieu, vi,
                    "Trạm dừng Xóm Chiếu là nơi lưu giữ nét văn hóa làng nghề truyền thống nổi tiếng từ thế kỷ trước. Nơi đây từng là trung tâm dệt chiếu cói nức tiếng phương Nam, nay trở thành thiên đường ẩm thực đường phố sầm uất bậc nhất của Thành phố Hồ Chí Minh.",
                    "tram-xom-chieu-vi.mp3", 95);

            // 4. Tuyến tham quan (Tours)
            Tour tourQuan4 = new Tour();
            tourQuan4.setName("Tour Tuyến Xe Buýt Lịch Sử Quận 4");
            tourQuan4.setDescription("Hành trình qua các trạm dừng lịch sử Khánh Hội, Vĩnh Hội và Xóm Chiếu");
            tourQuan4.setEstimatedDurationMinutes(60);
            tourQuan4.setImageUrl("https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600");
            tourQuan4.setLocations(List.of(tramKhanhHoi, tramVinhHoi, tramXomChieu));
            tourRepository.save(tourQuan4);

            Tour tourDinhDocLap = new Tour();
            tourDinhDocLap.setName("Tour Khám phá Dinh Độc Lập");
            tourDinhDocLap.setDescription("Hành trình tham quan di tích lịch sử đặc biệt cấp quốc gia");
            tourDinhDocLap.setEstimatedDurationMinutes(90);
            tourDinhDocLap.setImageUrl("https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=600");
            tourDinhDocLap.setLocations(List.of(dinhDocLap, phongKhanhTiet, phongDaiYen));
            tourRepository.save(tourDinhDocLap);

            // 5. Nhật ký nghe mẫu (Playback Logs cho Dashboard)
            recordSampleLog(tramKhanhHoi, "vi", 85, TriggerType.QR_CODE, 10.7602, 106.7031);
            recordSampleLog(tramKhanhHoi, "en", 80, TriggerType.QR_CODE, 10.7602, 106.7031);
            recordSampleLog(tramXomChieu, "vi", 90, TriggerType.GEOFENCE, 10.7631, 106.7075);
            recordSampleLog(tramXomChieu, "vi", 95, TriggerType.QR_CODE, 10.7631, 106.7075);
            recordSampleLog(phongKhanhTiet, "vi", 110, TriggerType.QR_CODE, 10.7771, 106.6954);
            recordSampleLog(phongKhanhTiet, "en", 115, TriggerType.QR_CODE, 10.7771, 106.6954);
            recordSampleLog(tramVinhHoi, "vi", 75, TriggerType.GEOFENCE, 10.7585, 106.7012);
        }
    }

    private void createNarration(Location loc, Language lang, String text, String audio, int duration) {
        Narration narration = new Narration();
        narration.setLocation(loc);
        narration.setLanguage(lang);
        narration.setTextContent(text);
        narration.setAudioUrl(audio);
        narration.setDurationInSeconds(duration);
        narrationRepository.save(narration);
    }

    private void recordSampleLog(Location loc, String lang, int duration, TriggerType type, double lat, double lng) {
        PlaybackLog log = new PlaybackLog();
        log.setLocation(loc);
        log.setLanguageCode(lang);
        log.setListenDurationSeconds(duration);
        log.setTriggerType(type);
        log.setLatitude(lat);
        log.setLongitude(lng);
        log.setDeviceId("demo-device-sgu");
        log.setCreatedAt(LocalDateTime.now().minusHours((long) (Math.random() * 24)));
        playbackLogRepository.save(log);
    }
}
