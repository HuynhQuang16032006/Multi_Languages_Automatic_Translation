package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import vn.edu.sgu.audiotour.config.TtsProperties;
import vn.edu.sgu.audiotour.dto.NarrationResponse;
import vn.edu.sgu.audiotour.entity.Language;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.entity.Narration;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.exception.ServiceUnavailableException;
import vn.edu.sgu.audiotour.repository.NarrationRepository;
import vn.edu.sgu.audiotour.service.TtsService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class TtsServiceTest {

    private static final Path AUDIO_DIR = Paths.get("uploads", "audio");
    private static final String TEXT = "Dinh Äá»™c Láº­p Ä‘Æ°á»£c xÃ¢y dá»±ng nÄƒm 1962 trÃªn ná»n Dinh Norodom cÅ©.";
    private static final byte[] MP3 = {1, 2, 3, 4};

    @Mock
    private NarrationRepository narrationRepository;

    private MockRestServiceServer server;
    private TtsService ttsService;
    private Narration narration;
    private String fileName;
    private String generatedFileName;

    @BeforeEach
    void setUp() throws IOException {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        TtsProperties properties = new TtsProperties("test-key", "https://api.elevenlabs.io",
                "eleven_flash_v2_5", "voice-test");
        ttsService = new TtsService(properties, narrationRepository, builder);

        fileName = "tts-test-" + UUID.randomUUID() + ".mp3";

        Language vi = new Language(1L, "vi", "Tiáº¿ng Viá»‡t");
        Location location = new Location();
        location.setId(10L);
        location.setName("Dinh Äá»™c Láº­p");

        narration = new Narration();
        narration.setId(1L);
        narration.setLocation(location);
        narration.setLanguage(vi);
        narration.setTextContent(TEXT);
        narration.setAudioUrl(fileName);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(AUDIO_DIR.resolve(fileName));
        if (generatedFileName != null) Files.deleteIfExists(AUDIO_DIR.resolve(generatedFileName));
    }

    @Test
    void testGenerateAudio_WritesFileAndSavesAudioUrl() throws IOException {
        when(narrationRepository.findById(1L)).thenReturn(Optional.of(narration));
        server.expect(requestTo("https://api.elevenlabs.io/v1/text-to-speech/voice-test?output_format=mp3_44100_128"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("xi-api-key", "test-key"))
                .andRespond(withSuccess(MP3, MediaType.parseMediaType("audio/mpeg")));

        NarrationResponse response = ttsService.generateAudio(1L, false);
        generatedFileName = response.getAudioUrl();

        server.verify();
        assertArrayEquals(MP3, Files.readAllBytes(AUDIO_DIR.resolve(generatedFileName)));
        assertNotEquals(fileName, generatedFileName);
        assertEquals("vi", response.getLanguageCode());
        assertEquals("Dinh Äá»™c Láº­p", response.getLocationName());
        assertEquals((int) Math.round(TEXT.length() / 15.0), response.getDurationInSeconds());
    }

    @Test
    void testGenerateAudio_ReusesExistingFileWithoutCallingApi() throws IOException {
        Files.createDirectories(AUDIO_DIR);
        Files.writeString(AUDIO_DIR.resolve(fileName), "FILE_CU", StandardCharsets.UTF_8);

        when(narrationRepository.findById(2L)).thenReturn(Optional.of(narration));

        NarrationResponse response = ttsService.generateAudio(2L, false);

        // KhÃ´ng cÃ³ expectation nÃ o trÃªn server -> verify() chá»©ng minh khÃ´ng gá»i API, khÃ´ng tá»‘n credit
        server.verify();
        assertEquals("FILE_CU", Files.readString(AUDIO_DIR.resolve(fileName), StandardCharsets.UTF_8));
        assertEquals(fileName, response.getAudioUrl());
    }

    @Test
    void forceGenerationPreservesExistingFile() throws IOException {
        Files.createDirectories(AUDIO_DIR);
        Files.writeString(AUDIO_DIR.resolve(fileName), "old audio", StandardCharsets.UTF_8);
        when(narrationRepository.findById(1L)).thenReturn(Optional.of(narration));
        server.expect(requestTo("https://api.elevenlabs.io/v1/text-to-speech/voice-test?output_format=mp3_44100_128"))
                .andRespond(withSuccess(MP3, MediaType.parseMediaType("audio/mpeg")));

        NarrationResponse response = ttsService.generateAudio(1L, true);
        generatedFileName = response.getAudioUrl();

        assertNotEquals(fileName, generatedFileName);
        assertEquals("old audio", Files.readString(AUDIO_DIR.resolve(fileName), StandardCharsets.UTF_8));
        assertArrayEquals(MP3, Files.readAllBytes(AUDIO_DIR.resolve(generatedFileName)));
    }

    @Test
    void failedRegenerationPreservesExistingFileAndReference() throws IOException {
        Files.createDirectories(AUDIO_DIR);
        Files.writeString(AUDIO_DIR.resolve(fileName), "old audio", StandardCharsets.UTF_8);
        when(narrationRepository.findById(1L)).thenReturn(Optional.of(narration));
        server.expect(requestTo("https://api.elevenlabs.io/v1/text-to-speech/voice-test?output_format=mp3_44100_128"))
                .andRespond(withServerError());

        assertThrows(ServiceUnavailableException.class, () -> ttsService.generateAudio(1L, true));
        assertEquals(fileName, narration.getAudioUrl());
        assertEquals("old audio", Files.readString(AUDIO_DIR.resolve(fileName), StandardCharsets.UTF_8));
    }

    @Test
    void testGenerateAudio_MissingApiKeyThrowsServiceUnavailable() {
        RestClient.Builder builder = RestClient.builder();
        TtsService serviceWithoutKey = new TtsService(
                new TtsProperties("", "https://api.elevenlabs.io", "eleven_flash_v2_5", "voice-test"),
                narrationRepository, builder);
        when(narrationRepository.findById(3L)).thenReturn(Optional.of(narration));

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> serviceWithoutKey.generateAudio(3L, false));

        assertTrue(exception.getMessage().contains("ELEVENLABS_API_KEY"));
    }

    @Test
    void testGenerateAudio_UpstreamErrorIsWrapped() {
        when(narrationRepository.findById(4L)).thenReturn(Optional.of(narration));
        server.expect(requestTo("https://api.elevenlabs.io/v1/text-to-speech/voice-test?output_format=mp3_44100_128"))
                .andRespond(withStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"detail\":\"quota exceeded\"}"));

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> ttsService.generateAudio(4L, false));

        assertTrue(exception.getMessage().contains("429"));
    }

    @Test
    void testGenerateAudio_NarrationNotFound() {
        when(narrationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ttsService.generateAudio(99L, false));
    }
}
