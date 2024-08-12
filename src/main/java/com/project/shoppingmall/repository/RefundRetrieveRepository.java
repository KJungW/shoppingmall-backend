package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Refund;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRetrieveRepository extends JpaRepository<Refund, Long> {

  @Query("select r from Refund r where r.purchaseItem.id = :refundId")
  Slice<Refund> findByPurchaseItem(@Param("refundId") long refundId, Pageable pageable);
}
