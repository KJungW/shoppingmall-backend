package com.project.shoppingmall.service.report;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ProductReport;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.ContinuousReportError;
import com.project.shoppingmall.repository.ProductReportRepository;
import com.project.shoppingmall.repository.ReviewReportRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.test_dto.SliceManager;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.report.*;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import com.project.shoppingmall.type.ReportResultType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Slice;

class ReportServiceTest {
  private ReportService target;
  private MemberFindService mockMemberFindService;
  private ProductFindService mockProductFindService;
  private ReviewFindService mockReviewFindService;
  private ProductReportRepository mockedProductReportRepository;
  private ReviewReportRepository mockedReviewReportRepository;

  @BeforeEach
  public void beforeEach() {
    mockMemberFindService = mock(MemberFindService.class);
    mockProductFindService = mock(ProductFindService.class);
    mockReviewFindService = mock(ReviewFindService.class);
    mockedProductReportRepository = mock(ProductReportRepository.class);
    mockedReviewReportRepository = mock(ReviewReportRepository.class);
    target =
        new ReportService(
            mockMemberFindService,
            mockProductFindService,
            mockReviewFindService,
            mockedProductReportRepository,
            mockedReviewReportRepository);
  }

  @Test
  @DisplayName("saveProductReport() : 정상흐름")
  public void saveProductReport_ok() {
    // given
    Long inputMemberId = 10L;
    Long inputProductId = 20L;
    String inputTitle = "test Title";
    String inputDescription = "test Description";

    Member givenReporter = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId);
    Slice<ProductReport> givenLatestReportSlice =
        SliceManager.setMockSlice(0, 1, new ArrayList<>());

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenReporter));
    when(mockProductFindService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));
    when(mockedProductReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(givenLatestReportSlice);

    // when
    ProductReport result =
        target.saveProductReport(inputMemberId, inputProductId, inputTitle, inputDescription);

    // then
    ProductReportChecker.check(
        givenReporter,
        inputTitle,
        inputDescription,
        false,
        ReportResultType.WAITING_PROCESSED,
        givenProduct,
        result);
  }

  @Test
  @DisplayName("saveProductReport() : 24시간 이내의 같은 제품 연속신고")
  public void saveProductReport_continuousReportIn24Hour() {
    // given
    Long inputMemberId = 10L;
    Long inputProductId = 20L;
    String inputTitle = "test Title";
    String inputDescription = "test Description";

    Member givenReporter = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId);
    ProductReport previousReport =
        ProductReportBuilder.make(
            30L, ReportResultType.WAITING_PROCESSED, LocalDateTime.now().minusHours(15));
    Slice<ProductReport> givenLatestReportSlice =
        SliceManager.setMockSlice(0, 1, List.of(previousReport));

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenReporter));
    when(mockProductFindService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));
    when(mockedProductReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(givenLatestReportSlice);

    // when
    assertThrows(
        ContinuousReportError.class,
        () ->
            target.saveProductReport(inputMemberId, inputProductId, inputTitle, inputDescription));
  }

  @Test
  @DisplayName("saveProductReport() : 24시간 이후의 같은 제품 연속신고")
  public void saveProductReport_continuousReportAfter24Hour() {
    // given
    Long inputMemberId = 10L;
    Long inputProductId = 20L;
    String inputTitle = "test Title";
    String inputDescription = "test Description";

    Member givenReporter = MemberBuilder.makeMember(inputMemberId);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId);
    ProductReport previousReport =
        ProductReportBuilder.make(
            30L, ReportResultType.WAITING_PROCESSED, LocalDateTime.now().minusHours(25));
    Slice<ProductReport> givenLatestReportSlice =
        SliceManager.setMockSlice(0, 1, List.of(previousReport));

    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenReporter));
    when(mockProductFindService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));
    when(mockedProductReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(givenLatestReportSlice);

    // when
    ProductReport result =
        target.saveProductReport(inputMemberId, inputProductId, inputTitle, inputDescription);

    // then
    ProductReportChecker.check(
        givenReporter,
        inputTitle,
        inputDescription,
        false,
        ReportResultType.WAITING_PROCESSED,
        givenProduct,
        result);
  }

  @Test
  @DisplayName("saveReviewReport() : 정상흐름")
  public void saveReviewReport_ok() {
    // given
    Long inputReporterId = 10L;
    Long inputReviewId = 20L;
    String inputTitle = "test Title";
    String inputDescription = "test Description";

    Member givenReporter = MemberBuilder.makeMember(inputReporterId);
    Product givenProduct = ProductBuilder.makeProduct(214124L);
    Review givenReview = ReviewBuilder.makeReview(inputReviewId, givenReporter, givenProduct);
    Slice<ReviewReport> givenLatestReportSlice = SliceManager.setMockSlice(0, 1, List.of());

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenReporter));
    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));
    when(mockedReviewReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(givenLatestReportSlice);

    // when
    ReviewReport result =
        target.saveReviewReport(inputReporterId, inputReviewId, inputTitle, inputDescription);

    // then
    ReviewReportChecker.check(
        givenReporter,
        inputTitle,
        inputDescription,
        false,
        ReportResultType.WAITING_PROCESSED,
        givenReview,
        result);
  }

  @Test
  @DisplayName("saveReviewReport() : 24시간 이내의 같은 제품 연속신고")
  public void saveReviewReport_continuousReportIn24Hour() {
    // given
    Long inputReporterId = 10L;
    Long inputReviewId = 20L;
    String inputTitle = "test Title";
    String inputDescription = "test Description";

    Member givenReporter = MemberBuilder.makeMember(inputReporterId);
    Product givenProduct = ProductBuilder.makeProduct(214124L);
    Review givenReview = ReviewBuilder.makeReview(inputReviewId, givenReporter, givenProduct);
    ReviewReport givenPreviousReport =
        ReviewReportBuilder.make(
            13210L, ReportResultType.WAITING_PROCESSED, LocalDateTime.now().minusHours(15));
    Slice<ReviewReport> givenLatestReportSlice =
        SliceManager.setMockSlice(0, 1, List.of(givenPreviousReport));

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenReporter));
    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));
    when(mockedReviewReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(givenLatestReportSlice);

    // when then
    assertThrows(
        ContinuousReportError.class,
        () ->
            target.saveReviewReport(inputReporterId, inputReviewId, inputTitle, inputDescription));
  }

  @Test
  @DisplayName("saveReviewReport() : 24시간 이후의 같은 제품 연속신고")
  public void saveReviewReport_continuousReportAfter24Hour() {
    // given
    Long inputReporterId = 10L;
    Long inputReviewId = 20L;
    String inputTitle = "test Title";
    String inputDescription = "test Description";

    Member givenReporter = MemberBuilder.makeMember(inputReporterId);
    Product givenProduct = ProductBuilder.makeProduct(214124L);
    Review givenReview = ReviewBuilder.makeReview(inputReviewId, givenReporter, givenProduct);
    ReviewReport givenPreviousReport =
        ReviewReportBuilder.make(
            13210L, ReportResultType.WAITING_PROCESSED, LocalDateTime.now().minusHours(25));
    Slice<ReviewReport> givenLatestReportSlice =
        SliceManager.setMockSlice(0, 1, List.of(givenPreviousReport));

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenReporter));
    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));
    when(mockedReviewReportRepository.findLatestReports(anyLong(), anyLong(), any()))
        .thenReturn(givenLatestReportSlice);

    // when
    ReviewReport result =
        target.saveReviewReport(inputReporterId, inputReviewId, inputTitle, inputDescription);

    // then
    ReviewReportChecker.check(
        givenReporter,
        inputTitle,
        inputDescription,
        false,
        ReportResultType.WAITING_PROCESSED,
        givenReview,
        result);
  }
}
