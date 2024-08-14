package com.project.shoppingmall.controller.review_retrieve.output;

import com.project.shoppingmall.dto.review.ReviewDto;
import com.project.shoppingmall.entity.Review;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@AllArgsConstructor
public class OutputGetReviewsByProduct {
  private Integer currentSliceNumber;
  private Integer sliceSize;
  private Boolean isFirst;
  private Boolean isLast;
  private Boolean hasNext;
  private Boolean hasPrevious;
  private List<ReviewDto> reviewsList;

  public OutputGetReviewsByProduct(Slice<Review> sliceResult) {
    this.currentSliceNumber = sliceResult.getNumber();
    this.sliceSize = sliceResult.getSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.hasNext();
    this.hasPrevious = sliceResult.hasPrevious();
    this.reviewsList = sliceResult.getContent().stream().map(ReviewDto::new).toList();
  }
}
