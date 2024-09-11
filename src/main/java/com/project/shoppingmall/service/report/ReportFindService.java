package com.project.shoppingmall.service.report;

import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.repository.ProductReportRepository;
import com.project.shoppingmall.repository.ReviewReportRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportFindService {
  private final ProductReportRepository productReportRepository;
  private final ReviewReportRepository reviewReportRepository;

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

  public List<ProductReport> findAllProductReportByReporter(long reporterId) {
    return productReportRepository.findAllByReporter(reporterId);
  }

  public List<ReviewReport> findAllReviewReportByReporter(long reporterId) {
    return reviewReportRepository.findAllByReporter(reporterId);
  }
}
