package com.project.shoppingmall.controller.review_retrieve;

import com.project.shoppingmall.controller.review_retrieve.output.OutputGetReviewsByProduct;
import com.project.shoppingmall.dto.SliceResult;
import com.project.shoppingmall.dto.review.ReviewDto;
import com.project.shoppingmall.service.review.ReviewRetrieveService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewRetrieveController {
  private final ReviewRetrieveService reviewRetrieveService;

  @GetMapping("product/reviews")
  public OutputGetReviewsByProduct getReviewsByProduct(
      @PositiveOrZero @RequestParam("sliceNumber") Integer sliceNumber,
      @Positive @RequestParam("sliceSize") Integer sliceSize,
      @RequestParam("productId") Long productId) {
    SliceResult<ReviewDto> sliceResult =
        reviewRetrieveService.retrieveByProduct(productId, sliceNumber, sliceSize);
    return new OutputGetReviewsByProduct(sliceResult);
  }
}
