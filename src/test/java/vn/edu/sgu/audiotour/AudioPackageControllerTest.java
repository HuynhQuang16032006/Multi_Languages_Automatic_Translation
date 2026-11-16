package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.sgu.audiotour.controller.AudioPackageController;
import vn.edu.sgu.audiotour.exception.GlobalExceptionHandler;
import vn.edu.sgu.audiotour.exception.NotFoundException;
import vn.edu.sgu.audiotour.service.LocationService;
import vn.edu.sgu.audiotour.service.NarrationService;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AudioPackageControllerTest {

    private MockMvc mockMvc;

    @TempDir
    Path tempDir;

    @Mock
    private NarrationService narrationService;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private AudioPackageController audioPackageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(audioPackageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testDownloadOfflinePackage_Success() throws Exception {
        byte[] dummyZip = new byte[]{0x50, 0x4B, 0x03, 0x04}; // ZIP magic bytes header

        when(narrationService.createOfflinePackage(1L, "vi", null)).thenReturn(dummyZip);

        mockMvc.perform(get("/api/packages/offline")
                        .param("locationId", "1")
                        .param("languageCode", "vi"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tour-offline.zip\""))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(dummyZip));
    }

    @Test
    void testStreamAudio_Success() throws Exception {
        byte[] audio = "TEST_AUDIO_DATA".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Path audioFile = Files.write(tempDir.resolve("sample.mp3"), audio);
        when(narrationService.getAudioResource(1L, "vi"))
                .thenReturn(new FileSystemResource(audioFile));

        mockMvc.perform(get("/api/packages/stream")
                        .param("locationId", "1")
                        .param("languageCode", "vi")
                        .accept("audio/mpeg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"sample.mp3\""))
                .andExpect(content().bytes(audio));
    }

    @Test
    void testStreamAudio_Range() throws Exception {
        Path audioFile = Files.write(tempDir.resolve("sample.mp3"), new byte[]{0, 1, 2, 3, 4, 5});
        when(narrationService.getAudioResource(1L, "vi"))
                .thenReturn(new FileSystemResource(audioFile));

        mockMvc.perform(get("/api/packages/stream")
                        .param("locationId", "1")
                        .param("languageCode", "vi")
                        .accept("audio/mpeg")
                        .header(HttpHeaders.RANGE, "bytes=1-3"))
                .andExpect(status().isPartialContent())
                .andExpect(content().contentType("audio/mpeg"))
                .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 1-3/6"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void testStreamAudio_IncompatibleAccept() throws Exception {
        mockMvc.perform(get("/api/packages/stream")
                        .param("locationId", "1")
                        .param("languageCode", "vi")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void testStreamAudio_MissingFileWithAudioAccept() throws Exception {
        when(narrationService.getAudioResource(1L, "vi"))
                .thenThrow(new RuntimeException("File âm thanh không tồn tại"));

        mockMvc.perform(get("/api/packages/stream")
                        .param("locationId", "1")
                        .param("languageCode", "vi")
                        .accept("audio/mpeg"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("File âm thanh không tồn tại"));
    }

    @Test
    void testDownloadOfflinePackage_MissingFile() throws Exception {
        when(narrationService.createOfflinePackage(1L, "vi", null))
                .thenThrow(new RuntimeException("File âm thanh không tồn tại"));

        mockMvc.perform(get("/api/packages/offline")
                        .param("locationId", "1")
                        .param("languageCode", "vi")
                        .accept("application/zip"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("File âm thanh không tồn tại"));
    }

    @Test
    void testStreamAudio_MissingNarrationWithAudioAccept() throws Exception {
        when(narrationService.getAudioResource(1L, "vi"))
                .thenThrow(new NotFoundException("Không tìm thấy bài thuyết minh"));

        mockMvc.perform(get("/api/packages/stream")
                        .param("locationId", "1")
                        .param("languageCode", "vi")
                        .accept("audio/mpeg"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }
}
