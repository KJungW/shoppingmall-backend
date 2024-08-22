package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.report.ReviewReport;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewReportRetrieveRepository extends JpaRepository<ReviewReport, Long> {
  @Query(
      "select rr from ReviewReport rr "
          + "left join fetch rr.reporter rp "
          + "left join fetch rr.review r "
          + "left join fetch r.writer w "
          + "left join r.product p "
          + "left join p.productType pt "
          + "where pt.id = :productTypeId "
          + "and rr.isProcessedComplete = false")
  Slice<ReviewReport> findUnprocessedReviewReportReport(
      @Param("productTypeId") long productTypeId, Pageable pageable);

  @Query(
      "select rr from ReviewReport rr "
          + "left join fetch rr.reporter rp "
          + "left join fetch rr.review r "
          + "left join fetch r.writer w "
          + "where w.id = :reviewWriterId ")
  Slice<ReviewReport> findReviewReportsByReviewWriter(
      @Param("reviewWriterId") long reviewWriterId, PageRequest pageRequest);
}
