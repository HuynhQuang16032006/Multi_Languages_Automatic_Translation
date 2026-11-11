package vn.edu.sgu.audiotour;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.edu.sgu.audiotour.dto.LocationRequest;
import vn.edu.sgu.audiotour.entity.Location;
import vn.edu.sgu.audiotour.exception.ConflictException;
import vn.edu.sgu.audiotour.repository.LocationRepository;
import vn.edu.sgu.audiotour.service.LocationService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LocationHierarchyConcurrencyIntegrationTest {
    @Autowired LocationRepository locations;
    @Autowired LocationService service;

    @Test
    void concurrentReparentingCannotCreateCycle() throws Exception {
        Location a = new Location();
        a.setName("Concurrent hierarchy A");
        a = locations.saveAndFlush(a);
        Location b = new Location();
        b.setName("Concurrent hierarchy B");
        b = locations.saveAndFlush(b);
        long aId = a.getId(), bId = b.getId();
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> updateParent(start, aId, bId));
            var second = pool.submit(() -> updateParent(start, bId, aId));
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        } finally {
            LocationRequest clear = new LocationRequest();
            clear.setName("Concurrent hierarchy cleanup");
            service.updateLocation(aId, clear);
            service.updateLocation(bId, clear);
            locations.deleteById(aId);
            locations.deleteById(bId);
        }
    }

    @Test
    void concurrentQrUpdatesWithSameExpectedValueHaveOneWinner() throws Exception {
        Location location = new Location();
        location.setName("Concurrent QR");
        location.setQrCode("CONCURRENT-OLD");
        long id = locations.saveAndFlush(location).getId();
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> updateQr(start, id, "CONCURRENT-A"));
            var second = pool.submit(() -> updateQr(start, id, "CONCURRENT-B"));
            start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertTrue(java.util.Set.of("CONCURRENT-A", "CONCURRENT-B")
                    .contains(locations.findById(id).orElseThrow().getQrCode()));
        } finally {
            locations.deleteById(id);
        }
    }

    private boolean updateQr(CountDownLatch start, long id, String code) throws InterruptedException {
        start.await();
        try {
            service.updateQrCode(id, code, "CONCURRENT-OLD");
            return true;
        } catch (ConflictException expected) {
            return false;
        }
    }

    private boolean updateParent(CountDownLatch start, long id, long parentId) throws InterruptedException {
        start.await();
        LocationRequest request = new LocationRequest();
        request.setName("Concurrent hierarchy");
        request.setParentId(parentId);
        try {
            service.updateLocation(id, request);
            return true;
        } catch (ConflictException expected) {
            return false;
        }
    }
}
