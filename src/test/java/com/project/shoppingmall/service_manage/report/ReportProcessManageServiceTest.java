package com.project.shoppingmall.service_manage.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.AlreadyProcessedReport;
import com.project.shoppingmall.service.report.ReportService;
import com.project.shoppingmall.testdata.ProductReportBuilder;
import com.project.shoppingmall.testdata.ReviewReportBuilder;
import com.project.shoppingmall.type.ReportResultType;
import com.project.shoppingmall.type.ReportResultTypeForApi;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReportProcessManageServiceTest {
  private ReportProcessManageService target;
  private ReportService mockReportService;

  @BeforeEach
  public void beforeEach() {
    mockReportService = mock(ReportService.class);
    target = new ReportProcessManageService(mockReportService);
  }

  @Test
  @DisplayName("processReviewReport() : 정상흐름")
  public void processProductReport_ok() throws IOException {
    // given
    // - 인자세팅
    long inputProductReportId = 10L;
    ReportResultTypeForApi inputResultType = ReportResultTypeForApi.MEMBER_BAN;

    ProductReport givenProductReport = ProductReportBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProductReport, "id", inputProductReportId);
    ReflectionTestUtils.setField(
        givenProductReport, "reportResult", ReportResultType.WAITING_PROCESSED);
    when(mockReportService.finaProductReportById(anyLong()))
        .thenReturn(Optional.of(givenProductReport));

    // when
    target.processProductReport(inputProductReportId, inputResultType);

    // then
    assertTrue(givenProductReport.isProcessedComplete());
    assertEquals(inputResultType.toReportResultType(), givenProductReport.getReportResult());
  }

  @Test
  @DisplayName("processReviewReport() : 이미 처리가 완료된 신고일 경우")
  public void processProductReport_alreadyReport() throws IOException {
    // given
    long inputProductReportId = 10L;
    ReportResultTypeForApi inputResultType = ReportResultTypeForApi.MEMBER_BAN;

    ProductReport givenProductReport = ProductReportBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProductReport, "id", inputProductReportId);
    ReflectionTestUtils.setField(givenProductReport, "reportResult", ReportResultType.MEMBER_BAN);
    when(mockReportService.finaProductReportById(anyLong()))
        .thenReturn(Optional.of(givenProductReport));

    // when
    assertThrows(
        AlreadyProcessedReport.class,
        () -> target.processProductReport(inputProductReportId, inputResultType));
  }

  @Test
  @DisplayName("processReviewReport() : 정상흐름")
  public void processReviewReport_ok() throws IOException {
    // given
    long inputReviewReportId = 10L;
    ReportResultTypeForApi inputResultType = ReportResultTypeForApi.TARGET_BAN;

    ReviewReport givenReviewReport = ReviewReportBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReviewReport, "id", inputReviewReportId);
    ReflectionTestUtils.setField(
        givenReviewReport, "reportResult", ReportResultType.WAITING_PROCESSED);
    when(mockReportService.findReviewReportById(anyLong()))
        .thenReturn(Optional.of(givenReviewReport));

    // when
    target.processReviewReport(inputReviewReportId, inputResultType);

    // then
    assertTrue(givenReviewReport.isProcessedComplete());
    assertEquals(inputResultType.toReportResultType(), givenReviewReport.getReportResult());
  }

  @Test
  @DisplayName("processReviewReport() : 이미 처리가 완료된 신고일 경우")
  public void processReviewReport_alreadyReport() throws IOException {
    // given
    long inputReviewReportId = 10L;
    ReportResultTypeForApi inputResultType = ReportResultTypeForApi.TARGET_BAN;

    ReviewReport givenReviewReport = ReviewReportBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReviewReport, "id", inputReviewReportId);
    ReflectionTestUtils.setField(givenReviewReport, "reportResult", ReportResultType.MEMBER_BAN);
    when(mockReportService.findReviewReportById(anyLong()))
        .thenReturn(Optional.of(givenReviewReport));

    // when
    assertThrows(
        AlreadyProcessedReport.class,
        () -> target.processReviewReport(inputReviewReportId, inputResultType));
  }
}
