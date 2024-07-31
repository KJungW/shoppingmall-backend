package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(
      "select p from Product p "
          + "left join fetch p.seller "
          + "left join fetch p.productType "
          + "left join fetch p.contents "
          + "left join fetch p.singleOption "
          + "where p.id = :productId ")
  public Optional<Product> findByIdWithAll(@Param("productId") Long productId);
}
