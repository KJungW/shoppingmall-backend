package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.AlreadyProcessedReport;
import com.project.shoppingmall.exception.ContinuousReportError;
import com.project.shoppingmall.repository.ProductReportRepository;
import com.project.shoppingmall.repository.ReviewReportRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.report.ReportService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.testdata.*;
import com.project.shoppingmall.type.ReportResultType;
import com.project.shoppingmall.type.ReportResultTypeForApi;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Slice;
import org.springframework.test.util.ReflectionTestUtils;

class ReportServiceTest {
  private ReportService target;
  private MemberService mockedMemberService;
  private ProductService mockedProductService;
  private ReviewService mockedReviewService;
  private ProductReportRepository mockedProductReportRepository;
  private ReviewReportRepository mockedReviewReportRepository;

  @BeforeEach
  public void beforeEach() {
    mockedMemberService = mock(MemberService.class);
    mockedProductService = mock(ProductService.class);
    mockedReviewService = mock(ReviewService.class);
    mockedProductReportRepository = mock(ProductReportRepository.class);
    mockedReviewReportRepository = mock(ReviewReportRepository.class);
    target =
        new ReportService(
            mockedMemberService,
            mockedProductService,
            mockedReviewService,
            mockedProductReportRepository,
            mockedReviewReportRepository);
  }

  @Test
  @DisplayName("saveProductReport() : 정상흐름")
  public void saveProductReport_ok() throws IOException {
    // given
    // - 인자세팅
    long rightMemberId = 10L;
    long rightProductId = 20L;
    String rightTitle = "test Title";
    String rightDescription = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockedMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockedProductService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

    // - productReportRepository.findLatestReport() 세팅
    Slice mockedSlice = mock(Slice.class);
    when(mockedSlice.getContent()).thenReturn(new ArrayList<>());
    when(mockedProductReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(mockedSlice);

    // when
    target.saveProductReport(rightMemberId, rightProductId, rightTitle, rightDescription);

    // then
    ArgumentCaptor<ProductReport> reportCaptor = ArgumentCaptor.forClass(ProductReport.class);
    verify(mockedProductReportRepository, times(1)).save(reportCaptor.capture());
    ProductReport reportResult = reportCaptor.getValue();

    assertEquals(rightMemberId, reportResult.getReporter().getId());
    assertEquals(rightTitle, reportResult.getTitle());
    assertEquals(rightDescription, reportResult.getDescription());
    assertFalse(reportResult.isProcessedComplete());
    assertEquals(givenProductId, reportResult.getProduct().getId());
  }

  @Test
  @DisplayName("saveProductReport() : 24시간 이내의 같은 제품 연속신고")
  public void saveProductReport_continuousReportIn24Hour() throws IOException {
    // given
    // - 인자세팅
    long rightMemberId = 10L;
    long rightProductId = 20L;
    String rightTitle = "test Title";
    String rightDescription = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockedMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockedProductService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

    // - productReportRepository.findLatestReport() 세팅
    ProductReport givenLatestReport = ProductReportBuilder.fullData().build();
    ReflectionTestUtils.setField(
        givenLatestReport, "createDate", LocalDateTime.now().minusHours(15));

    Slice mockedSlice = mock(Slice.class);
    when(mockedSlice.getContent()).thenReturn(new ArrayList<>(List.of(givenLatestReport)));
    when(mockedProductReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(mockedSlice);

    // when
    assertThrows(
        ContinuousReportError.class,
        () ->
            target.saveProductReport(rightMemberId, rightProductId, rightTitle, rightDescription));
  }

  @Test
  @DisplayName("saveProductReport() : 24시간 이후의 같은 제품 연속신고")
  public void saveProductReport_continuousReportAfter24Hour() throws IOException {
    // given
    // - 인자세팅
    long rightMemberId = 10L;
    long rightProductId = 20L;
    String rightTitle = "test Title";
    String rightDescription = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", rightMemberId);
    when(mockedMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockedProductService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

    // - productReportRepository.findLatestReport() 세팅
    ProductReport givenLatestReport = ProductReportBuilder.fullData().build();
    ReflectionTestUtils.setField(
        givenLatestReport, "createDate", LocalDateTime.now().minusHours(30));

    Slice mockedSlice = mock(Slice.class);
    when(mockedSlice.getContent()).thenReturn(new ArrayList<>(List.of(givenLatestReport)));
    when(mockedProductReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(mockedSlice);

    // when
    target.saveProductReport(rightMemberId, rightProductId, rightTitle, rightDescription);

    // then
    ArgumentCaptor<ProductReport> reportCaptor = ArgumentCaptor.forClass(ProductReport.class);
    verify(mockedProductReportRepository, times(1)).save(reportCaptor.capture());
    ProductReport reportResult = reportCaptor.getValue();

    assertEquals(rightMemberId, reportResult.getReporter().getId());
    assertEquals(rightTitle, reportResult.getTitle());
    assertEquals(rightDescription, reportResult.getDescription());
    assertFalse(reportResult.isProcessedComplete());
    assertEquals(givenProductId, reportResult.getProduct().getId());
  }

  @Test
  @DisplayName("saveReviewReport() : 정상흐름")
  public void saveReviewReport_ok() throws IOException {
    // given
    // - 인자세팅
    long givenReporterId = 10L;
    long givenReviewId = 20L;
    String givenTitle = "test Title";
    String givenDesc = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenReporterId);
    when(mockedMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - reviewService.findById() 세팅
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    when(mockedReviewService.findById(anyLong())).thenReturn(Optional.of(givenReview));

    // - reviewReportRepository.findLatestReport() 세팅
    Slice mockedSlice = mock(Slice.class);
    when(mockedSlice.getContent()).thenReturn(new ArrayList<>());
    when(mockedReviewReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(mockedSlice);

    // when
    target.saveReviewReport(givenReporterId, givenReviewId, givenTitle, givenDesc);

    // then
    ArgumentCaptor<ReviewReport> reportCaptor = ArgumentCaptor.forClass(ReviewReport.class);
    verify(mockedReviewReportRepository, times(1)).save(reportCaptor.capture());
    ReviewReport reportResult = reportCaptor.getValue();

    assertEquals(givenReporterId, reportResult.getReporter().getId());
    assertEquals(givenTitle, reportResult.getTitle());
    assertEquals(givenDesc, reportResult.getDescription());
    assertFalse(reportResult.isProcessedComplete());
    assertEquals(givenReviewId, reportResult.getReview().getId());
  }

  @Test
  @DisplayName("saveReviewReport() : 24시간 이내의 같은 제품 연속신고")
  public void saveReviewReport_continuousReportIn24Hour() throws IOException {
    // given
    // - 인자세팅
    long givenReporterId = 10L;
    long givenReviewId = 20L;
    String givenTitle = "test Title";
    String givenDesc = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenReporterId);
    when(mockedMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - reviewService.findById() 세팅
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    when(mockedReviewService.findById(anyLong())).thenReturn(Optional.of(givenReview));

    // - reviewReportRepository.findLatestReport() 세팅
    ReviewReport givenLatestReport = ReviewReportBuilder.fullData().build();
    ReflectionTestUtils.setField(
        givenLatestReport, "createDate", LocalDateTime.now().minusHours(15));

    Slice mockedSlice = mock(Slice.class);
    when(mockedSlice.getContent()).thenReturn(new ArrayList<>(List.of(givenLatestReport)));
    when(mockedReviewReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(mockedSlice);

    // when then
    assertThrows(
        ContinuousReportError.class,
        () -> target.saveReviewReport(givenReporterId, givenReviewId, givenTitle, givenDesc));
  }

  @Test
  @DisplayName("saveReviewReport() : 24시간 이후의 같은 제품 연속신고")
  public void saveReviewReport_continuousReportAfter24Hour() throws IOException {
    // given
    // - 인자세팅
    long givenReporterId = 10L;
    long givenReviewId = 20L;
    String givenTitle = "test Title";
    String givenDesc = "test Description";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenReporterId);
    when(mockedMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - reviewService.findById() 세팅
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    when(mockedReviewService.findById(anyLong())).thenReturn(Optional.of(givenReview));

    // - reviewReportRepository.findLatestReport() 세팅
    ReviewReport givenLatestReport = ReviewReportBuilder.fullData().build();
    ReflectionTestUtils.setField(
        givenLatestReport, "createDate", LocalDateTime.now().minusHours(30));

    Slice mockedSlice = mock(Slice.class);
    when(mockedSlice.getContent()).thenReturn(new ArrayList<>(List.of(givenLatestReport)));
    when(mockedReviewReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(mockedSlice);

    // when
    target.saveReviewReport(givenReporterId, givenReviewId, givenTitle, givenDesc);

    // then
    ArgumentCaptor<ReviewReport> reportCaptor = ArgumentCaptor.forClass(ReviewReport.class);
    verify(mockedReviewReportRepository, times(1)).save(reportCaptor.capture());
    ReviewReport reportResult = reportCaptor.getValue();

    assertEquals(givenReporterId, reportResult.getReporter().getId());
    assertEquals(givenTitle, reportResult.getTitle());
    assertEquals(givenDesc, reportResult.getDescription());
    assertFalse(reportResult.isProcessedComplete());
    assertEquals(givenReviewId, reportResult.getReview().getId());
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
    when(mockedProductReportRepository.findById(anyLong()))
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
    when(mockedProductReportRepository.findById(anyLong()))
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
    when(mockedReviewReportRepository.findById(anyLong()))
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
    when(mockedReviewReportRepository.findById(anyLong()))
        .thenReturn(Optional.of(givenReviewReport));

    // when
    assertThrows(
        AlreadyProcessedReport.class,
        () -> target.processReviewReport(inputReviewReportId, inputResultType));
  }
}
