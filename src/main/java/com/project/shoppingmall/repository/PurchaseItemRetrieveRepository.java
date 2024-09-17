package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.PurchaseItem;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseItemRetrieveRepository extends JpaRepository<PurchaseItem, Long> {

  @Query(
      "select pi from PurchaseItem  pi "
          + "left join fetch pi.purchase pu "
          + "where pi.productId = :productId "
          + "and pu.state = 'COMPLETE' ")
  Slice<PurchaseItem> findAllForSeller(@Param("productId") long productId, Pageable pageable);

  @Query(
      "SELECT pi FROM PurchaseItem pi "
          + "left join fetch pi.purchase pu "
          + "where pi.sellerId = :sellerId "
          + "and pu.state = 'COMPLETE' "
          + "and pu.createDate between :startDate and :endDate")
  Slice<PurchaseItem> findAllForSellerBetweenDate(
      @Param("sellerId") long sellerId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);

  @Query(
      "select distinct pi "
          + "from PurchaseItem pi "
          + "join pi.refunds r "
          + "left join fetch pi.purchase pu "
          + "where pu.buyerId = :buyerId "
          + "and pu.state = 'Complete'")
  Slice<PurchaseItem> findRefundedAllForBuyer(
      @Param("buyerId") Long buyerId, PageRequest pageRequest);

  @Query(
      "select distinct pi "
          + "from PurchaseItem pi "
          + "join pi.refunds r "
          + "left join fetch pi.purchase pu "
          + "where pi.sellerId = :sellerId "
          + "and pu.state = 'Complete'")
  Slice<PurchaseItem> findRefundedAllForSeller(
      @Param("sellerId") Long sellerId, PageRequest pageRequest);
}
