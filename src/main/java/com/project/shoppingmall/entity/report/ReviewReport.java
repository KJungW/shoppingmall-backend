package com.project.shoppingmall.entity.report;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.exception.ServerLogicError;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@DiscriminatorValue("REVIEW_REPORT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReport extends Report {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REVIEW_ID")
  private Review review;

  @Builder
  public ReviewReport(Member reporter, String title, String description, Review review) {
    super(reporter, title, description);
    updateReview(review);
  }

  private void updateReview(Review review) {
    if (review == null) throw new ServerLogicError("ReviewReport의 review에 빈값이 들어왔습니다.");
    this.review = review;
  }
}
