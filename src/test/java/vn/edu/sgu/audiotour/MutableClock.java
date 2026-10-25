package vn.edu.sgu.audiotour;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Clock chỉnh tay: thay cho sleep để test mốc hết hạn và thời điểm ghi log. */
final class MutableClock extends Clock {

    private Instant instant;

    MutableClock(Instant start) {
        this.instant = start;
    }

    void advanceMillis(long millis) {
        instant = instant.plusMillis(millis);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
