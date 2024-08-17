package com.project.shoppingmall.service;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.ReviewRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewDeleteService {
  private final ReviewRepository reviewRepository;
  private final PurchaseItemService purchaseItemService;
  private final ReportService reportService;
  private final ReportDeleteService reportDeleteService;

  public void deleteReview(Review review) {
    if (review == null) throw new ServerLogicError("비어있는 Review을 제거하려고 시도하고 있습니다.");

    List<ReviewReport> reviewReportList = reportService.findAllByReview(review.getId());
    PurchaseItem purchaseItem =
        purchaseItemService
            .findByReviewId(review.getId())
            .orElseThrow(() -> new ServerLogicError("PurchaseItem과 연결되지 않은 Review가 발견되었습니다."));

    reportDeleteService.deleteReviewReportList(reviewReportList);
    reviewRepository.delete(review);
    purchaseItem.deleteReview();
  }

  public void deleteReviewList(List<Review> reviews) {
    reviews.forEach(this::deleteReview);
  }
}
