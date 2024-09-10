package com.project.shoppingmall.service;

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
import com.project.shoppingmall.service.report.ReportService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.testdata.*;
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
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockProductFindService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

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
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockProductFindService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

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
    when(mockMemberFindService.findById(any())).thenReturn(Optional.of(givenMember));

    // - productService.findByIdWithSeller() 세팅
    Long givenSellerId = 50L;
    Long givenProductId = 24L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockProductFindService.findByIdWithSeller(any())).thenReturn(Optional.of(givenProduct));

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
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - reviewService.findById() 세팅
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));

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
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - reviewService.findById() 세팅
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));

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
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // - reviewService.findById() 세팅
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));

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
}
