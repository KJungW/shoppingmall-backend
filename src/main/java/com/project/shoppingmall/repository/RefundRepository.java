package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Refund;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRepository extends JpaRepository<Refund, Long> {

  @Query(
      "select r from Refund r " + "left join fetch r.purchaseItem i " + "where r.id = :refundId ")
  Optional<Refund> findByIdWithPurchaseItemProduct(@Param("refundId") long refundId);
}
