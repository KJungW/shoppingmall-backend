package com.project.shoppingmall.service_manage.ban;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.EntityManagerService;
import com.project.shoppingmall.service.alarm.AlarmService;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.service_manage.product.ProductManageService;
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class BanManageServiceTest {
  private BanManageService target;
  private MemberFindService mockMemberFindService;
  private ProductFindService mockProductFindService;
  private ProductManageService mockProductManageService;
  private ReviewService mockReviewService;
  private ReviewFindService mockReviewFindService;
  private AlarmService mockAlarmService;
  private EntityManagerService mockEntityManagerService;

  @BeforeEach
  public void beforeEach() {
    mockMemberFindService = mock(MemberFindService.class);
    mockProductFindService = mock(ProductFindService.class);
    mockProductManageService = mock(ProductManageService.class);
    mockReviewService = mock(ReviewService.class);
    mockReviewFindService = mock(ReviewFindService.class);
    mockAlarmService = mock(AlarmService.class);
    mockEntityManagerService = mock(EntityManagerService.class);
    target =
        new BanManageService(
            mockMemberFindService,
            mockProductFindService,
            mockProductManageService,
            mockReviewService,
            mockReviewFindService,
            mockAlarmService,
            mockEntityManagerService);
  }

  @Test
  @DisplayName("banMember() : 정상흐름")
  public void banMember_ok() {
    // given
    long inputMemberId = 20L;
    boolean inputIsBan = true;

    Member givenMember = MemberBuilder.makeMember(inputMemberId, !inputIsBan);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    Member result = target.banMember(inputMemberId, inputIsBan);

    // then
    checkManagerBan(inputIsBan, result);
    check_alarmService_makeMemberBanAlarm(inputMemberId);
    check_productManageService_banProductsBySellerId(inputMemberId, inputIsBan);
    check_reviewService_banReviewsByWriterId(inputMemberId, inputIsBan);
  }

  @Test
  @DisplayName("banMember() : 현재 회원의 벤상태와 입력값 벤상태가 동일함")
  public void banMember_equalIsBan() {
    // given
    long inputMemberId = 20L;
    boolean inputIsBan = true;

    Member givenMember = MemberBuilder.makeMember(inputMemberId, inputIsBan);

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    Member result = target.banMember(inputMemberId, inputIsBan);

    // then
    checkManagerBan(inputIsBan, result);
    check_alarmService_makeMemberBanAlarm_notWork();
    check_productManageService_banProductsBySellerId_notWork();
    check_reviewService_banReviewsByWriterId_notWork();
  }

  @Test
  @DisplayName("banMember() : 조회된 회원이 존재하지 않음")
  public void banMember_noMember() {
    // given
    long inputMemberId = 20L;
    boolean inputIsBan = true;

    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.banMember(inputMemberId, inputIsBan));
  }

  @Test
  @DisplayName("banProduct() : 정상흐름")
  public void banProduct_ok() {
    // given
    long inputProductId = 20L;
    boolean inputIsBan = true;

    Member givenSeller = MemberBuilder.makeMember(12323L);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenSeller, !inputIsBan);

    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));

    // when
    Product result = target.banProduct(inputProductId, inputIsBan);

    // then
    checkProductBan(inputIsBan, result);
    check_alarmService_makeProductBanAlarm(inputProductId);
  }

  @Test
  @DisplayName("banProduct() : 조회된 제품 없음")
  public void banProduct_noProduct() {
    // given
    long inputProductId = 20L;
    boolean inputIsBan = true;

    when(mockProductFindService.findByIdWithSeller(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(DataNotFound.class, () -> target.banProduct(inputProductId, inputIsBan));
  }

  @Test
  @DisplayName("banReview() : 정상흐름")
  public void banReview_ok() {
    // given
    long inputReviewId = 20L;
    boolean inputIsBan = true;

    Member givenWriter = MemberBuilder.makeMember(30L);
    Review givenReview = ReviewBuilder.makeReview(inputReviewId, givenWriter, !inputIsBan);

    Mockito.when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.of(givenReview));

    // when
    Review result = target.banReview(inputReviewId, inputIsBan);

    // then
    checkReviewBan(inputIsBan, result);
    check_alarmService_makeReviewBanAlarm(inputReviewId);
  }

  @Test
  @DisplayName("banReview() : 조회된 리뷰가 없음")
  public void banReview_noReview() {
    // given
    long inputReviewId = 20L;
    boolean inputIsBan = true;

    Mockito.when(mockReviewFindService.findById(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(DataNotFound.class, () -> target.banReview(inputReviewId, inputIsBan));
  }

  public void checkManagerBan(boolean isBan, Member target) {
    assertEquals(isBan, target.getIsBan());
  }

  public void checkProductBan(boolean isBan, Product target) {
    assertEquals(isBan, target.getIsBan());
  }

  public void checkReviewBan(boolean isBan, Review target) {
    assertEquals(isBan, target.getIsBan());
  }

  public void check_alarmService_makeMemberBanAlarm(long expectedMemberId) {
    ArgumentCaptor<Long> alarmListenerId = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmService, times(1)).makeMemberBanAlarm(alarmListenerId.capture());
    assertEquals(expectedMemberId, alarmListenerId.getValue());
  }

  public void check_alarmService_makeMemberBanAlarm_notWork() {
    verify(mockAlarmService, times(0)).makeMemberBanAlarm(anyLong());
  }

  public void check_alarmService_makeProductBanAlarm(long expectedProductId) {
    ArgumentCaptor<Long> productIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmService, times(1)).makeProductBanAlarm(productIdCaptor.capture());
    assertEquals(expectedProductId, productIdCaptor.getValue());
  }

  public void check_alarmService_makeProductBanAlarm_notWork(long expectedProductId) {
    verify(mockAlarmService, times(0)).makeProductBanAlarm(anyLong());
  }

  public void check_alarmService_makeReviewBanAlarm(long expectedReviewId) {
    ArgumentCaptor<Long> reviewIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmService, times(1)).makeReviewBanAlarm(reviewIdCaptor.capture());
    assertEquals(expectedReviewId, reviewIdCaptor.getValue());
  }

  public void check_alarmService_makeReviewBanAlarm_noWork(long expectedReviewId) {
    verify(mockAlarmService, times(0)).makeReviewBanAlarm(anyLong());
  }

  public void check_productManageService_banProductsBySellerId(
      long expectedMemberId, boolean expectedIsBan) {
    ArgumentCaptor<Long> sellerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Boolean> productBanCaptor = ArgumentCaptor.forClass(Boolean.class);
    verify(mockProductManageService, times(1))
        .banProductsBySellerId(sellerIdCaptor.capture(), productBanCaptor.capture());
    assertEquals(expectedMemberId, sellerIdCaptor.getValue());
    assertEquals(expectedIsBan, productBanCaptor.getValue());
  }

  public void check_productManageService_banProductsBySellerId_notWork() {
    verify(mockProductManageService, times(0)).banProductsBySellerId(anyLong(), anyBoolean());
  }

  public void check_reviewService_banReviewsByWriterId(
      long expectedMemberId, boolean expectedIsBan) {
    ArgumentCaptor<Long> writerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Boolean> reviewBanCaptor = ArgumentCaptor.forClass(Boolean.class);
    verify(mockReviewService, times(1))
        .banReviewsByWriterId(writerIdCaptor.capture(), reviewBanCaptor.capture());
    assertEquals(expectedMemberId, writerIdCaptor.getValue());
    assertEquals(expectedIsBan, reviewBanCaptor.getValue());
  }

  public void check_reviewService_banReviewsByWriterId_notWork() {
    verify(mockReviewService, times(0)).banReviewsByWriterId(anyLong(), anyBoolean());
  }
}
