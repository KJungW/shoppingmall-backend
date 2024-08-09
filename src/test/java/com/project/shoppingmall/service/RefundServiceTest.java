package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Purchase;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.DataNotFound;
import com.project.shoppingmall.repository.RefundRepository;
import com.project.shoppingmall.testdata.MemberBuilder;
import com.project.shoppingmall.testdata.PurchaseBuilder;
import com.project.shoppingmall.testdata.PurchaseItemBuilder;
import com.project.shoppingmall.type.PurchaseStateType;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RefundServiceTest {
  private RefundService target;
  private RefundRepository mockRefundRepository;
  private MemberService meockMemberService;
  private PurchaseItemService mockPurchaseItemService;

  @BeforeEach
  public void beforeEach() {
    mockRefundRepository = mock(RefundRepository.class);
    meockMemberService = mock(MemberService.class);
    mockPurchaseItemService = mock(PurchaseItemService.class);
    target = new RefundService(mockRefundRepository, meockMemberService, mockPurchaseItemService);
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
    when(meockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

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
    when(meockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

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
    when(meockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

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
    when(meockMemberService.findById(any())).thenReturn(Optional.of(givenMember));

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
}
