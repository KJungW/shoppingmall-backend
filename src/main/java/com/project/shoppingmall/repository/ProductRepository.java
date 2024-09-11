package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query(
      "select p from Product p "
          + "left join fetch p.seller "
          + "left join fetch p.productType "
          + "left join fetch p.contents "
          + "where p.id = :productId ")
  Optional<Product> findByIdWithAll(@Param("productId") Long productId);

  @EntityGraph(attributePaths = {"seller"})
  @Query("select p from Product p where p.id = :productId ")
  Optional<Product> findByIdWithSeller(@Param("productId") Long productId);

  @Query("select p from Product p " + "left join p.productType t " + "where t.id = :typeId ")
  Slice<Product> findProductsByTypeInBatch(@Param("typeId") Long typeId, Pageable pageable);

  @Query("select p from Product p where p.seller.id = :sellerId ")
  List<Product> findAllBySeller(@Param("sellerId") Long sellerId);
}
