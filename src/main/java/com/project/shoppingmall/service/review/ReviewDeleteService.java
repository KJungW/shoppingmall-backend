package com.project.shoppingmall.service.review;

import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.ReviewRepository;
import com.project.shoppingmall.service.alarm.AlarmDeleteService;
import com.project.shoppingmall.service.alarm.AlarmFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.service.report.ReportDeleteService;
import com.project.shoppingmall.service.report.ReportFindService;
import com.project.shoppingmall.service.s3.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewDeleteService {
  private final ReviewService reviewService;
  private final ReviewRepository reviewRepository;
  private final PurchaseItemService purchaseItemService;
  private final ReportFindService reportFindService;
  private final ReportDeleteService reportDeleteService;
  private final AlarmFindService alarmFindService;
  private final AlarmDeleteService alarmDeleteService;
  private final S3Service s3Service;

  public void deleteReview(Review review) {
    if (review == null) throw new ServerLogicError("비어있는 Review을 제거하려고 시도하고 있습니다.");

    List<ReviewReport> reviewReportList = reportFindService.findAllByReview(review.getId());
    reportDeleteService.deleteReviewReportList(reviewReportList);

    List<Alarm> alarmList = alarmFindService.findByTargetReview(review.getId());
    alarmDeleteService.deleteAlarmList(alarmList);

    PurchaseItem purchaseItem =
        purchaseItemService
            .findByReviewId(review.getId())
            .orElseThrow(() -> new ServerLogicError("PurchaseItem과 연결되지 않은 Review가 발견되었습니다."));
    purchaseItem.deleteReview();

    if (!review.getReviewImageUri().isBlank()) {
      s3Service.deleteFile(review.getReviewImageUri());
    }
    reviewRepository.delete(review);
    reviewRepository.flush();

    Product product = review.getProduct();
    ReviewScoresCalcResult scoreCalcResult =
        reviewService.calcReviewScoresInProduct(product.getId());
    product.refreshScore(scoreCalcResult);
  }

  public void deleteReviewList(List<Review> reviews) {
    reviews.forEach(this::deleteReview);
  }

  public void deleteReviewByWriter(Long writerId, Long reviewId) {
    PurchaseItem purchaseItem =
        purchaseItemService
            .findByReviewId(reviewId)
            .orElseThrow(() -> new DataNotFound("Id에 해당하는 리뷰가 존재하지 않습니다."));
    Review review = purchaseItem.getReview();

    if (!review.getWriter().getId().equals(writerId)) {
      throw new DataNotFound("다른 회원의 리뷰입니다.");
    }

    deleteReview(review);
  }
}
