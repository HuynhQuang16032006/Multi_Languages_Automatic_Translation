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

        seedMultilingualNarrations();
    }

    // Khởi tạo các bản thuyết minh đa ngôn ngữ (en, zh, ja) dựa trên bản tiếng Việt làm chuẩn (idempotent)
    private void seedMultilingualNarrations() {
        Language en = languageRepository.findByCode("en")
                .orElseGet(() -> languageRepository.save(new Language(null, "en", "English")));
        Language zh = languageRepository.findByCode("zh")
                .orElseGet(() -> languageRepository.save(new Language(null, "zh", "中文")));
        Language ja = languageRepository.findByCode("ja")
                .orElseGet(() -> languageRepository.save(new Language(null, "ja", "日本語")));

        Location tramKhanhHoi = locationRepository.findByQrCode("QR-KHANH-HOI-01").orElse(null);
        Location tramVinhHoi = locationRepository.findByQrCode("QR-VINH-HOI-01").orElse(null);
        Location tramXomChieu = locationRepository.findByQrCode("QR-XOM-CHIEU-01").orElse(null);
        Location dinhDocLap = locationRepository.findByQrCode("QR-DINH-DOC-LAP-01").orElse(null);
        Location phongKhanhTiet = locationRepository.findByQrCode("QR-PHONG-KHANH-TIET").orElse(null);
        Location phongDaiYen = locationRepository.findByQrCode("QR-PHONG-DAI-YEN").orElse(null);

        // 1. Tiếng Anh (English) cho các điểm còn thiếu
        if (tramKhanhHoi != null && narrationRepository.findByLocationIdAndLanguageCode(tramKhanhHoi.getId(), "en").isEmpty()) {
            createNarration(tramKhanhHoi, en,
                    "Welcome to Khanh Hoi bus stop in District 4. Khanh Hoi was historically a fertile islet along Ben Nghe canal, closely associated with the bustling Saigon commercial port and the rich cultural exchange of Southern Vietnamese settlers since the pioneering era.",
                    "tram-khanh-hoi-en.mp3", 90);
        }
        if (tramVinhHoi != null && narrationRepository.findByLocationIdAndLanguageCode(tramVinhHoi.getId(), "en").isEmpty()) {
            createNarration(tramVinhHoi, en,
                    "Vinh Hoi bus stop takes you to the bustling trading hub of District 4. Once a lively waterway freight transport terminal, it is now a dynamic developing area famous for its distinctive and vibrant street food culture.",
                    "tram-vinh-hoi-en.mp3", 85);
        }
        if (tramXomChieu != null && narrationRepository.findByLocationIdAndLanguageCode(tramXomChieu.getId(), "en").isEmpty()) {
            createNarration(tramXomChieu, en,
                    "Xom Chieu bus stop preserves the traditional handicraft village culture renowned since the last century. Once a famous center for woven sedge mats in the South, today it has transformed into one of the most vibrant street food paradises in Ho Chi Minh City.",
                    "tram-xom-chieu-en.mp3", 95);
        }
        if (dinhDocLap != null && narrationRepository.findByLocationIdAndLanguageCode(dinhDocLap.getId(), "en").isEmpty()) {
            createNarration(dinhDocLap, en,
                    "The Independence Palace, now the Independence Palace Historical Relic, is located at 135 Nam Ky Khoi Nghia Street, District 1, Ho Chi Minh City. Construction commenced in 1962 on the site of the former Norodom Palace, designed by the talented architect Ngo Viet Thu. This landmark stands as a historical witness marking the momentous era of peace and national reunification at noon on April 30, 1975.",
                    "dinh-doc-lap-en.mp3", 150);
        }
        if (phongDaiYen != null && narrationRepository.findByLocationIdAndLanguageCode(phongDaiYen.getId(), "en").isEmpty()) {
            createNarration(phongDaiYen, en,
                    "The Banquet Chamber is situated on the first floor of the Independence Palace. This hall previously hosted state banquets accommodating over one hundred distinguished guests, distinguished by an architectural design that harmoniously blends Eastern traditions with Western modern styles.",
                    "phong-dai-yen-en.mp3", 100);
        }

        // 2. Tiếng Trung (中文) cho các điểm
        if (tramKhanhHoi != null && narrationRepository.findByLocationIdAndLanguageCode(tramKhanhHoi.getId(), "zh").isEmpty()) {
            createNarration(tramKhanhHoi, zh,
                    "欢迎来到第四郡庆会社交巴士站。昔日的庆会地区是滨艺运河畔一块肥沃的岛屿，自拓荒时期起便与繁华的西贡商港以及南部居民的多元文化交流紧密相连。",
                    "tram-khanh-hoi-zh.mp3", 80);
        }
        if (tramVinhHoi != null && narrationRepository.findByLocationIdAndLanguageCode(tramVinhHoi.getId(), "zh").isEmpty()) {
            createNarration(tramVinhHoi, zh,
                    "永会社交巴士站带您走进第四郡繁华热闹的商贸天地。这里曾是水运货物集散的重要枢纽，如今已发展成为充满活力、汇聚丰富地道街头美食的特色街区。",
                    "tram-vinh-hoi-zh.mp3", 80);
        }
        if (tramXomChieu != null && narrationRepository.findByLocationIdAndLanguageCode(tramXomChieu.getId(), "zh").isEmpty()) {
            createNarration(tramXomChieu, zh,
                    "照村巴士站承载着上世纪闻名遐迩的传统手工艺村文化。这里曾是越南南方著名的草席编织中心，如今已蜕变成为胡志明市最繁华热闹的街头美食天堂之一。",
                    "tram-xom-chieu-zh.mp3", 85);
        }
        if (dinhDocLap != null && narrationRepository.findByLocationIdAndLanguageCode(dinhDocLap.getId(), "zh").isEmpty()) {
            createNarration(dinhDocLap, zh,
                    "独立宫位于胡志明市第一郡南圻起义街135号，1962年在诺罗敦宫旧址上建成，由建筑师吴曰树设计。1975年4月30日中午，这里见证了越南南北统一的历史时刻。",
                    "dinh-doc-lap-zh.mp3", 140);
        }
        if (phongKhanhTiet != null && narrationRepository.findByLocationIdAndLanguageCode(phongKhanhTiet.getId(), "zh").isEmpty()) {
            createNarration(phongKhanhTiet, zh,
                    "欢迎光临独立宫庆节厅。这是整座宫殿中面积最大的厅堂，设计典雅庄重，可容纳五百余人。在1975年4月30日之前，这里曾是举行重要内阁会议、接待各国元首以及重大历史外交活动的重要场所。",
                    "phong-khanh-tiet-zh.mp3", 115);
        }
        if (phongDaiYen != null && narrationRepository.findByLocationIdAndLanguageCode(phongDaiYen.getId(), "zh").isEmpty()) {
            createNarration(phongDaiYen, zh,
                    "大宴厅位于独立宫二楼。这里曾是举办容纳百余位贵宾的国家级盛大晚宴的场所，其建筑风格巧妙融合了东方传统韵味与西方现代美学。",
                    "phong-dai-yen-zh.mp3", 95);
        }

        // 3. Tiếng Nhật (日本語) cho các điểm (sử dụng các file audio 1-ja.mp3 đến 6-ja.mp3 đã tích hợp)
        if (tramKhanhHoi != null && narrationRepository.findByLocationIdAndLanguageCode(tramKhanhHoi.getId(), "ja").isEmpty()) {
            createNarration(tramKhanhHoi, ja,
                    "カインホイ（Khanh Hoi）バス停へようこそ。かつてのカインホイ地域は、ベンゲー運河沿いの肥沃な中州であり、サイゴンの活気ある商業港や開拓時代からの南部住民の豊かな文化交流と深く結びついていました。",
                    "1-ja.mp3", 85);
        }
        if (tramVinhHoi != null && narrationRepository.findByLocationIdAndLanguageCode(tramVinhHoi.getId(), "ja").isEmpty()) {
            createNarration(tramVinhHoi, ja,
                    "ヴィンホイ（Vinh Hoi）バス停は、第4区の活気ある交易空間へと皆様をご案内します。ここはかつて水上貨物輸送の賑やかな拠点であり、現在は特色豊かな屋台グルメが楽しめる活気あふれるエリアとなっています。",
                    "2-ja.mp3", 85);
        }
        if (tramXomChieu != null && narrationRepository.findByLocationIdAndLanguageCode(tramXomChieu.getId(), "ja").isEmpty()) {
            createNarration(tramXomChieu, ja,
                    "ソムチエウ（Xom Chieu）バス停は、前世紀から続く伝統工芸村の文化を今に伝えています。かつては南部で名高いイグサのゴザ織りの中心地であり、現在はホーチミン市で最も賑やかなストリートフードの天国となっています。",
                    "3-ja.mp3", 90);
        }
        if (dinhDocLap != null && narrationRepository.findByLocationIdAndLanguageCode(dinhDocLap.getId(), "ja").isEmpty()) {
            createNarration(dinhDocLap, ja,
                    "統一会堂（旧独立宮殿）は、ホーチミン市第1区ナムキーコイギア通り135番地に位置しています。才能ある建築家ゴー・ヴィエット・トゥ（Ngo Viet Thu）の設計により、1962年に旧ノロドム宮殿の跡地に建設が開始されました。ここは1975年4月30日正午の平和と国家統一の瞬間を刻んだ歴史の証人です。",
                    "4-ja.mp3", 145);
        }
        if (phongKhanhTiet != null && narrationRepository.findByLocationIdAndLanguageCode(phongKhanhTiet.getId(), "ja").isEmpty()) {
            createNarration(phongKhanhTiet, ja,
                    "統一会堂の慶節の間（カンティエット・ルーム）へようこそ。宮殿内で最も広い面積を誇るこの部屋は、500人以上を収容できる格式高い造りとなっています。1975年4月30日以前は、重要な閣僚会議や国家元首の歓迎式典、歴史的な外交行事の場として使われていました。",
                    "5-ja.mp3", 120);
        }
        if (phongDaiYen != null && narrationRepository.findByLocationIdAndLanguageCode(phongDaiYen.getId(), "ja").isEmpty()) {
            createNarration(phongDaiYen, ja,
                    "大宴会場は統一会堂の2階に位置しています。ここは100名以上の賓客をもてなす国家的な晩餐会が開催された場所で、東洋の伝統と西洋の現代性が調和した素晴らしい建築美が特徴です。",
                    "6-ja.mp3", 100);
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
