package com.project.shoppingmall.service.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.ReviewRepository;
import com.project.shoppingmall.service.alarm.AlarmDeleteService;
import com.project.shoppingmall.service.alarm.AlarmFindService;
import com.project.shoppingmall.service.purchase_item.PurchaseItemFindService;
import com.project.shoppingmall.service.report.ReportDeleteService;
import com.project.shoppingmall.service.report.ReportFindService;
import com.project.shoppingmall.service.s3.S3Service;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewDeleteServiceTest {
  private ReviewDeleteService target;
  private ReviewService mockReviewService;
  private ReviewRepository mockReviewRepository;
  private PurchaseItemFindService mockPurchaseItemFindService;
  private ReportFindService mockReportFindService;
  private ReportDeleteService mockReportDeleteService;
  private AlarmFindService mockAlarmFindService;
  private AlarmDeleteService mockAlarmDeleteService;
  private S3Service mockS3Service;

  @BeforeEach
  public void beforeEach() {
    mockReviewService = mock(ReviewService.class);
    mockReviewRepository = mock(ReviewRepository.class);
    mockPurchaseItemFindService = mock(PurchaseItemFindService.class);
    mockReportFindService = mock(ReportFindService.class);
    mockReportDeleteService = mock(ReportDeleteService.class);
    mockAlarmFindService = mock(AlarmFindService.class);
    mockAlarmDeleteService = mock(AlarmDeleteService.class);
    mockS3Service = mock(S3Service.class);
    target =
        spy(
            new ReviewDeleteService(
                mockReviewService,
                mockReviewRepository,
                mockPurchaseItemFindService,
                mockReportFindService,
                mockReportDeleteService,
                mockAlarmFindService,
                mockAlarmDeleteService,
                mockS3Service));
  }

  @Test
  @DisplayName("deleteReviewInController() : 정상흐름")
  public void deleteReviewInController_ok() {
    // given
    long inputWriterId = 20L;
    long inputReviewId = 27L;

    Product givenProduct = ProductBuilder.makeProduct(23L);
    Member givenWriter = MemberBuilder.makeMember(inputWriterId);
    Review givenReview = ReviewBuilder.makeReview(inputReviewId, givenWriter, givenProduct);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(523L, givenProduct, givenReview);

    when(mockPurchaseItemFindService.findByReviewId(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    doNothing().when(target).deleteReview(any());

    // when
    target.deleteReviewInController(inputWriterId, inputReviewId);
  }

  @Test
  @DisplayName("deleteReviewInController() : 다른 회원의 리뷰를 제거하려고 시도")
  public void deleteReviewInController_otherMemberReview() {
    // given
    long inputWriterId = 20L;
    long inputReviewId = 27L;

    Product givenProduct = ProductBuilder.makeProduct(23L);
    Member otherMember = MemberBuilder.makeMember(34234L);
    Review givenReview = ReviewBuilder.makeReview(inputReviewId, otherMember, givenProduct);
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.makePurchaseItem(523L, givenProduct, givenReview);

    when(mockPurchaseItemFindService.findByReviewId(anyLong()))
        .thenReturn(Optional.of(givenPurchaseItem));
    doNothing().when(target).deleteReview(any());

    // when
    assertThrows(
        DataNotFound.class, () -> target.deleteReviewInController(inputWriterId, inputReviewId));
  }
}
