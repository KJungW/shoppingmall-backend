package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRetrieveRepository extends JpaRepository<Product, Long> {

  @EntityGraph(attributePaths = {"seller", "productType", "productImages"})
  Slice<Product> findByProductTypeIdAndIsBan(Long productTypeId, boolean isban, Pageable pageable);

  @EntityGraph(attributePaths = {"seller", "productType", "productImages"})
  Slice<Product> findByNameContainingIgnoreCaseAndIsBan(
      String searchWord, boolean isban, Pageable pageable);
}
