package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FrontendRoutingTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void touristRoutesForwardToReactWithoutAuthentication() throws Exception {
        for (String path : new String[]{"/next/", "/next/explore", "/next/places/1", "/next/player/1", "/next/map", "/next/qr", "/next/about"}) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/next/index.html"));
        }
    }

    @Test
    void frontendAccessDoesNotExposeAdminApi() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRoutesForwardToReactWithoutExposingApis() throws Exception {
        for (String path : new String[]{"/next/admin", "/next/admin/places", "/next/admin/audio", "/next/admin/qr"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/next/index.html"));
        }
    }
}
