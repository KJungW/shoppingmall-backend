package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Refund;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {

  @Query(
      "select r from Refund r " + "left join fetch r.purchaseItem i " + "where r.id = :refundId ")
  Optional<Refund> findByIdWithPurchaseItemProduct(@Param("refundId") long refundId);

  @Query(
      "select r from Refund r "
          + "left join fetch r.purchaseItem pi "
          + "where pi.sellerId = :sellerId "
          + "and r.state != 'COMPLETE' "
          + "and r.state != 'REJECTED' ")
  List<Refund> findAllProcessingStateRefundBySeller(@Param("sellerId") long sellerId);

  @Query(
      "select sum(r.refundPrice) "
          + "from Refund r "
          + "left join r.purchaseItem pi "
          + "left join pi.purchase pu "
          + "where pi.sellerId = :sellerId "
          + "and pu.state = 'COMPLETE' "
          + "and pu.createDate between :startDate and :endDate "
          + "and r.state = 'COMPLETE'")
  Long findRefundPriceInPeriodBySeller(
      @Param("sellerId") long sellerId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
