package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FrontendRoutingTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void rootRedirectsToReactAndPreservesQrQuery() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/next/explore"));
        mockMvc.perform(get("/?venue=4&lang=en"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/next/places/4?source=qr&venue=4&lang=en"));
        for (String venue : new String[]{"0", "01", "-1", "9007199254740992", "999999999999999999999", "evil.example"}) {
            mockMvc.perform(get("/?venue=" + venue))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/next/explore?venue=" + venue));
        }
    }

    @Test
    void touristRoutesForwardToReactWithoutAuthentication() throws Exception {
        for (String path : new String[]{"/next", "/next/", "/next/explore", "/next/places/1", "/next/player/1", "/next/map", "/next/qr", "/next/about"}) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(forwardedUrl("/next/index.html"));
        }
    }

    @Test
    void frontendAccessDoesNotExposeAdminApi() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/locations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRoutesForwardToReactWithoutExposingApis() throws Exception {
        for (String path : new String[]{"/next/admin", "/next/admin/places", "/next/admin/audio", "/next/admin/qr"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk()).andExpect(forwardedUrl("/next/index.html"));
        }
    }

    @Test
    void legacyPagesRemainAccessible() throws Exception {
        for (String path : new String[]{"/index.html", "/player.html", "/dashboard.html", "/demo.html"}) {
            mockMvc.perform(get(path)).andExpect(status().isOk());
        }
    }
}
