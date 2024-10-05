package com.project.shoppingmall.service_manage.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.AlreadyProcessedReport;
import com.project.shoppingmall.service.report.ReportFindService;
import com.project.shoppingmall.test_entity.report.ProductReportBuilder;
import com.project.shoppingmall.test_entity.report.ReviewReportBuilder;
import com.project.shoppingmall.type.ReportResultType;
import com.project.shoppingmall.type.ReportResultTypeForApi;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReportProcessManageServiceTest {
  private ReportProcessManageService target;
  private ReportFindService mockReportFindService;

  @BeforeEach
  public void beforeEach() {
    mockReportFindService = mock(ReportFindService.class);
    target = new ReportProcessManageService(mockReportFindService);
  }

  @Test
  @DisplayName("processReviewReport() : 정상흐름")
  public void processProductReport_ok() {
    // given
    long inputProductReportId = 10L;
    ReportResultTypeForApi inputResultType = ReportResultTypeForApi.MEMBER_BAN;

    ProductReport givenProductReport =
        ProductReportBuilder.make(inputProductReportId, ReportResultType.WAITING_PROCESSED);

    when(mockReportFindService.finaProductReportById(anyLong()))
        .thenReturn(Optional.of(givenProductReport));

    // when
    target.processProductReport(inputProductReportId, inputResultType);

    // then
    assertTrue(givenProductReport.getIsProcessedComplete());
    assertEquals(inputResultType.toReportResultType(), givenProductReport.getReportResult());
  }

  @Test
  @DisplayName("processReviewReport() : 이미 처리가 완료된 신고일 경우")
  public void processProductReport_alreadyReport() {
    // given
    long inputProductReportId = 10L;
    ReportResultTypeForApi inputResultType = ReportResultTypeForApi.MEMBER_BAN;

    ProductReport givenProductReport =
        ProductReportBuilder.make(inputProductReportId, ReportResultType.NO_ACTION);

    when(mockReportFindService.finaProductReportById(anyLong()))
        .thenReturn(Optional.of(givenProductReport));

    // when
    assertThrows(
        AlreadyProcessedReport.class,
        () -> target.processProductReport(inputProductReportId, inputResultType));
  }

  @Test
  @DisplayName("processReviewReport() : 정상흐름")
  public void processReviewReport_ok() {
    // given
    long inputReviewReportId = 10L;
    ReportResultTypeForApi inputResultType = ReportResultTypeForApi.TARGET_BAN;

    ReviewReport givenReviewReport =
        ReviewReportBuilder.make(inputReviewReportId, ReportResultType.WAITING_PROCESSED);

    when(mockReportFindService.findReviewReportById(anyLong()))
        .thenReturn(Optional.of(givenReviewReport));

    // when
    target.processReviewReport(inputReviewReportId, inputResultType);

    // then
    assertTrue(givenReviewReport.getIsProcessedComplete());
    assertEquals(inputResultType.toReportResultType(), givenReviewReport.getReportResult());
  }

  @Test
  @DisplayName("processReviewReport() : 이미 처리가 완료된 신고일 경우")
  public void processReviewReport_alreadyReport() {
    // given
    long inputReviewReportId = 10L;
    ReportResultTypeForApi inputResultType = ReportResultTypeForApi.TARGET_BAN;

    ReviewReport givenReviewReport =
        ReviewReportBuilder.make(inputReviewReportId, ReportResultType.NO_ACTION);

    when(mockReportFindService.findReviewReportById(anyLong()))
        .thenReturn(Optional.of(givenReviewReport));

    // when
    assertThrows(
        AlreadyProcessedReport.class,
        () -> target.processReviewReport(inputReviewReportId, inputResultType));
  }
}
