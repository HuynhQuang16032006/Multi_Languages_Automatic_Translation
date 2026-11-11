package vn.edu.sgu.audiotour.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.Mac;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        // Fail-fast lúc khởi động: secret rỗng/quá ngắn sẽ bị WeakKeyException khó hiểu nếu không chặn trước
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Thiếu biến môi trường JWT_SECRET: jwt.secret không được để trống");
        }
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "Biến môi trường JWT_SECRET phải dài tối thiểu 32 byte (HS256), hiện tại chỉ "
                            + secretBytes.length + " byte");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
    }

    // Tạo token HS256 với subject là username, thời hạn lấy từ cấu hình
    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(String username, String passwordHash) {
        Date now = new Date();
        return Jwts.builder().subject(username).issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .claim("credential", credentialFingerprint(passwordHash))
                .signWith(key, Jwts.SIG.HS256).compact();
    }

    public boolean isTokenValid(String token, String username, String passwordHash) {
        try {
            Claims claims = parse(token).getPayload();
            String credential = claims.get("credential", String.class);
            return username.equals(claims.getSubject()) && credential != null
                    && MessageDigest.isEqual(credential.getBytes(StandardCharsets.UTF_8),
                    credentialFingerprint(passwordHash).getBytes(StandardCharsets.UTF_8));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String credentialFingerprint(String passwordHash) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(passwordHash.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot compute credential fingerprint", e);
        }
    }

    // Lấy username (subject) từ token
    public String extractUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    // Token hợp lệ khi chữ ký đúng và chưa hết hạn
    public boolean isTokenValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    }
}
