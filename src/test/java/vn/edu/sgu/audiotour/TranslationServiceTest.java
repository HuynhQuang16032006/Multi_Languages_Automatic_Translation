package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import vn.edu.sgu.audiotour.config.MtProperties;
import vn.edu.sgu.audiotour.exception.ServiceUnavailableException;
import vn.edu.sgu.audiotour.service.TranslationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

class TranslationServiceTest {

    private static final String BASE_URL = "https://api-free.deepl.com";

    private MockRestServiceServer server;
    private TranslationService translationService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        translationService = new TranslationService(new MtProperties("test-key", BASE_URL), builder);
    }

    @Test
    void testTranslate_SendsSourceAndTargetAndReturnsText() {
        server.expect(requestTo(BASE_URL + "/v2/translate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "DeepL-Auth-Key test-key"))
                .andExpect(content().string(containsString("\"source_lang\":\"VI\"")))
                .andExpect(content().string(containsString("\"target_lang\":\"JA\"")))
                .andExpect(content().string(containsString("\"Xin chào\"")))
                .andRespond(withSuccess(
                        "{\"translations\":[{\"detected_source_language\":\"VI\",\"text\":\"こんにちは\"}]}",
                        MediaType.APPLICATION_JSON));

        String result = translationService.translate("Xin chào", "ja");

        server.verify();
        assertEquals("こんにちは", result);
    }

    @Test
    void testTranslate_ChineseUsesSimplifiedVariant() {
        server.expect(requestTo(BASE_URL + "/v2/translate"))
                .andExpect(content().string(containsString("\"target_lang\":\"ZH-HANS\"")))
                .andRespond(withSuccess(
                        "{\"translations\":[{\"detected_source_language\":\"VI\",\"text\":\"你好\"}]}",
                        MediaType.APPLICATION_JSON));

        assertEquals("你好", translationService.translate("Xin chào", "zh"));
    }

    @Test
    void testTranslate_MissingApiKeyThrowsServiceUnavailable() {
        TranslationService withoutKey = new TranslationService(
                new MtProperties("", BASE_URL), RestClient.builder());

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> withoutKey.translate("Xin chào", "ja"));

        assertTrue(exception.getMessage().contains("DEEPL_API_KEY"));
    }

    @Test
    void testTranslate_UpstreamErrorIsWrapped() {
        server.expect(requestTo(BASE_URL + "/v2/translate"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Wrong endpoint. Use api-free.deepl.com\"}"));

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> translationService.translate("Xin chào", "ja"));

        assertTrue(exception.getMessage().contains("403"));
    }

    @Test
    void testTranslate_EmptyTranslationsThrowsServiceUnavailable() {
        server.expect(requestTo(BASE_URL + "/v2/translate"))
                .andRespond(withSuccess("{\"translations\":[]}", MediaType.APPLICATION_JSON));

        assertThrows(ServiceUnavailableException.class, () -> translationService.translate("Xin chào", "ja"));
    }
}
