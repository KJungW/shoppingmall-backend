package com.project.shoppingmall.test_dto.review;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.review.ReviewDto;
import com.project.shoppingmall.entity.Review;
import java.util.List;

public class ReviewDtoManager {

  public static void check(Review review, ReviewDto target) {
    assertEquals(review.getId(), target.getId());
    assertEquals(review.getWriter().getId(), target.getWriterId());
    assertEquals(review.getWriter().getNickName(), target.getWriterName());
    assertEquals(review.getProduct().getId(), target.getProductId());
    assertEquals(review.getScore(), target.getScore());
    assertEquals(review.getTitle(), target.getTitle());
    assertEquals(review.getReviewImageDownloadUrl(), target.getReviewImageUrl());
    assertEquals(review.getDescription(), target.getDescription());
  }

  public static void checkList(List<Review> reviews, List<ReviewDto> targets) {
    assertEquals(reviews.size(), targets.size());
    for (int i = 0; i < targets.size(); i++) {
      check(reviews.get(i), targets.get(i));
    }
  }
}
