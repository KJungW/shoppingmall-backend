package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.report.ProductReport;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductReportRetrieveRepository extends JpaRepository<ProductReport, Long> {
  @Query(
      "select pr from ProductReport pr "
          + "left join fetch pr.reporter r "
          + "left join fetch pr.product p "
          + "left join fetch p.seller s "
          + "left join fetch p.productType pt "
          + "where pt.id = :productTypeId "
          + "and pr.isProcessedComplete = false")
  Slice<ProductReport> findUnprocessedProductReport(
      @Param("productTypeId") long productTypeId, Pageable pageable);

  @Query(
      "select pr from ProductReport pr "
          + "left join fetch pr.reporter r "
          + "left join fetch pr.product p "
          + "left join fetch p.seller s "
          + "left join fetch p.productType pt "
          + "where s.id = :productSellerId ")
  Slice<ProductReport> findProductReportsByProductSeller(
      @Param("productSellerId") long productSellerId, PageRequest pageRequest);
}
