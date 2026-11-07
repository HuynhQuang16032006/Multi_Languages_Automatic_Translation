package vn.edu.sgu.audiotour.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.sgu.audiotour.entity.PackageDownload;

public interface PackageDownloadRepository extends JpaRepository<PackageDownload, Long> {
}
