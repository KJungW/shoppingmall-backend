package com.project.shoppingmall.controller.review_retrieve.output;

import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.review.ReviewDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

  public OutputGetReviewsByProduct(SliceResult<ReviewDto> sliceResult) {
    this.currentSliceNumber = sliceResult.getCurrentSliceNumber();
    this.sliceSize = sliceResult.getSliceSize();
    this.isFirst = sliceResult.isFirst();
    this.isLast = sliceResult.isLast();
    this.hasNext = sliceResult.isHasNext();
    this.hasPrevious = sliceResult.isHasPrevious();
    this.reviewsList = sliceResult.getContentList();
  }
}
