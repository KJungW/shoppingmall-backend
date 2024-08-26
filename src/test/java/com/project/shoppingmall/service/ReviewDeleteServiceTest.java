package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.dto.refund.ReviewScoresCalcResult;
import com.project.shoppingmall.entity.Alarm;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.entity.report.ReviewReport;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ReviewRepository;
import com.project.shoppingmall.service.alarm.AlarmDeleteService;
import com.project.shoppingmall.service.alarm.AlarmFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.service.report.ReportDeleteService;
import com.project.shoppingmall.service.report.ReportService;
import com.project.shoppingmall.service.review.ReviewDeleteService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.testdata.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class ReviewDeleteServiceTest {
  private ReviewDeleteService target;
  private ReviewService mockReviewService;
  private ReviewRepository mockReviewRepository;
  private PurchaseItemService mockPurchaseItemService;
  private ReportService mockReportService;
  private ReportDeleteService mockReportDeleteService;
  private AlarmFindService mockAlarmFindService;
  private AlarmDeleteService mockAlarmDeleteService;
  private S3Service mockS3Service;

  @BeforeEach
  public void beforeEach() {
    mockReviewService = mock(ReviewService.class);
    mockReviewRepository = mock(ReviewRepository.class);
    mockPurchaseItemService = mock(PurchaseItemService.class);
    mockReportService = mock(ReportService.class);
    mockReportDeleteService = mock(ReportDeleteService.class);
    mockAlarmFindService = mock(AlarmFindService.class);
    mockAlarmDeleteService = mock(AlarmDeleteService.class);
    mockS3Service = mock(S3Service.class);
    target =
        new ReviewDeleteService(
            mockReviewService,
            mockReviewRepository,
            mockPurchaseItemService,
            mockReportService,
            mockReportDeleteService,
            mockAlarmFindService,
            mockAlarmDeleteService,
            mockS3Service);
  }

  @Test
  @DisplayName("deleteReviewByWriter() : 정상흐름")
  public void deleteReviewByWriter_ok() throws IOException {
    // given
    // - 인자세팅
    long givenWriterId = 20L;
    long givenReviewId = 27L;

    // - purchaseItemService.findByReviewId() 세팅
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", 23L);

    String givenReviewImageUri = "testReviewImageUri";
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    ReflectionTestUtils.setField(givenReview, "reviewImageUri", givenReviewImageUri);
    ReflectionTestUtils.setField(givenReview, "product", givenProduct);
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", givenWriterId);

    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "review", givenReview);
    when(mockPurchaseItemService.findByReviewId(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // - reportService.findAllByReview() 세팅
    List<ReviewReport> givenReviewReport =
        new ArrayList<ReviewReport>(
            List.of(
                ReviewReportBuilder.fullData().build(),
                ReviewReportBuilder.fullData().build(),
                ReviewReportBuilder.fullData().build()));
    when(mockReportService.findAllByReview(anyLong())).thenReturn(givenReviewReport);

    // - alarmFindService.findByTargetProduct() 세팅
    List<Alarm> givenAlarms =
        new ArrayList<>(
            List.of(
                AlamBuilder.reviewBanFullData().build(), AlamBuilder.reviewBanFullData().build()));
    when(mockAlarmFindService.findByTargetReview(anyLong())).thenReturn(givenAlarms);

    // - reviewService.calcReviewScoresInProduct() 세팅
    ReviewScoresCalcResult givenReviewCalcResult = new ReviewScoresCalcResult(20L, 4.5);
    when(mockReviewService.calcReviewScoresInProduct(anyLong())).thenReturn(givenReviewCalcResult);

    // when
    target.deleteReviewByWriter(givenWriterId, givenReviewId);

    // then
    // - Review와 연결된 ReviewReport 제거 확인
    ArgumentCaptor<List<ReviewReport>> reviewListCaptor = ArgumentCaptor.forClass(List.class);
    verify(mockReportDeleteService, times(1)).deleteReviewReportList(reviewListCaptor.capture());
    assertEquals(givenReviewReport.size(), reviewListCaptor.getValue().size());

    // - Review와 연결된 Alarm 제거 확인
    ArgumentCaptor<List<Alarm>> alarmListCaptor = ArgumentCaptor.forClass(List.class);
    verify(mockAlarmDeleteService, times(1)).deleteAlarmList(alarmListCaptor.capture());
    assertEquals(givenAlarms.size(), alarmListCaptor.getValue().size());

    // - Review를 참조하는 PurchaseItem의 외래키의 null 확인
    assertNull(givenPurchaseItem.getReview());

    // - 리뷰 이미지 제거 확인
    ArgumentCaptor<String> imageUriCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockS3Service, times(1)).deleteFile(imageUriCaptor.capture());
    assertEquals(givenReviewImageUri, imageUriCaptor.getValue());

    // - Review 제거 확인
    ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
    verify(mockReviewRepository, times(1)).delete(reviewCaptor.capture());
    assertSame(givenReview, reviewCaptor.getValue());

    // - 리뷰 제거로 인한 제품의 평점 업데이트 확인
    assertEquals(givenReviewCalcResult.getScoreAverage(), givenProduct.getScoreAvg());
  }

  @Test
  @DisplayName("deleteReviewByWriter() : 다른 회원의 리뷰를 제거하려고 시도")
  public void deleteReviewByWriter_otherMemberReview() throws IOException {
    // given
    long givenWriterId = 20L;
    long givenReviewId = 27L;

    // - purchaseItemService.findByReviewId() 세팅
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", 23L);

    long givenOtherMemberId = 50L;
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", givenOtherMemberId);
    ReflectionTestUtils.setField(givenReview, "product", givenProduct);

    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "review", givenReview);
    when(mockPurchaseItemService.findByReviewId(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // when
    assertThrows(
        DataNotFound.class, () -> target.deleteReviewByWriter(givenWriterId, givenReviewId));
  }
}
