package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.report.ProductReport;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductReportRepository extends JpaRepository<ProductReport, Long> {
  @Query(
      "select r from ProductReport r "
          + "where r.reporter.id = :reporterId "
          + "and r.product.id = :productId ")
  public List<ProductReport> findLatestReport(
      @Param("reporterId") Long reporterId, @Param("productId") Long productId, Pageable pageable);
}
