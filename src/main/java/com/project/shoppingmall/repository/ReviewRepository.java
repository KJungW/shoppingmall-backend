package com.project.shoppingmall.repository;

import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  @Query(
      "SELECT new com.project.shoppingmall.dto.refund.ReviewScoresCalcResult(COUNT(r), AVG(r.score)) "
          + "FROM Review r "
          + "WHERE r.product.id = :productId")
  ReviewScoresCalcResult calcReviewScoresInProduct(@Param("productId") Long productId);

  @Query("select r from Review r where r.product.id = :productId")
  List<Review> findAllByProduct(@Param("productId") long productId);
}
