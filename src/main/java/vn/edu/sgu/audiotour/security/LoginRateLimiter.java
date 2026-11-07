package vn.edu.sgu.audiotour.security;

import org.springframework.stereotype.Component;
import vn.edu.sgu.audiotour.exception.TooManyRequestsException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    public static final int MAX_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public void checkLimit(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return;
        }
        AttemptInfo info = attempts.get(clientIp);
        if (info == null) {
            return;
        }

        Instant now = Instant.now();
        if (info.lockedUntil != null) {
            if (now.isBefore(info.lockedUntil)) {
                long remainingMinutes = Math.max(1, Duration.between(now, info.lockedUntil).toMinutes() + 1);
                throw new TooManyRequestsException(
                        "Đăng nhập sai quá " + MAX_ATTEMPTS + " lần. Vui lòng thử lại sau " + remainingMinutes + " phút.");
            } else {
                // Hết hạn khoá, xoá để tính lại từ đầu
                attempts.remove(clientIp);
            }
        }
    }

    public void recordFailure(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return;
        }
        Instant now = Instant.now();
        attempts.compute(clientIp, (ip, current) -> {
            if (current == null || (current.lockedUntil != null && now.isAfter(current.lockedUntil))) {
                return new AttemptInfo(1, null);
            }
            int newCount = current.failedCount + 1;
            Instant lockedUntil = (newCount >= MAX_ATTEMPTS) ? now.plus(LOCK_DURATION) : null;
            return new AttemptInfo(newCount, lockedUntil);
        });

        if (attempts.size() > 1000) {
            cleanupExpired(now);
        }
    }

    public void recordSuccess(String clientIp) {
        if (clientIp != null) {
            attempts.remove(clientIp);
        }
    }

    public void reset() {
        attempts.clear();
    }

    private void cleanupExpired(Instant now) {
        attempts.entrySet().removeIf(entry -> {
            AttemptInfo info = entry.getValue();
            return info.lockedUntil != null && now.isAfter(info.lockedUntil);
        });
    }

    private static class AttemptInfo {
        final int failedCount;
        final Instant lockedUntil;

        AttemptInfo(int failedCount, Instant lockedUntil) {
            this.failedCount = failedCount;
            this.lockedUntil = lockedUntil;
        }
    }
}
