package com.project.shoppingmall.repository;

import com.project.shoppingmall.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewBulkRepository extends JpaRepository<Review, Long> {

  @Modifying(clearAutomatically = true)
  @Query("update Review r " + "set r.isBan = :isBan " + "where r.writer.id = :writerId")
  int banReviewsByWriterId(@Param("writerId") long writerId, @Param("isBan") boolean isBan);
}
