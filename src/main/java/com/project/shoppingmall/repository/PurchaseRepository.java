package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Purchase;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
  Optional<Purchase> findByPurchaseUid(String purchaseUid);

  @Query("select p from Purchase p where p.buyerId = :buyerId")
  List<Purchase> findAllByBuyer(@Param("buyerId") long buyerId);
}
