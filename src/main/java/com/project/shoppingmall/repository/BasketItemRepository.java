package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.BasketItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BasketItemRepository extends JpaRepository<BasketItem, Long> {
  @Query("select bi from BasketItem bi where bi.product.id = :productId")
  List<BasketItem> findAllByProduct(@Param("productId") long productId);

  @Query("select bi from BasketItem bi where bi.member.id = :memberId")
  List<BasketItem> findAllByMember(@Param("memberId") long memberId);
}
