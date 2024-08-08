package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.PurchaseItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {

  @EntityGraph(attributePaths = {"purchase", "refunds"})
  @Query("select p from PurchaseItem p where p.id=:purchaseId")
  Optional<PurchaseItem> findByIdWithPurchaseAndRefund(@Param("purchaseId") Long purchaseId);
}
