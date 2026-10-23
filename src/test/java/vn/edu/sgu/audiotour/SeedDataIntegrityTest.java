package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.repository.LanguageRepository;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.repository.NarrationRepository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm tra toàn vẹn dữ liệu seed sau khi application context khởi động.
 * Idempotent: chạy được cả trên DB đã có dữ liệu và DB trống (DataInitializer tự seed).
 */
@SpringBootTest
class SeedDataIntegrityTest {

    private static final List<String> EXPECTED_QR_CODES = List.of(
            "QR-KHANH-HOI-01", "QR-VINH-HOI-01", "QR-XOM-CHIEU-01",
            "QR-DINH-DOC-LAP-01", "QR-PHONG-KHANH-TIET", "QR-PHONG-DAI-YEN");

    // Chỉ các file MP3 này thực sự tồn tại trong uploads/audio và được DataInitializer seed
    private static final List<String> EXPECTED_AUDIO_FILES = List.of(
            "dinh-doc-lap-vi.mp3", "dinh-doc-lap-zh.mp3", "phong-khanh-tiet-vi.mp3", "phong-khanh-tiet-en.mp3",
            "phong-dai-yen-vi.mp3", "tram-khanh-hoi-vi.mp3", "tram-vinh-hoi-vi.mp3",
            "tram-xom-chieu-vi.mp3");

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private NarrationRepository narrationRepository;

    @Test
    void testSeedLanguagesExist() {
        assertTrue(languageRepository.findByCode("vi").isPresent(), "Thiếu ngôn ngữ vi");
        assertTrue(languageRepository.findByCode("en").isPresent(), "Thiếu ngôn ngữ en");
    }

    @Test
    void testSeedPoisExist() {
        for (String qrCode : EXPECTED_QR_CODES) {
            assertTrue(locationRepository.findByQrCode(qrCode).isPresent(),
                    "Thiếu POI với mã QR: " + qrCode);
        }
    }

    @Test
    void testDinhDocLapHasTwoSubLocations() {
        Location dinhDocLap = locationRepository.findByQrCode("QR-DINH-DOC-LAP-01").orElseThrow();
        List<String> subLocationNames = locationRepository.findByParentLocationId(dinhDocLap.getId())
                .stream().map(Location::getName).toList();

        assertEquals(2, subLocationNames.size(), "Dinh Độc Lập phải có đúng 2 khu vực con");
        assertTrue(subLocationNames.contains("Phòng Khánh Tiết"));
        assertTrue(subLocationNames.contains("Phòng Đại Yến"));
    }

    @Test
    void testNarrationsExistForEverySeededAudioFile() {
        List<String> seededAudioUrls = narrationRepository.findAll().stream()
                .map(Narration::getAudioUrl)
                .toList();

        for (String audioFile : EXPECTED_AUDIO_FILES) {
            assertTrue(Files.exists(Paths.get("uploads", "audio", audioFile)),
                    "File MP3 không tồn tại trong uploads/audio: " + audioFile);
            assertTrue(seededAudioUrls.contains(audioFile),
                    "Thiếu bản thuyết minh cho file âm thanh: " + audioFile);
        }
    }
}
