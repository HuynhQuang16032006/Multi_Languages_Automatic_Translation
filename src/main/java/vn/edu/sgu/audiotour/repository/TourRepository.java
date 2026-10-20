package vn.edu.sgu.audiotour.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    // Lấy tour có phân trang kèm địa điểm.
    // shortcut: fetch join + phân trang nên Hibernate phân trang trong bộ nhớ và tải hết dữ liệu (HHH90003004); nâng cấp bằng cách phân trang theo id rồi fetch theo id.
    @Query(value = "select distinct t from Tour t",
            countQuery = "select count(t) from Tour t")
    @EntityGraph(attributePaths = {"locations"})
    Page<Tour> findAllPaged(Pageable pageable);
}
