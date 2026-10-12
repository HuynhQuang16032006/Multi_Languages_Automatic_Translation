package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.service.AudioPackageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioPackageServiceTest {

    @Mock
    private NarrationRepository narrationRepository;

    @InjectMocks
    private AudioPackageService audioPackageService;

    private static final String TEST_AUDIO_FILENAME = "test-sample.mp3";
    private static final Path AUDIO_DIR = Paths.get("uploads", "audio");
    private static final Path AUDIO_FILE = AUDIO_DIR.resolve(TEST_AUDIO_FILENAME);

    @BeforeEach
    void setUp() throws IOException {
        Files.createDirectories(AUDIO_DIR);
        if (!Files.exists(AUDIO_FILE)) {
            Files.writeString(AUDIO_FILE, "TEST_AUDIO_DATA_FOR_UNIT_TEST");
        }
    }

    @Test
    void testCreateOfflinePackage_Success() throws IOException {
        // Arrange
        Long locationId = 1L;
        String langCode = "vi";
        String sampleText = "Nội dung thuyết minh thử nghiệm.";

        Narration narration = new Narration();
        narration.setId(10L);
        narration.setTextContent(sampleText);
        narration.setAudioUrl(TEST_AUDIO_FILENAME);
        narration.setDurationInSeconds(60);

        when(narrationRepository.findByLocationIdAndLanguageCode(locationId, langCode))
                .thenReturn(Optional.of(narration));

        // Act
        byte[] zipBytes = audioPackageService.createOfflinePackage(locationId, langCode);

        // Assert
        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);

        // Verify ZIP contents
        Map<String, byte[]> unzippedEntries = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                unzippedEntries.put(entry.getName(), zis.readAllBytes());
                zis.closeEntry();
            }
        }

        assertTrue(unzippedEntries.containsKey("content.txt"));
        assertEquals(sampleText, new String(unzippedEntries.get("content.txt"), StandardCharsets.UTF_8));

        assertTrue(unzippedEntries.containsKey(TEST_AUDIO_FILENAME));
        assertEquals("TEST_AUDIO_DATA_FOR_UNIT_TEST", new String(unzippedEntries.get(TEST_AUDIO_FILENAME), StandardCharsets.UTF_8));
    }

    @Test
    void testCreateOfflinePackage_NarrationNotFound() {
        when(narrationRepository.findByLocationIdAndLanguageCode(999L, "vi"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                audioPackageService.createOfflinePackage(999L, "vi")
        );

        assertTrue(exception.getMessage().contains("Không tìm thấy"));
    }

    @Test
    void testCreateOfflinePackage_AudioFileNotFound() {
        Narration narration = new Narration();
        narration.setTextContent("Text");
        narration.setAudioUrl("non-existent-audio.mp3");

        when(narrationRepository.findByLocationIdAndLanguageCode(1L, "vi"))
                .thenReturn(Optional.of(narration));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                audioPackageService.createOfflinePackage(1L, "vi")
        );

        assertTrue(exception.getMessage().contains("File âm thanh không tồn tại"));
    }
}
