package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.PurchaseItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseItemRetrieveRepository extends JpaRepository<PurchaseItem, Long> {

  @Query(
      "select pi from PurchaseItem  pi "
          + "left join fetch pi.purchase pu "
          + "left join fetch pi.product p "
          + "left join fetch pu.buyer "
          + "where p.id = :productId "
          + "and pu.state = 'COMPLETE' ")
  Slice<PurchaseItem> findAllForSeller(@Param("productId") long productId, Pageable pageable);
}
