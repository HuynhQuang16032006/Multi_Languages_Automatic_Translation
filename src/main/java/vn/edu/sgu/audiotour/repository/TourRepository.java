package vn.edu.sgu.audiotour.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.Tour;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {

    @Override
    @EntityGraph(attributePaths = {"locations"})
    List<Tour> findAll();

    @Override
    @EntityGraph(attributePaths = {"locations"})
    Optional<Tour> findById(Long id);
}
