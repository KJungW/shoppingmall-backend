package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Purchase;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRetrieveRepository extends JpaRepository<Purchase, Long> {
  @Query("select p from Purchase p " + "where p.buyerId = :buyerId " + "and p.state != 'FAIL'")
  Slice<Purchase> findAllByBuyer(@Param("buyerId") Long buyerId, Pageable pageable);
}
