package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.service.NarrationService;

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
class NarrationServiceTest {

    @Mock
    private NarrationRepository narrationRepository;

    @InjectMocks
    private NarrationService narrationService;

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
    void testGetNarration_Found() {
        Location location = new Location();
        location.setId(5L);
        location.setName("Dinh Độc Lập");

        Language language = new Language();
        language.setId(1L);
        language.setCode("vi");
        language.setName("Tiếng Việt");

        Narration narration = new Narration();
        narration.setId(10L);
        narration.setLocation(location);
        narration.setLanguage(language);
        narration.setTextContent("Dinh Độc Lập được xây dựng năm 1887, là biểu tượng của tinh thần dân chủ và độc lập.");
        narration.setAudioUrl("dinh-doc-lap-vi.mp3");
        narration.setDurationInSeconds(95);

        when(narrationRepository.findByLocationIdAndLanguageCode(5L, "vi"))
                .thenReturn(Optional.of(narration));

        NarrationResponse result = narrationService.getNarration(5L, "vi");

        assertNotNull(result);
        assertEquals(5L, result.getLocationId());
        assertEquals("Dinh Độc Lập", result.getLocationName());
        assertEquals("vi", result.getLanguageCode());
        assertEquals("Dinh Độc Lập được xây dựng năm 1887, là biểu tượng của tinh thần dân chủ và độc lập.", result.getTextContent());
        assertEquals("dinh-doc-lap-vi.mp3", result.getAudioUrl());
        assertEquals(95, result.getDurationInSeconds());
    }

    @Test
    void testGetNarration_NotFound() {
        when(narrationRepository.findByLocationIdAndLanguageCode(999L, "xx"))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                narrationService.getNarration(999L, "xx")
        );

        assertTrue(exception.getMessage().contains("999"));
        assertTrue(exception.getMessage().contains("xx"));
    }

    @Test
    void testGetAudioResource_Success() {
        Narration narration = new Narration();
        narration.setAudioUrl(TEST_AUDIO_FILENAME);

        when(narrationRepository.findByLocationIdAndLanguageCode(1L, "vi"))
                .thenReturn(Optional.of(narration));

        Resource resource = narrationService.getAudioResource(1L, "vi");

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals(TEST_AUDIO_FILENAME, resource.getFilename());
    }

    @Test
    void testGetAudioResource_NarrationNotFound() {
        when(narrationRepository.findByLocationIdAndLanguageCode(999L, "vi"))
                .thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                narrationService.getAudioResource(999L, "vi")
        );

        assertTrue(exception.getMessage().contains("Không tìm thấy"));
    }

    @Test
    void testCreateOfflinePackage_Success() throws IOException {
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

        byte[] zipBytes = narrationService.createOfflinePackage(locationId, langCode);

        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);

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
                narrationService.createOfflinePackage(999L, "vi")
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
                narrationService.createOfflinePackage(1L, "vi")
        );

        assertTrue(exception.getMessage().contains("File âm thanh không tồn tại"));
    }
}
