package com.project.shoppingmall.dto.review;

import com.project.shoppingmall.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewDto {
  private Long id;
  private Long writerId;
  private String writerName;
  private Long productId;
  private Integer score;
  private String title;
  private String reviewImageUrl;
  private String description;

  public ReviewDto(Review review) {
    this.id = review.getId();
    this.writerId = review.getWriter().getId();
    this.writerName = review.getWriter().getNickName();
    this.productId = review.getProduct().getId();
    this.score = review.getScore();
    this.title = review.getTitle();
    this.reviewImageUrl = review.getReviewImageUrl();
    this.description = review.getDescription();
  }
}
