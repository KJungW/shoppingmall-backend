package com.project.shoppingmall.service.report;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.ContinuousReportError;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ProductReportRepository;
import com.project.shoppingmall.repository.ReviewReportRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.review.ReviewService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {
  private final MemberFindService memberFindService;
  private final ProductFindService productFindService;
  private final ReviewService reviewService;
  private final ProductReportRepository productReportRepository;
  private final ReviewReportRepository reviewReportRepository;

  @Transactional
  public void saveProductReport(long memberId, long productId, String title, String description) {
    Member reporter =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));
    Product product =
        productFindService
            .findByIdWithSeller(productId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 제품이 존재하지 않습니다."));

    if (checkProductReportIsWithinOneDay(memberId, productId)) {
      throw new ContinuousReportError("연속으로 신고를 진행할 수 없습니다. 24시간이 지난뒤에 신고해주세요");
    }

    ProductReport report =
        ProductReport.builder()
            .reporter(reporter)
            .title(title)
            .description(description)
            .product(product)
            .build();
    productReportRepository.save(report);
  }

  @Transactional
  public void saveReviewReport(long memberId, long reviewId, String title, String description) {
    Member reporter =
        memberFindService
            .findById(memberId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 회원이 존재하지 않습니다."));
    Review review =
        reviewService
            .findById(reviewId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 제품이 존재하지 않습니다."));

    if (checkReviewReportIsWithinOneDay(memberId, reviewId)) {
      throw new ContinuousReportError("연속으로 신고를 진행할 수 없습니다. 24시간이 지난뒤에 신고해주세요");
    }

    ReviewReport report =
        ReviewReport.builder()
            .reporter(reporter)
            .title(title)
            .description(description)
            .review(review)
            .build();
    reviewReportRepository.save(report);
  }

  private boolean checkProductReportIsWithinOneDay(Long memberId, Long productId) {
    PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<ProductReport> sliceResult =
        productReportRepository.findLatestReports(memberId, productId, pageRequest);
    if (!sliceResult.getContent().isEmpty()) {
      ProductReport latestReport = sliceResult.getContent().get(0);
      return latestReport.getCreateDate().isAfter(LocalDateTime.now().minusDays(1));
    }
    return false;
  }

  private boolean checkReviewReportIsWithinOneDay(Long memberId, Long reviewId) {
    PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "createDate"));
    Slice<ReviewReport> sliceResult =
        reviewReportRepository.findLatestReports(memberId, reviewId, pageRequest);
    if (!sliceResult.getContent().isEmpty()) {
      ReviewReport latestReport = sliceResult.getContent().get(0);
      return latestReport.getCreateDate().isAfter(LocalDateTime.now().minusDays(1));
    }
    return false;
  }

  public Optional<ProductReport> finaProductReportById(long productReviewId) {
    return productReportRepository.findById(productReviewId);
  }

  public Optional<ReviewReport> findReviewReportById(long reviewReportId) {
    return reviewReportRepository.findById(reviewReportId);
  }

  public List<ProductReport> findAllByProduct(long productId) {
    return productReportRepository.findAllByProduct(productId);
  }

  public List<ReviewReport> findAllByReview(long reviewId) {
    return reviewReportRepository.findAllByReview(reviewId);
  }
}
