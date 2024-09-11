package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.report.ProductReport;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductReportRepository extends JpaRepository<ProductReport, Long> {
  @Query(
      "select r from ProductReport r "
          + "where r.reporter.id = :reporterId "
          + "and r.product.id = :productId ")
  Slice<ProductReport> findLatestReports(
      @Param("reporterId") long reporterId, @Param("productId") long productId, Pageable pageable);

  @Query("select pr from ProductReport pr where pr.product.id = :productId")
  List<ProductReport> findAllByProduct(@Param("productId") long productId);

  @Query("select pr from ProductReport pr where pr.reporter.id = :reporterId")
  List<ProductReport> findAllByReporter(@Param("reporterId") long reporterId);
}
