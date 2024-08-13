package com.project.shoppingmall.dto.refund;

import lombok.Getter;

@Getter
public class ReviewScoresCalcResult {
  private Long reviewCount;
  private Double scoreAverage;

  public ReviewScoresCalcResult(Long reviewCount, Double scoreAverage) {
    this.reviewCount = reviewCount;
    this.scoreAverage = scoreAverage;

    if (reviewCount == null) this.reviewCount = 0l;
    if (scoreAverage == null) this.scoreAverage = 0.0;
  }
}
