package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.report.ReviewReport;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
  @Query(
      "select r from ReviewReport r "
          + "where r.reporter.id = :reporterId "
          + "and r.review.id = :reviewId ")
  Slice<ReviewReport> findLatestReports(
      @Param("reporterId") long reporterId, @Param("reviewId") long reviewId, Pageable pageable);
}
