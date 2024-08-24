package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.ProductType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
  @Query(
      "select pt from ProductType pt "
          + "where pt.typeName like concat(:baseProductTypePrefix, '%') ")
  Optional<ProductType> findBaseProductType(
      @Param("baseProductTypePrefix") String baseProductTypePrefix);
}
