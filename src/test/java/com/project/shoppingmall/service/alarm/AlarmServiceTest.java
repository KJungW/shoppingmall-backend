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
import com.project.shoppingmall.test_entity.member.MemberBuilder;
import com.project.shoppingmall.test_entity.product.ProductBuilder;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.test_entity.refund.RefundBuilder;
import com.project.shoppingmall.test_entity.review.ReviewBuilder;
import com.project.shoppingmall.type.AlarmType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

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
    Member givenListener = MemberBuilder.makeMember(inputListenerId, givenIsBan);

    set_mockMemberFindService_findById(givenListener);

    // when
    Alarm resultAlarm = target.makeMemberBanAlarm(inputListenerId);

    // then
    checkResultAlarm(givenListener, resultAlarm, AlarmType.MEMBER_BAN);
  }

  @Test
  @DisplayName("makeReviewBanAlarm() : 정상흐름")
  public void makeReviewBanAlarm_ok() {
    // given
    long inputReviewId = 30L;

    Member givenWriter = MemberBuilder.makeMember(10L);
    boolean givenReviewBan = true;
    Review givenReview = ReviewBuilder.makeReview(inputReviewId, givenWriter, givenReviewBan);

    set_reviewFindService_findByIdWithWriter(givenReview);

    // when
    Alarm resultAlarm = target.makeReviewBanAlarm(inputReviewId);

    // then
    checkResultAlarm(givenReview, resultAlarm, AlarmType.REVIEW_BAN);
  }

  @DisplayName("makeProductBanAlarm() : 정상흐름")
  public void makeProductBanAlarm_ok() {
    // given
    long inputProductId = 30L;

    Member givenSeller = MemberBuilder.makeMember(30L);
    boolean givenProductBan = true;
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenSeller, givenProductBan);

    set_productFindService_findByIdWithSeller(givenProduct);

    // when
    Alarm resultAlarm = target.makeProductBanAlarm(inputProductId);

    // then
    checkResultAlarm(givenProduct, resultAlarm, AlarmType.PRODUCT_BAN);
  }

  @Test
  @DisplayName("makeRefundRequestAlarm() : 정상흐름")
  public void makeRefundRequestAlarm_ok() {
    // given
    long inputRefundId = 30L;

    Member givenSeller = MemberBuilder.makeMember(20L, false);
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.makePurchaseItem(60L, givenSeller);
    Refund givenRefund = RefundBuilder.make(inputRefundId, givenPurchaseItem);

    set_mockMemberFindService_findById(givenSeller);
    set_refundFindService_findByIdWithPurchaseItemProduct(givenRefund);

    // when
    Alarm resultAlarm = target.makeRefundRequestAlarm(inputRefundId);

    // then
    checkResultAlarm(givenRefund, resultAlarm, AlarmType.REFUND_REQUEST);
  }

  @Test
  @DisplayName("makeTypeDeleteAlarm() : 정상흐름")
  public void makeTypeDeleteAlarm_ok() {
    // given
    long inputProductId = 30L;

    Member givenSeller = MemberBuilder.makeMember(20L, false);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenSeller);

    set_productFindService_findByIdWithSeller(givenProduct);

    // when
    Alarm resultAlarm = target.makeTypeDeleteAlarm(inputProductId);

    // then
    checkResultAlarm(givenProduct, resultAlarm, AlarmType.TYPE_DELETE);
  }

  @Test
  @DisplayName("makeAllTypeDeleteAlarm() : 정상흐름")
  public void makeAllTypeDeleteAlarm_ok() {
    // given
    List<Product> givenProducts = ProductBuilder.makeProductList(List.of(10L, 20L, 30L));

    // when
    List<Alarm> resultAlarms = target.makeAllTypeDeleteAlarm(givenProducts);

    // then
    assertEquals(givenProducts.size(), resultAlarms.size());
    for (int i = 0; i < resultAlarms.size(); i++)
      checkResultAlarm(givenProducts.get(i), resultAlarms.get(i), AlarmType.TYPE_DELETE);
  }

  @Test
  @DisplayName("makeAllTypeDeleteAlarm() : 영속성 컨텍스트에서 관리되지 않는 제품이 입력됨")
  public void makeAllTypeDeleteAlarm_noProduct() {
    // given
    List<Product> givenProducts = ProductBuilder.makeProductList(List.of(10L, 20L, 30L));

    when(mockAlarmRepository.saveAll(any()))
        .thenThrow(new DataIntegrityViolationException("알림이 존재하지 않는 제품을 참조하고 있음"));

    // when
    assertThrows(ServerLogicError.class, () -> target.makeAllTypeDeleteAlarm(givenProducts));
  }

  @Test
  @DisplayName("makeTypeUpdateAlarm() : 정상흐름")
  public void makeTypeUpdateAlarm_ok() {
    // given
    long inputProductId = 30L;

    Member givenSeller = MemberBuilder.makeMember(20L);
    Product givenProduct = ProductBuilder.makeProduct(inputProductId, givenSeller);

    set_productFindService_findByIdWithSeller(givenProduct);

    // when
    Alarm resultAlarm = target.makeTypeUpdateAlarm(inputProductId);

    // then
    checkResultAlarm(givenProduct, resultAlarm, AlarmType.TYPE_UPDATE);
  }

  @Test
  @DisplayName("makeAllTypeUpdateAlarm() : 정상흐름")
  public void makeAllTypeUpdateAlarm_ok() {
    // given
    List<Product> givenProducts = ProductBuilder.makeProductList(List.of(10L, 20L, 30L));

    // when
    List<Alarm> resultAlarms = target.makeAllTypeUpdateAlarm(givenProducts);

    // then
    assertEquals(givenProducts.size(), resultAlarms.size());
    for (int i = 0; i < resultAlarms.size(); i++)
      checkResultAlarm(givenProducts.get(i), resultAlarms.get(i), AlarmType.TYPE_UPDATE);
  }

  @Test
  @DisplayName("makeAllTypeUpdateAlarm() : 영속성 컨텍스트에서 관리되지 않는 제품이 입력됨")
  public void makeAllTypeUpdateAlarm_noProduct() {
    // given
    List<Product> givenProducts = ProductBuilder.makeProductList(List.of(10L, 20L, 30L));

    when(mockAlarmRepository.saveAll(any()))
        .thenThrow(new DataIntegrityViolationException("알림이 존재하지 않는 제품을 참조하고 있음"));

    // when
    assertThrows(ServerLogicError.class, () -> target.makeAllTypeUpdateAlarm(givenProducts));
  }

  public void set_mockMemberFindService_findById(Member givenListener) {
    when(mockMemberFindService.findById(anyLong())).thenReturn(Optional.of(givenListener));
  }

  public void set_reviewFindService_findByIdWithWriter(Review givenReview) {
    when(mockReviewFindService.findByIdWithWriter(anyLong())).thenReturn(Optional.of(givenReview));
  }

  public void set_productFindService_findByIdWithSeller(Product givenProduct) {
    when(mockProductFindService.findByIdWithSeller(anyLong()))
        .thenReturn(Optional.of(givenProduct));
  }

  public void set_refundFindService_findByIdWithPurchaseItemProduct(Refund givenRefund) {
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));
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
