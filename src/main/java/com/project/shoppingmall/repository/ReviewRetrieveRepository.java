package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRetrieveRepository extends JpaRepository<Review, Long> {

  @Query(
      "select r from  Review r "
          + "left join fetch r.writer w "
          + "where r.product.id = :productId "
          + "and r.isBan = false ")
  Slice<Review> findAllByProduct(@Param("productId") long productId, Pageable pageable);
}
