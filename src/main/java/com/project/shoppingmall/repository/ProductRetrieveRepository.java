package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRetrieveRepository extends JpaRepository<Product, Long> {

  @Query(
      "select p from Product p "
          + "left join fetch p.seller s "
          + "left join fetch p.productType pt "
          + "where pt.id = :productTypeId "
          + "and p.saleState = 'ON_SALE' "
          + "and p.isBan = false ")
  Slice<Product> findByProductType(@Param("productTypeId") long productTypeId, Pageable pageable);

  @Query(
      "select p from Product p "
          + "left join fetch p.seller s "
          + "left join fetch p.productType pt "
          + "where p.name like concat('%', :searchWord, '%') "
          + "and p.saleState = 'ON_SALE' "
          + "and p.isBan = false ")
  Slice<Product> findBySearchWord(@Param("searchWord") String searchWord, Pageable pageable);

  @Query(
      "select p from Product p "
          + "left join fetch p.seller s "
          + "left join fetch p.productType pt "
          + "where s.id = :sellerId ")
  Slice<Product> findAllBySeller(@Param("sellerId") long sellerId, Pageable pageable);

  @Query(
      "select p from Product p "
          + "left join fetch p.seller s "
          + "left join fetch p.productType pt "
          + "where p.saleState = 'ON_SALE' "
          + "and p.isBan = false "
          + "order by function('RAND') ")
  Slice<Product> findAllByRandom(Pageable pageable);
}
