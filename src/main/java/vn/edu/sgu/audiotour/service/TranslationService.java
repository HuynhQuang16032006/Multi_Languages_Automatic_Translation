package vn.edu.sgu.audiotour.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import vn.edu.sgu.audiotour.config.MtProperties;
import vn.edu.sgu.audiotour.exception.ServiceUnavailableException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dịch văn bản sang ngôn ngữ đích bằng DeepL. Dùng cho bước thêm ngôn ngữ mới (ADR 0005).
 */
@Slf4j
@Service
public class TranslationService {

    private static final String SOURCE_LANGUAGE = "VI";

    private final MtProperties properties;
    private final RestClient restClient;

    public TranslationService(MtProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    }

    // Lỗi mạng hoặc upstream đều thành 503: không phải lỗi dữ liệu của client
    public String translate(String text, String targetCode) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ServiceUnavailableException(
                    "Chưa cấu hình DEEPL_API_KEY, không dịch được sang '" + targetCode + "'");
        }
        try {
            DeepLResponse response = restClient.post()
                    .uri("/v2/translate")
                    .header("Authorization", "DeepL-Auth-Key " + properties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "text", List.of(text),
                            "source_lang", SOURCE_LANGUAGE,
                            "target_lang", toDeepLCode(targetCode)))
                    .retrieve()
                    .body(DeepLResponse.class);

            if (response == null || response.translations() == null || response.translations().isEmpty()) {
                throw new ServiceUnavailableException("DeepL trả về bản dịch rỗng cho '" + targetCode + "'");
            }
            return response.translations().get(0).text();
        } catch (RestClientResponseException ex) {
            throw new ServiceUnavailableException("DeepL trả lỗi " + ex.getStatusCode().value() + ": "
                    + abbreviate(ex.getResponseBodyAsString()));
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Không gọi được DeepL: " + ex.getMessage());
        }
    }

    // DeepL nhận mã chữ hoa; tiếng Trung phải chỉ rõ biến thể giản thể
    static String toDeepLCode(String code) {
        return "zh".equalsIgnoreCase(code) ? "ZH-HANS" : code.toUpperCase(Locale.ROOT);
    }

    public record DeepLResponse(List<Item> translations) {
    }

    public record Item(String detected_source_language, String text) {
    }

    private String abbreviate(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 200 ? body : body.substring(0, 200) + "...";
    }
}
