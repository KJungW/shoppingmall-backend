package com.project.shoppingmall.service.alarm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.*;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.final_value.AlarmContentTemplate;
import com.project.shoppingmall.repository.AlarmRepository;
import com.project.shoppingmall.service.member.MemberService;
import com.project.shoppingmall.service.product.ProductService;
import com.project.shoppingmall.service.refund.RefundFindService;
import com.project.shoppingmall.service.review.ReviewService;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.ProductBuilder;
import com.project.shoppingmall.testdata.RefundBuilder;
import com.project.shoppingmall.testdata.ReviewBuilder;
import com.project.shoppingmall.type.AlarmType;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class AlarmServiceTest {
  private AlarmService target;
  private AlarmRepository mockAlarmRepository;
  private MemberService mockMemberService;
  private ReviewService mockReviewService;
  private ProductService mockProductService;
  private RefundFindService mockRefundFindService;

  @BeforeEach
  public void beforeEach() {
    mockAlarmRepository = mock(AlarmRepository.class);
    mockMemberService = mock(MemberService.class);
    mockReviewService = mock(ReviewService.class);
    mockProductService = mock(ProductService.class);
    mockRefundFindService = mock(RefundFindService.class);
    target =
        new AlarmService(
            mockAlarmRepository,
            mockMemberService,
            mockReviewService,
            mockProductService,
            mockRefundFindService);
  }

  @Test
  @DisplayName("makeMemberBanAlarm() : 정상흐름 - 멤버가 벤상태인 경우")
  public void makeMemberBanAlarm_ok_memberBanTrue() {
    // given
    long inputListenerId = 10L;

    boolean givenIsBan = true;
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    ReflectionTestUtils.setField(givenMember, "isBan", givenIsBan);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    Alarm resultAlarm = target.makeMemberBanAlarm(inputListenerId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(AlarmType.MEMBER_BAN, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeMemberBanAlarmContent(givenIsBan), resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertNull(resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeMemberBanAlarm() : 정상흐름 - 멤버가 벤상태가 아닌 경우")
  public void makeMemberBanAlarm_ok_memberBanFalse() {
    // given
    long inputListenerId = 10L;

    boolean givenIsBan = false;
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    ReflectionTestUtils.setField(givenMember, "isBan", givenIsBan);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    // when
    Alarm resultAlarm = target.makeMemberBanAlarm(inputListenerId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(AlarmType.MEMBER_BAN, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeMemberBanAlarmContent(givenIsBan), resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertNull(resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeReviewBanAlarm() : 정상흐름 - 리뷰가 벤상태인 경우")
  public void makeReviewBanAlarm_ok_reviewBanTrue() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputReviewId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    boolean givenIsBan = true;
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "isBan", givenIsBan);
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", inputListenerId);
    when(mockReviewService.findByIdWithWriter(anyLong())).thenReturn(Optional.of(givenReview));

    // when
    Alarm resultAlarm = target.makeReviewBanAlarm(inputListenerId, inputReviewId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(givenReview.getId(), resultAlarm.getTargetReview().getId());
    assertEquals(AlarmType.REVIEW_BAN, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeReviewBanAlarmContent(givenIsBan, givenReview.getTitle()),
        resultAlarm.getContent());
    assertSame(givenReview, resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertNull(resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeReviewBanAlarm() : 정상흐름 - 리뷰가 벤상태가 아닌 경우")
  public void makeReviewBanAlarm_ok_reviewBanFalse() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputReviewId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    boolean givenIsBan = false;
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview, "isBan", givenIsBan);
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", inputListenerId);
    when(mockReviewService.findByIdWithWriter(anyLong())).thenReturn(Optional.of(givenReview));

    // when
    Alarm resultAlarm = target.makeReviewBanAlarm(inputListenerId, inputReviewId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(givenReview.getId(), resultAlarm.getTargetReview().getId());
    assertEquals(AlarmType.REVIEW_BAN, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeReviewBanAlarmContent(givenIsBan, givenReview.getTitle()),
        resultAlarm.getContent());
    assertSame(givenReview, resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertNull(resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeReviewBanAlarm() : 다른 회원의 리뷰에 대한 리뷰벤 알림 생성")
  public void makeReviewBanAlarm_otherMemberReview() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputReviewId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    long otherMemberId = 20L;
    Review givenReview = ReviewBuilder.fullData().build();
    ReflectionTestUtils.setField(givenReview.getWriter(), "id", otherMemberId);
    when(mockReviewService.findByIdWithWriter(anyLong())).thenReturn(Optional.of(givenReview));

    // when then
    assertThrows(
        DataNotFound.class, () -> target.makeReviewBanAlarm(inputListenerId, inputReviewId));
  }

  @Test
  @DisplayName("makeProductBanAlarm() : 정상흐름 - 제품이 벤상태인 경우")
  public void makeProductBanAlarm_ok_productBanTrue() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputProductId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    boolean givenIsBan = true;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", inputProductId);
    ReflectionTestUtils.setField(givenProduct, "isBan", givenIsBan);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", inputListenerId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    Alarm resultAlarm = target.makeProductBanAlarm(inputListenerId, inputProductId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(givenProduct.getId(), resultAlarm.getTargetProduct().getId());
    assertEquals(AlarmType.PRODUCT_BAN, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeProductBanAlarmContent(givenIsBan, givenProduct.getName()),
        resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertSame(givenProduct, resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeProductBanAlarm() : 정상흐름 - 제품이 벤상태가 아닌 경우")
  public void makeProductBanAlarm_ok_productBanFalse() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputProductId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    boolean givenIsBan = false;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", inputProductId);
    ReflectionTestUtils.setField(givenProduct, "isBan", givenIsBan);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", inputListenerId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when
    Alarm resultAlarm = target.makeProductBanAlarm(inputListenerId, inputProductId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(givenProduct.getId(), resultAlarm.getTargetProduct().getId());
    assertEquals(AlarmType.PRODUCT_BAN, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeProductBanAlarmContent(givenIsBan, givenProduct.getName()),
        resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertSame(givenProduct, resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeProductBanAlarm() : 다른 회원의 제품에 대한 제품벤 알림 생성")
  public void makeProductBanAlarm_otherMemberProduct() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputProductId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    long otherMemberId = 20L;
    Product givenProduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenProduct, "id", inputProductId);
    ReflectionTestUtils.setField(givenProduct.getSeller(), "id", otherMemberId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenProduct));

    // when then
    assertThrows(
        DataNotFound.class, () -> target.makeProductBanAlarm(inputListenerId, inputProductId));
  }

  @Test
  @DisplayName("makeRefundRequestAlarm() : 정상흐름")
  public void makeRefundRequestAlarm_ok() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputRefundId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    Refund givenRefund = RefundBuilder.makeRefundWithPurchaseItem();
    ReflectionTestUtils.setField(givenRefund, "id", inputRefundId);
    ReflectionTestUtils.setField(givenRefund.getPurchaseItem(), "sellerId", inputListenerId);
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when
    Alarm resultAlarm = target.makeRefundRequestAlarm(inputListenerId, inputRefundId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(
        givenMember.getId(), resultAlarm.getTargetRefund().getPurchaseItem().getSellerId());
    assertEquals(AlarmType.REFUND_REQUEST, resultAlarm.getAlarmType());
    assertEquals(AlarmContentTemplate.makeRefundRequestAlarmContent(), resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertSame(givenRefund, resultAlarm.getTargetRefund());
    assertNull(resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeRefundRequestAlarm() :다른 회원의 판매상품에 대한 환불요청 알림생성")
  public void makeRefundRequestAlarm_otherMemberRefund() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputRefundId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    long otherMemberId = 20L;
    Refund givenRefund = RefundBuilder.makeRefundWithPurchaseItem();
    ReflectionTestUtils.setField(givenRefund, "id", inputRefundId);
    ReflectionTestUtils.setField(givenRefund.getPurchaseItem(), "sellerId", otherMemberId);
    when(mockRefundFindService.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when then
    assertThrows(
        DataNotFound.class, () -> target.makeRefundRequestAlarm(inputListenerId, inputRefundId));
  }

  @Test
  @DisplayName("makeTypeDeleteAlarm() : 정상흐름")
  public void makeTypeDeleteAlarm_ok() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputProductId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    Product givenproduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenproduct, "id", inputProductId);
    ReflectionTestUtils.setField(givenproduct.getSeller(), "id", inputListenerId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenproduct));

    // when
    Alarm resultAlarm = target.makeTypeDeleteAlarm(inputListenerId, inputProductId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(givenMember.getId(), resultAlarm.getTargetProduct().getSeller().getId());
    assertEquals(AlarmType.TYPE_DELETE, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeTypeDeleteAlarmContent(givenproduct.getName()),
        resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertSame(givenproduct, resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeTypeDeleteAlarm() : 다른 회원의 판매상품에 대한 타입삭제 알림생성")
  public void makeTypeDeleteAlarm_otherMemberProduct() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputProductId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    long otherMemberId = 20L;
    Product givenproduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenproduct, "id", inputProductId);
    ReflectionTestUtils.setField(givenproduct.getSeller(), "id", otherMemberId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenproduct));

    // when then
    assertThrows(
        DataNotFound.class, () -> target.makeTypeDeleteAlarm(inputListenerId, inputProductId));
  }

  @Test
  @DisplayName("makeTypeUpdateAlarm() : 정상흐름")
  public void makeTypeUpdateAlarm_ok() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputProductId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    Product givenproduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenproduct, "id", inputProductId);
    ReflectionTestUtils.setField(givenproduct.getSeller(), "id", inputListenerId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenproduct));

    // when
    Alarm resultAlarm = target.makeTypeUpdateAlarm(inputListenerId, inputProductId);

    // then
    ArgumentCaptor<Alarm> alarmCaptor = ArgumentCaptor.forClass(Alarm.class);
    verify(mockAlarmRepository, times(1)).save(alarmCaptor.capture());
    assertSame(resultAlarm, alarmCaptor.getValue());

    assertEquals(givenMember.getId(), resultAlarm.getListener().getId());
    assertEquals(givenMember.getId(), resultAlarm.getTargetProduct().getSeller().getId());
    assertEquals(AlarmType.TYPE_UPDATE, resultAlarm.getAlarmType());
    assertEquals(
        AlarmContentTemplate.makeTypeUpdateAlarmContent(givenproduct.getName()),
        resultAlarm.getContent());
    assertNull(resultAlarm.getTargetReview());
    assertNull(resultAlarm.getTargetRefund());
    assertSame(givenproduct, resultAlarm.getTargetProduct());
  }

  @Test
  @DisplayName("makeTypeUpdateAlarm() : 다른 회원의 판매상품에 대한 타입수정 알림생성")
  public void makeTypeUpdateAlarm_otherMemberProduct() throws IOException {
    // given
    long inputListenerId = 10L;
    long inputProductId = 30L;

    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", inputListenerId);
    when(mockMemberService.findById(anyLong())).thenReturn(Optional.of(givenMember));

    long otherMemberId = 20L;
    Product givenproduct = ProductBuilder.fullData().build();
    ReflectionTestUtils.setField(givenproduct, "id", inputProductId);
    ReflectionTestUtils.setField(givenproduct.getSeller(), "id", otherMemberId);
    when(mockProductService.findByIdWithSeller(anyLong())).thenReturn(Optional.of(givenproduct));

    // when then
    assertThrows(
        DataNotFound.class, () -> target.makeTypeUpdateAlarm(inputListenerId, inputProductId));
  }
}
