package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import vn.edu.sgu.audiotour.exception.GlobalExceptionHandler;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testDataIntegrityViolationMapsTo409WithoutRawSql() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "Duplicate entry 'vi' for key 'uk_languages_code'");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/languages");

        ResponseEntity<Map<String, Object>> response =
                handler.handleDataIntegrityViolation(ex, new ServletWebRequest(request));

        assertEquals(409, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(Set.of("timestamp", "status", "error", "message", "path"), body.keySet());
        assertEquals(409, body.get("status"));
        assertEquals("/api/languages", body.get("path"));
        assertTrue(String.valueOf(body.get("message")).contains("ràng buộc"));
        assertFalse(String.valueOf(body).contains("Duplicate entry"));
        assertFalse(String.valueOf(body).contains("uk_languages_code"));
    }
}
