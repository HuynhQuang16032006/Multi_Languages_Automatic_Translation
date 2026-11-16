package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Trang chủ React và trang HTML cũ phải mở được không cần token. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GuestHomePageTest {

    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private int port;

    @Test
    void testRootPathRedirectsToReact() throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/")).build(),
                HttpResponse.BodyHandlers.ofString());

        assertEquals(302, response.statusCode());
        URI location = URI.create(response.headers().firstValue("location").orElseThrow());
        assertEquals("http", location.getScheme());
        assertEquals("localhost", location.getHost());
        assertEquals(port, location.getPort());
        assertEquals("/next/explore", location.getPath());
    }

    @Test
    void testIndexHtmlIsPublic() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void testIndexHtmlContainsPoiListAndLeafletMap() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("poiList")))
                .andExpect(content().string(containsString("leaflet")));
    }

    // QR theo toà: trang chủ phải đọc ?venue= và có banner "Bạn đang ở"
    @Test
    void testIndexHtmlHandlesVenueParam() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"venueBanner\"")))
                .andExpect(content().string(containsString("get(\"venue\")")));
    }

    // Không có locationId lẫn qr: player phải quay về trang chọn, không phát nhầm POI mặc định
    @Test
    void testPlayerSendsGuestsBackWithoutLocation() throws Exception {
        mockMvc.perform(get("/player.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("window.location.replace(\"index.html\")")));
    }

    // ADR 0006: Trang chủ có bộ chọn ngôn ngữ toàn cục trên header và lưu trữ Preferred Language
    @Test
    void testIndexHtmlSupportsPreferredLanguage() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("headerLangSelect")))
                .andExpect(content().string(containsString("audiotour_preferred_lang")));
    }

    // ADR 0006: Player hỗ trợ ghi nhớ Preferred Language và đồng bộ đa tab
    @Test
    void testPlayerHtmlSupportsPreferredLanguage() throws Exception {
        mockMvc.perform(get("/player.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("audiotour_preferred_lang")))
                .andExpect(content().string(containsString("addEventListener(\"storage\"")));
    }
}
