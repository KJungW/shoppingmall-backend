package com.project.shoppingmall.service.review;

import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.repository.ProductReportRepository;
import com.project.shoppingmall.repository.ReviewReportRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportDeleteService {
  private final ProductReportRepository productReportRepository;
  private final ReviewReportRepository reviewReportRepository;

  public void deleteProductReport(ProductReport productReport) {
    if (productReport == null) throw new ServerLogicError("비어있는 ProductReport를 제거하려고 시도하고 있습니다.");
    productReportRepository.delete(productReport);
  }

  public void deleteProductReportList(List<ProductReport> productReports) {
    productReportRepository.deleteAllInBatch(productReports);
  }

  public void deleteReviewReport(ReviewReport reviewReport) {
    if (reviewReport == null) throw new ServerLogicError("비어있는 ReviewReport를 제거하려고 시도하고 있습니다.");
    reviewReportRepository.delete(reviewReport);
  }

  public void deleteReviewReportList(List<ReviewReport> reviewReports) {
    reviewReportRepository.deleteAllInBatch(reviewReports);
  }
}
