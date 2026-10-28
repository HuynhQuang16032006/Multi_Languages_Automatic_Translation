package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import vn.edu.sgu.audiotour.controller.AudioPackageController;
import vn.edu.sgu.audiotour.service.NarrationService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AudioPackageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NarrationService narrationService;

    @InjectMocks
    private AudioPackageController audioPackageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(audioPackageController).build();
    }

    @Test
    void testDownloadOfflinePackage_Success() throws Exception {
        byte[] dummyZip = new byte[]{0x50, 0x4B, 0x03, 0x04}; // ZIP magic bytes header

        when(narrationService.createOfflinePackage(1L, "vi")).thenReturn(dummyZip);

        mockMvc.perform(get("/api/packages/offline")
                        .param("locationId", "1")
                        .param("languageCode", "vi"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tour-offline.zip\""))
                .andExpect(content().contentType("application/zip"))
                .andExpect(content().bytes(dummyZip));
    }
}
