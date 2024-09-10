package com.project.shoppingmall.service.alarm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.final_value.AlarmContentTemplate;
import com.project.shoppingmall.repository.AlarmRepository;
import com.project.shoppingmall.service.member.MemberFindService;
import com.project.shoppingmall.service.product.ProductFindService;
import com.project.shoppingmall.service.refund.RefundFindService;
import com.project.shoppingmall.service.review.ReviewFindService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.ProductBuilder;
import com.project.shoppingmall.testdata.RefundBuilder;
import com.project.shoppingmall.testdata.ReviewBuilder;
import com.project.shoppingmall.type.AlarmType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

class AlarmServiceTest {
  private AlarmService target;
  private AlarmRepository mockAlarmRepository;
  private MemberFindService mockMemberFindService;
  private ReviewFindService mockReviewFindService;
  private ProductFindService mockProductFindService;
  private RefundFindService mockRefundFindService;

  @BeforeEach
  public void beforeEach() {
    mockAlarmRepository = mock(AlarmRepository.class);
    mockMemberFindService = mock(MemberFindService.class);
    mockReviewFindService = mock(ReviewFindService.class);
    mockProductFindService = mock(ProductFindService.class);
    mockRefundFindService = mock(RefundFindService.class);
    target =
        new AlarmService(
            mockAlarmRepository,
            mockMemberFindService,
            mockReviewFindService,
            mockProductFindService,
            mockRefundFindService);
  }

  @Test
  @DisplayName("makeMemberBanAlarm() : 정상흐름")
  public void makeMemberBanAlarm_ok() {
    // given
    long inputListenerId = 10L;

    boolean givenIsBan = true;
    Member givenMember = set_mockMemberFindService_findById(inputListenerId, givenIsBan);

    // when
    Alarm resultAlarm = target.makeMemberBanAlarm(inputListenerId);

    // then
    checkResultAlarm(givenMember, resultAlarm, AlarmType.MEMBER_BAN);
  }

  @Test
  @DisplayName("makeReviewBanAlarm() : 정상흐름")
  public void makeReviewBanAlarm_ok() throws IOException {
    // given
    long inputReviewId = 30L;

    long givenWriterId = 10L;
    boolean givenIsBan = true;
    Review givenReview = set_reviewFindService_findByIdWithWriter(givenWriterId, givenIsBan);

    // when
    Alarm resultAlarm = target.makeReviewBanAlarm(inputReviewId);

    // then
    checkResultAlarm(givenReview, resultAlarm, AlarmType.REVIEW_BAN);
  }

  @DisplayName("makeProductBanAlarm() : 정상흐름")
  public void makeProductBanAlarm_ok() throws IOException {
    // given
    long inputProductId = 30L;

    boolean givenIsBan = true;
    long givenSellerId = 10L;
    Product givenProduct =
        set_productFindService_findByIdWithSeller(inputProductId, givenSellerId, givenIsBan);

    // when
    Alarm resultAlarm = target.makeProductBanAlarm(inputProductId);

    // then
    checkResultAlarm(givenProduct, resultAlarm, AlarmType.PRODUCT_BAN);
  }

  @Test
  @DisplayName("makeRefundRequestAlarm() : 정상흐름")
  public void makeRefundRequestAlarm_ok() throws IOException {
    // given
    long inputRefundId = 30L;

    long givenSellerId = 20L;
    Refund givenRefund = set_refundFindService_findByIdWithPurchaseItemProduct(30L, 20L);
    Member givenSeller = set_mockMemberFindService_findById(givenSellerId, true);

    // when
    Alarm resultAlarm = target.makeRefundRequestAlarm(inputRefundId);

    // then
    checkResultAlarm(givenRefund, resultAlarm, AlarmType.REFUND_REQUEST);
  }

  @Test
  @DisplayName("makeTypeDeleteAlarm() : 정상흐름")
  public void makeTypeDeleteAlarm_ok() throws IOException {
    // given
    long inputProductId = 30L;

    long givenSellerId = 20L;
    Product givenProduct =
        set_productFindService_findByIdWithSeller(inputProductId, givenSellerId, true);

    // when
    Alarm resultAlarm = target.makeTypeDeleteAlarm(inputProductId);

    // then
    checkResultAlarm(givenProduct, resultAlarm, AlarmType.TYPE_DELETE);
  }

  @Test
  @DisplayName("makeAllTypeDeleteAlarm() : 정상흐름")
  public void makeAllTypeDeleteAlarm_ok() throws IOException {
    // given
    List<Product> givenProducts =
        new ArrayList<>(
            List.of(
                ProductBuilder.lightData().build(),
                ProductBuilder.lightData().build(),
                ProductBuilder.lightData().build()));

    // when
    List<Alarm> resultAlarms = target.makeAllTypeDeleteAlarm(givenProducts);

    // then
    assertEquals(givenProducts.size(), resultAlarms.size());
    for (int i = 0; i < resultAlarms.size(); i++)
      checkResultAlarm(givenProducts.get(i), resultAlarms.get(i), AlarmType.TYPE_DELETE);
  }

  @Test
  @DisplayName("makeAllTypeDeleteAlarm() : 영속성 컨텍스트에서 관리되지 않는 제품이 입력됨")
  public void makeAllTypeDeleteAlarm_noProduct() throws IOException {
    // given
    List<Product> givenProducts =
        new ArrayList<>(
            List.of(
                ProductBuilder.lightData().build(),
                ProductBuilder.lightData().build(),
                ProductBuilder.lightData().build()));

    when(mockAlarmRepository.saveAll(any()))
        .thenThrow(new DataIntegrityViolationException("알림이 존재하지 않는 제품을 참조하고 있음"));

    // when
    assertThrows(ServerLogicError.class, () -> target.makeAllTypeDeleteAlarm(givenProducts));
  }

  @Test
  @DisplayName("makeTypeUpdateAlarm() : 정상흐름")
  public void makeTypeUpdateAlarm_ok() throws IOException {
    // given
    long inputProductId = 30L;

    long givenSellerId = 20L;
    Product givenProduct =
        set_productFindService_findByIdWithSeller(inputProductId, givenSellerId, true);

    // when
    Alarm resultAlarm = target.makeTypeUpdateAlarm(inputProductId);

    // then
    checkResultAlarm(givenProduct, resultAlarm, AlarmType.TYPE_UPDATE);
  }

  @Test
  @DisplayName("makeAllTypeUpdateAlarm() : 정상흐름")
  public void makeAllTypeUpdateAlarm_ok() throws IOException {
    // given
    List<Product> givenProducts =
        new ArrayList<>(
            List.of(
                ProductBuilder.lightData().build(),
                ProductBuilder.lightData().build(),
                ProductBuilder.lightData().build()));

    // when
    List<Alarm> resultAlarms = target.makeAllTypeUpdateAlarm(givenProducts);

    // then
    assertEquals(givenProducts.size(), resultAlarms.size());
    for (int i = 0; i < resultAlarms.size(); i++)
      checkResultAlarm(givenProducts.get(i), resultAlarms.get(i), AlarmType.TYPE_UPDATE);
  }

  @Test
  @DisplayName("makeAllTypeUpdateAlarm() : 영속성 컨텍스트에서 관리되지 않는 제품이 입력됨")
  public void makeAllTypeUpdateAlarm_noProduct() throws IOException {
    // given
    List<Product> givenProducts =
        new ArrayList<>(
            List.of(
                ProductBuilder.lightData().build(),
                ProductBuilder.lightData().build(),
                ProductBuilder.lightData().build()));

    when(mockAlarmRepository.saveAll(any()))
        .thenThrow(new DataIntegrityViolationException("알림이 존재하지 않는 제품을 참조하고 있음"));

    // when
    assertThrows(ServerLogicError.class, () -> target.makeAllTypeUpdateAlarm(givenProducts));
  }

  public Member set_mockMemberFindService_findById(long memberId, boolean isBan) {
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", memberId);
    ReflectionTestUtils.setField(givenMember, "isBan", isBan);
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenMember));
    return givenMember;
  }

  public Review set_reviewFindService_findByIdWithWriter(long writerId, boolean isBan)
      throws IOException {
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "isBan", isBan);
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", writerId);
    when(mockReviewFindService.findByIdWithWriter(anyLong())).thenReturn(Optional.of(givenReview));
    return givenReview;
  }

  public Product set_productFindService_findByIdWithSeller(
      long productId, long sellerId, boolean isBan) throws IOException {
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", productId);
    ReflectionTestUtils.setField(givenProduct, "isBan", isBan);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", sellerId);
    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));
    return givenProduct;
  }

  public Refund set_refundFindService_findByIdWithPurchaseItemProduct(long refundId, long sellerId)
      throws IOException {
    Refund givenRefund = RefundBuilder.makeRefundWithPurchaseItem();
    ReflectionTestUtils.setField(givenRefund, "id", refundId);
    ReflectionTestUtils.setField(givenRefund.getPurchaseItem(), "sellerId", sellerId);
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));
    return givenRefund;
  }

  public void checkResultAlarm(Member givenListener, Alarm resultAlarm, AlarmType alarmType) {
    assertEquals(givenListener.getId(), resultAlarm.getListener().getId());
    assertEquals(alarmType, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeMemberBanAlarmContent(givenListener.getIsBan()),
        resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertNull(resultAlarm.getTargetProduct());
  }

  public void checkResultAlarm(Review givenReview, Alarm resultAlarm, AlarmType alarmType) {
    assertEquals(givenReview.getId(), resultAlarm.getTargetReview().getId());
    assertEquals(givenReview.getWriter().getId(), resultAlarm.getListener().getId());
    assertEquals(alarmType, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeReviewBanAlarmContent(
            givenReview.getIsBan(), givenReview.getTitle()),
        resultAlarm.getContent());
    assertSame(givenReview, resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertNull(resultAlarm.getTargetProduct());
  }

  public void checkResultAlarm(Product givenProduct, Alarm resultAlarm, AlarmType alarmType) {
    assertEquals(givenProduct.getId(), resultAlarm.getTargetProduct().getId());
    assertEquals(givenProduct.getSeller().getId(), resultAlarm.getListener().getId());
    assertEquals(alarmType, resultAlarm.getAlarmType());
    switch (alarmType) {
      case PRODUCT_BAN -> assertEquals(
          AlarmContentTemplate.makeProductBanAlarmContent(
              givenProduct.getIsBan(), givenProduct.getName()),
          resultAlarm.getContent());
      case TYPE_UPDATE -> assertEquals(
          AlarmContentTemplate.makeTypeUpdateAlarmContent(givenProduct.getName()),
          resultAlarm.getContent());
      case TYPE_DELETE -> assertEquals(
          AlarmContentTemplate.makeTypeDeleteAlarmContent(givenProduct.getName()),
          resultAlarm.getContent());
      default -> throw new ServerLogicError("제품을 타겟으로한 새로운 알림타입이 처리되지 않았습니다.");
    }
    assertNull(resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertSame(givenProduct, resultAlarm.getTargetProduct());
  }

  public void checkResultAlarm(Refund givenRefund, Alarm resultAlarm, AlarmType alarmType) {
    assertEquals(givenRefund.getPurchaseItem().getSellerId(), resultAlarm.getListener().getId());
    assertEquals(alarmType, resultAlarm.getAlarmType());
    assertEquals(AlarmContentTemplate.makeRefundRequestAlarmContent(), resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertSame(givenRefund, resultAlarm.getTargetRefund());
    assertNull(resultAlarm.getTargetProduct());
  }
}
