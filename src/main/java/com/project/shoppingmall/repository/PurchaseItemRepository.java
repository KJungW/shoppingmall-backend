package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.PurchaseItem;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {

  @EntityGraph(attributePaths = {"purchase", "refunds"})
  @Query("select p from PurchaseItem p where p.id=:purchaseId")
  Optional<PurchaseItem> findByIdWithPurchaseAndRefund(@Param("purchaseId") Long purchaseId);

  @Query(
      "select p from PurchaseItem p " + "left join fetch p.review r " + "where r.id = :reviewId ")
  Optional<PurchaseItem> findByReviewId(@Param("reviewId") long reviewId);

  @Query("select pi from PurchaseItem pi where pi.productId = :productId")
  Slice<PurchaseItem> findLatestByProduct(@Param("productId") long productId, Pageable pageable);

  @Query("select pi from PurchaseItem pi where pi.sellerId = :sellerId")
  Slice<PurchaseItem> findLatestBySeller(@Param("sellerId") long sellerId, Pageable pageable);

  @Query(
      "select sum(pi.finalPrice) "
          + "from PurchaseItem pi "
          + "left join pi.purchase pu "
          + "where pi.sellerId = :sellerId "
          + "and pu.state = 'COMPLETE' "
          + "and pu.createDate between :startDate and :endDate")
  Long findSalesRevenuePriceInPeriodBySeller(
      @Param("sellerId") long sellerId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
