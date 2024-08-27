package com.project.shoppingmall.service_manage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.Review;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.service.EntityManagerService;
import com.project.shoppingmall.service.alarm.AlarmService;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.service_manage.ban.BanManageService;
import com.project.shoppingmall.service_manage.product.ProductManageService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.ProductBuilder;
import com.project.shoppingmall.testdata.ReviewBuilder;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class BanManageServiceTest {
  private BanManageService target;
  private MemberService mockMemberService;
  private ProductService mockProductService;
  private ProductManageService mockProductManageService;
  private ReviewService mockReviewService;
  private AlarmService mockAlarmService;
  private EntityManagerService mockEntityManagerService;

  @BeforeEach
  public void beforeEach() {
    mockMemberService = mock(MemberService.class);
    mockProductService = mock(ProductService.class);
    mockProductManageService = mock(ProductManageService.class);
    mockReviewService = mock(ReviewService.class);
    mockAlarmService = mock(AlarmService.class);
    mockEntityManagerService = mock(EntityManagerService.class);
    target =
        new BanManageService(
            mockMemberService,
            mockProductService,
            mockProductManageService,
            mockReviewService,
            mockAlarmService,
            mockEntityManagerService);
  }

  @Test
  @DisplayName("banMember() : 정상흐름")
  public void banMember_ok() {
    // given
    long givenMemberId = 20L;
    boolean givenIsBan = true;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    ReflectionTestUtils.setField(givenMember, "isBan", !givenIsBan);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    target.banMember(givenMemberId, givenIsBan);

    // then
    assertEquals(givenIsBan, givenMember.getIsBan());

    ArgumentCaptor<Long> alarmListenerId = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmService, times(1)).makeMemberBanAlarm(alarmListenerId.capture());
    assertEquals(givenMember.getId(), alarmListenerId.getValue());

    ArgumentCaptor<Long> sellerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Boolean> productBanCaptor = ArgumentCaptor.forClass(Boolean.class);
    verify(mockProductManageService, times(1))
        .banProductsBySellerId(sellerIdCaptor.capture(), productBanCaptor.capture());
    assertEquals(givenMemberId, sellerIdCaptor.getValue());
    assertEquals(givenIsBan, productBanCaptor.getValue());

    ArgumentCaptor<Long> writerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Boolean> reviewBanCaptor = ArgumentCaptor.forClass(Boolean.class);
    verify(mockReviewService, times(1))
        .banReviewsByWriterId(writerIdCaptor.capture(), reviewBanCaptor.capture());
    assertEquals(givenMemberId, writerIdCaptor.getValue());
    assertEquals(givenIsBan, reviewBanCaptor.getValue());
  }

  @Test
  @DisplayName("banMember() : 현재 회원의 벤상태와 입력값 벤상태가 동일함")
  public void banMember_equalIsBan() {
    // given
    long givenMemberId = 20L;
    boolean givenIsBan = true;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    ReflectionTestUtils.setField(givenMember, "isBan", givenIsBan);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    target.banMember(givenMemberId, givenIsBan);

    // then
    assertEquals(givenIsBan, givenMember.getIsBan());
    verify(mockProductManageService, times(0)).banProductsBySellerId(anyLong(), anyBoolean());
    verify(mockReviewService, times(0)).banReviewsByWriterId(anyLong(), anyBoolean());
  }

  @Test
  @DisplayName("banMember() : 조회된 회원이 존재하지 않음")
  public void banMember_noMember() {
    // given
    long givenMemberId = 20L;
    boolean givenIsBan = true;

    when(mockMemberService.findById(anyLong())).thenReturn(Optional.empty());

    // when
    assertThrows(DataNotFound.class, () -> target.banMember(givenMemberId, givenIsBan));
  }

  @Test
  @DisplayName("banProduct() : 정상흐름")
  public void banProduct_ok() throws IOException {
    // given
    long givenProductId = 20L;
    boolean givenIsBan = true;

    long givenSellerId = 30L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", givenProductId);
    ReflectionTestUtils.setField(givenProduct, "isBan", !givenIsBan);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", givenSellerId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    target.banProduct(givenProductId, givenIsBan);

    // then
    assertEquals(givenIsBan, givenProduct.getIsBan());

    ArgumentCaptor<Long> alarmListenerIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Long> productIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmService, times(1))
        .makeProductBanAlarm(alarmListenerIdCaptor.capture(), productIdCaptor.capture());
    assertEquals(givenSellerId, alarmListenerIdCaptor.getValue());
    assertEquals(givenProduct.getId(), productIdCaptor.getValue());
  }

  @Test
  @DisplayName("banProduct() : 조회된 제품 없음")
  public void banProduct_noProduct() throws IOException {
    // given
    long givenProductId = 20L;
    boolean givenIsBan = true;

    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(DataNotFound.class, () -> target.banProduct(givenProductId, givenIsBan));
  }

  @Test
  @DisplayName("banReview() : 정상흐름")
  public void banReview_ok() throws IOException {
    // given
    long givenReviewId = 20L;
    boolean givenIsBan = true;

    long givenReviewWriterId = 30L;
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "id", givenReviewId);
    ReflectionTestUtils.setField(givenReview, "isBan", !givenIsBan);
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", givenReviewWriterId);
    Mockito.when(mockReviewService.findById(anyLong())).thenReturn(Optional.of(givenReview));

    // when
    target.banReview(givenReviewId, givenIsBan);

    // then
    assertEquals(givenIsBan, givenReview.getIsBan());

    ArgumentCaptor<Long> reviewIdCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Long> alarmListenerIdCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockAlarmService, times(1))
        .makeReviewBanAlarm(alarmListenerIdCaptor.capture(), reviewIdCaptor.capture());
    assertEquals(givenReview.getId(), reviewIdCaptor.getValue());
    assertEquals(givenReview.getWriter().getId(), alarmListenerIdCaptor.getValue());
  }

  @Test
  @DisplayName("banReview() : 조회된 리뷰가 없음")
  public void banReview_noReview() {
    // given
    long givenReviewId = 20L;
    boolean givenIsBan = true;

    Mockito.when(mockReviewService.findById(anyLong())).thenReturn(Optional.empty());

    // when then
    assertThrows(DataNotFound.class, () -> target.banReview(givenReviewId, givenIsBan));
  }
}
