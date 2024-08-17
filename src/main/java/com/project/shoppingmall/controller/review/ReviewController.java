package com.project.shoppingmall.controller.review;

import com.project.shoppingmall.controller.review.input.InputSaveReview;
import com.project.shoppingmall.controller.review.input.InputUpdateReview;
import com.project.shoppingmall.controller.review.output.OutputSaveReview;
import com.project.shoppingmall.controller.review.output.OutputUpdateReview;
import com.project.shoppingmall.dto.auth.AuthUserDetail;
import com.project.shoppingmall.dto.review.ReviewMakeData;
import com.project.shoppingmall.dto.review.ReviewUpdateData;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.service.ReviewDeleteService;
import com.project.shoppingmall.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService reviewService;
  private final ReviewDeleteService reviewDeleteService;

  @PostMapping("/review")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputSaveReview saveReview(
      @Valid @RequestPart(value = "reviewData") InputSaveReview input,
      @RequestPart(value = "reviewImage", required = false) MultipartFile reviewImage) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    ReviewMakeData makeData =
        ReviewMakeData.builder()
            .writerId(userDetail.getId())
            .purchaseItemId(input.getPurchaseItemId())
            .score(input.getScore())
            .title(input.getTitle())
            .description(input.getDescription())
            .reviewImage(reviewImage)
            .build();
    Review savedReview = reviewService.saveReview(makeData);
    return new OutputSaveReview(savedReview.getId());
  }

  @PutMapping("/review")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public OutputUpdateReview updateReview(
      @Valid @RequestPart(value = "reviewData") InputUpdateReview input,
      @RequestPart(value = "reviewImage", required = false) MultipartFile reviewImage) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    ReviewUpdateData updateData =
        ReviewUpdateData.builder()
            .writerId(userDetail.getId())
            .reviewID(input.getReviewId())
            .score(input.getScore())
            .title(input.getTitle())
            .description(input.getDescription())
            .reviewImage(reviewImage)
            .build();
    Review updateReview = reviewService.updateReview(updateData);
    return new OutputUpdateReview(updateReview.getId());
  }

  @DeleteMapping("/review/{reviewId}")
  @PreAuthorize("hasRole('ROLE_MEMBER')")
  public void deleteReview(@PathVariable("reviewId") Long reviewId) {
    AuthUserDetail userDetail =
        (AuthUserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    reviewDeleteService.deleteReviewByWriter(userDetail.getId(), reviewId);
  }
}
