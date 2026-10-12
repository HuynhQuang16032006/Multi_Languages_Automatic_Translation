package vn.edu.sgu.audiotour.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.PlaybackLog;

import java.util.List;

@Repository
public interface PlaybackLogRepository extends JpaRepository<PlaybackLog, Long> {

    @Query("SELECT AVG(p.listenDurationSeconds) FROM PlaybackLog p WHERE p.listenDurationSeconds IS NOT NULL")
    Double getAverageListenDuration();

    @Query("SELECT p.location.id, p.location.name, COUNT(p.id) FROM PlaybackLog p GROUP BY p.location.id, p.location.name ORDER BY COUNT(p.id) DESC")
    List<Object[]> getTopListenedLocations(Pageable pageable);

    @Query("SELECT p.triggerType, COUNT(p.id) FROM PlaybackLog p GROUP BY p.triggerType")
    List<Object[]> countListensByTriggerType();

    @Query("SELECT p.latitude, p.longitude, COUNT(p.id) FROM PlaybackLog p WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL GROUP BY p.latitude, p.longitude")
    List<Object[]> getHeatmapPoints();
}
