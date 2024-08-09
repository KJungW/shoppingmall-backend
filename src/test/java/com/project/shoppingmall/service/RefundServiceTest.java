package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.exception.NotRequestStateRefund;
import com.project.shoppingmall.repository.RefundRepository;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.PurchaseBuilder;
import com.project.shoppingmall.testdata.PurchaseItemBuilder;
import com.project.shoppingmall.testdata.RefundBuilder;
import com.project.shoppingmall.type.PurchaseStateType;
import com.project.shoppingmall.type.RefundStateType;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RefundServiceTest {
  private RefundService target;
  private RefundRepository mockRefundRepository;
  private MemberService mockMemberService;
  private PurchaseItemService mockPurchaseItemService;

  @BeforeEach
  public void beforeEach() {
    mockRefundRepository = mock(RefundRepository.class);
    mockMemberService = mock(MemberService.class);
    mockPurchaseItemService = mock(PurchaseItemService.class);
    target = new RefundService(mockRefundRepository, mockMemberService, mockPurchaseItemService);
  }

  @Test
  @DisplayName("saveRefund() : 정상흐름")
  public void saveRefund_ok() throws IOException {
    // given
    // - 인자세팅
    Long givenMemberId = 20L;
    Long givenPurchaseItemId = 23L;
    String givenRequestTitle = "testRefundRequestTitle";
    String givenRequestContent = "testRefundRequestContent";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - purchaseItemService.findByIdWithPurchaseAndRefund() 세팅
    Purchase givenPurchase = PurchaseBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchase.getBuyer(), "id", givenMemberId);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.COMPLETE);
    int givenFinalPrice = 100000;
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.fullData().finalPrice(givenFinalPrice).build();
    ReflectionTestUtils.setField(givenPurchaseItem, "id", givenPurchaseItemId);
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    when(mockPurchaseItemService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // - purchaseItemService.refundIsPossible() 세팅
    when(mockPurchaseItemService.refundIsPossible(any())).thenReturn(true);

    // when
    Refund resultRefund =
        target.saveRefund(
            givenMemberId, givenPurchaseItemId, givenRequestTitle, givenRequestContent);

    // then
    assertEquals(givenFinalPrice, resultRefund.getRefundPrice());
    assertEquals(givenRequestTitle, resultRefund.getRequestTitle());
    assertEquals(givenRequestContent, resultRefund.getRequestContent());
    assertEquals(1, givenPurchaseItem.getRefunds().size());
  }

  @Test
  @DisplayName("saveRefund() : 결제되지 않은 구매에 대한 환불 요청")
  public void saveRefund_noPayment() throws IOException {
    // given
    // - 인자세팅
    Long givenMemberId = 20L;
    Long givenPurchaseItemId = 23L;
    String givenRequestTitle = "testRefundRequestTitle";
    String givenRequestContent = "testRefundRequestContent";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - purchaseItemService.findByIdWithPurchaseAndRefund() 세팅
    Purchase givenPurchase = PurchaseBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchase.getBuyer(), "id", givenMemberId);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.FAIL);
    int givenFinalPrice = 100000;
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.fullData().finalPrice(givenFinalPrice).build();
    ReflectionTestUtils.setField(givenPurchaseItem, "id", givenPurchaseItemId);
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    when(mockPurchaseItemService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.saveRefund(
                givenMemberId, givenPurchaseItemId, givenRequestTitle, givenRequestContent));
  }

  @Test
  @DisplayName("saveRefund() : 다른 회원의 구매에 대한 환불 요청")
  public void saveRefund_otherMemberPurchaseItem() throws IOException {
    // given
    // - 인자세팅
    Long givenMemberId = 20L;
    Long givenPurchaseItemId = 23L;
    String givenRequestTitle = "testRefundRequestTitle";
    String givenRequestContent = "testRefundRequestContent";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - purchaseItemService.findByIdWithPurchaseAndRefund() 세팅
    Long wrongMemberId = 50L;
    Purchase givenPurchase = PurchaseBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchase.getBuyer(), "id", wrongMemberId);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.FAIL);
    int givenFinalPrice = 100000;
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.fullData().finalPrice(givenFinalPrice).build();
    ReflectionTestUtils.setField(givenPurchaseItem, "id", givenPurchaseItemId);
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    when(mockPurchaseItemService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.saveRefund(
                givenMemberId, givenPurchaseItemId, givenRequestTitle, givenRequestContent));
  }

  @Test
  @DisplayName("saveRefund() : 완료되었거나 진행중인 환불이 이미 존재")
  public void saveRefund_alreadyProcessedOrCompleteRefund() throws IOException {
    // given
    // - 인자세팅
    Long givenMemberId = 20L;
    Long givenPurchaseItemId = 23L;
    String givenRequestTitle = "testRefundRequestTitle";
    String givenRequestContent = "testRefundRequestContent";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - purchaseItemService.findByIdWithPurchaseAndRefund() 세팅
    Long wrongMemberId = 50L;
    Purchase givenPurchase = PurchaseBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchase.getBuyer(), "id", wrongMemberId);
    ReflectionTestUtils.setField(givenPurchase, "state", PurchaseStateType.FAIL);
    int givenFinalPrice = 100000;
    PurchaseItem givenPurchaseItem =
        PurchaseItemBuilder.fullData().finalPrice(givenFinalPrice).build();
    ReflectionTestUtils.setField(givenPurchaseItem, "id", givenPurchaseItemId);
    ReflectionTestUtils.setField(givenPurchaseItem, "purchase", givenPurchase);
    when(mockPurchaseItemService.findByIdWithPurchaseAndRefund(any()))
        .thenReturn(Optional.of(givenPurchaseItem));

    // - purchaseItemService.refundIsPossible() 세팅
    when(mockPurchaseItemService.refundIsPossible(any())).thenReturn(false);

    // when
    assertThrows(
        DataNotFound.class,
        () ->
            target.saveRefund(
                givenMemberId, givenPurchaseItemId, givenRequestTitle, givenRequestContent));
  }

  @Test
  @DisplayName("acceptRefund() : 정상흐름")
  public void acceptRefund_ok() throws IOException {
    // given
    // - 인자 세팅
    long givenMemberId = 10L;
    long givenRefundId = 20L;
    String givenResponseMessage = "testMessage";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - refundRepository.findByIdWithPurchaseItemProduct() 세팅
    Refund givenRefund = RefundBuilder.fullData().build();
    givenRefund.registerPurchaseItem(PurchaseItemBuilder.fullData().build());
    ReflectionTestUtils.setField(
        givenRefund.getPurchaseItem().getProduct().getSeller(), "id", givenMemberId);
    ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.REQUEST);
    when(mockRefundRepository.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when
    Refund result = target.acceptRefund(givenMemberId, givenRefundId, givenResponseMessage);

    // then
    assertEquals(RefundStateType.ACCEPT, result.getState());
    assertEquals(givenResponseMessage, result.getResponseContent());
  }

  @Test
  @DisplayName("acceptRefund() : 자신이 판매하지 않는 제품에 대한 환불데이터 승인요청")
  public void acceptRefund_otherSellerRefund() throws IOException {
    // given
    // - 인자 세팅
    long givenMemberId = 10L;
    long givenRefundId = 20L;
    String givenResponseMessage = "testMessage";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - refundRepository.findByIdWithPurchaseItemProduct() 세팅
    long wrongMemberId = 40;
    Refund givenRefund = RefundBuilder.fullData().build();
    givenRefund.registerPurchaseItem(PurchaseItemBuilder.fullData().build());
    ReflectionTestUtils.setField(
        givenRefund.getPurchaseItem().getProduct().getSeller(), "id", wrongMemberId);
    ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.REQUEST);
    when(mockRefundRepository.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when then
    assertThrows(
        DataNotFound.class,
        () -> target.acceptRefund(givenMemberId, givenRefundId, givenResponseMessage));
  }

  @Test
  @DisplayName("acceptRefund() : request상태가 아닌 환불에 해당 승인요청")
  public void acceptRefund_noRequest() throws IOException {
    // given
    // - 인자 세팅
    long givenMemberId = 10L;
    long givenRefundId = 20L;
    String givenResponseMessage = "testMessage";

    // - memberService.findById() 세팅
    Member givenMember = MemberBuilder.fullData().build();
    ReflectionTestUtils.setField(givenMember, "id", givenMemberId);
    when(mockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

    // - refundRepository.findByIdWithPurchaseItemProduct() 세팅
    Refund givenRefund = RefundBuilder.fullData().build();
    givenRefund.registerPurchaseItem(PurchaseItemBuilder.fullData().build());
    ReflectionTestUtils.setField(
        givenRefund.getPurchaseItem().getProduct().getSeller(), "id", givenMemberId);
    ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.ACCEPT);
    when(mockRefundRepository.findByIdWithPurchaseItemProduct(anyLong()))
        .thenReturn(Optional.of(givenRefund));

    // when then
    assertThrows(
        NotRequestStateRefund.class,
        () -> target.acceptRefund(givenMemberId, givenRefundId, givenResponseMessage));
  }
}
