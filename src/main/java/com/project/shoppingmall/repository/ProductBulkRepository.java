package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductBulkRepository extends JpaRepository<Product, Long> {
  @Modifying(clearAutomatically = true)
  @Query("update Product p " + "set p.isBan = :isBan " + "where p.seller.id = :sellerId")
  int banProductsBySellerId(@Param("sellerId") long sellerId, @Param("isBan") boolean isBan);
}
