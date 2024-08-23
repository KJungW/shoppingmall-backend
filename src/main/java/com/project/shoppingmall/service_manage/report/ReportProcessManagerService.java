package com.project.shoppingmall.service_manage.report;

import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.AlreadyProcessedReport;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.report.ReportService;
import com.project.shoppingmall.type.ReportResultType;
import com.project.shoppingmall.type.ReportResultTypeForApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportProcessManagerService {
  private final ReportService reportService;

  @Transactional
  public ProductReport processProductReport(
      long productReportId, ReportResultTypeForApi resultType) {
    ProductReport productReport =
        reportService
            .finaProductReportById(productReportId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 제품 신고 데이터가 없습니다."));
    if (!productReport.getReportResult().equals(ReportResultType.WAITING_PROCESSED))
      throw new AlreadyProcessedReport("이미 처리가 완료된 신고데이터 입니다.");

    productReport.completeReportProcess(resultType.toReportResultType());
    return productReport;
  }

  @Transactional
  public ReviewReport processReviewReport(long reviewReportId, ReportResultTypeForApi resultType) {
    ReviewReport reviewReport =
        reportService
            .findReviewReportById(reviewReportId)
            .orElseThrow(() -> new DataNotFound("ID에 해당하는 리뷰 신고 데이터가 없습니다."));
    if (!reviewReport.getReportResult().equals(ReportResultType.WAITING_PROCESSED))
      throw new AlreadyProcessedReport("이미 처리가 완료된 신고데이터 입니다.");

    reviewReport.completeReportProcess(resultType.toReportResultType());
    return reviewReport;
  }
}
