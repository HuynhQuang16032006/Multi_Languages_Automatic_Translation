package vn.edu.sgu.audiotour.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.sgu.audiotour.entity.AdminAuth;

@Repository
public interface AdminAuthRepository extends JpaRepository<AdminAuth, Integer> {
}
