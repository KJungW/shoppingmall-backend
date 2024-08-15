package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRetrieveRepository extends JpaRepository<Product, Long> {

  @EntityGraph(attributePaths = {"seller", "productType"})
  Slice<Product> findByProductTypeIdAndIsBan(Long productTypeId, boolean isban, Pageable pageable);

  @EntityGraph(attributePaths = {"seller", "productType"})
  Slice<Product> findByNameContainingIgnoreCaseAndIsBan(
      String searchWord, boolean isban, Pageable pageable);

  @EntityGraph(attributePaths = {"seller", "productType"})
  Slice<Product> findAllBySeller(Member seller, Pageable pageable);

  @EntityGraph(attributePaths = {"seller", "productType"})
  @Query("select p from Product p " + "order by function('RAND') ")
  Slice<Product> findAllByRandom(Pageable pageable);
}
