package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.BasketItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BasketItemRetrieveRepository extends JpaRepository<BasketItem, Long> {
  @Query(
      "select b from BasketItem b "
          + "left join fetch b.member "
          + "left join fetch b.product p "
          + "left join fetch p.seller "
          + "left join fetch p.productType "
          + "left join fetch p.productImages "
          + "where b.id = :basketItemId ")
  Optional<BasketItem> retrieveBasketItemDetail(@Param("basketItemId") Long basketId);

  @Query(
      "select b from BasketItem b "
          + "left join fetch b.member m "
          + "left join fetch b.product p "
          + "left join fetch p.seller "
          + "left join fetch p.productType "
          + "left join fetch p.productImages "
          + "where m.id = :memberId ")
  List<BasketItem> retrieveBasketByMemberId(@Param("memberId") Long memberId);
}
