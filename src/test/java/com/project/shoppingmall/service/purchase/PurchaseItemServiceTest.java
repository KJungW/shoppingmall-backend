package com.project.shoppingmall.service.purchase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.repository.PurchaseItemRepository;
import com.project.shoppingmall.service.purchase_item.PurchaseItemService;
import com.project.shoppingmall.test_entity.purchaseitem.PurchaseItemBuilder;
import com.project.shoppingmall.test_entity.refund.RefundBuilder;
import com.project.shoppingmall.type.RefundStateType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PurchaseItemServiceTest {
  private PurchaseItemService target;
  private PurchaseItemRepository mockPurchaseItemRepository;

  @BeforeEach
  public void beforeEach() {
    mockPurchaseItemRepository = mock(PurchaseItemRepository.class);
    target = new PurchaseItemService(mockPurchaseItemRepository);
  }

  @Test
  @DisplayName("selectRefundByState() : 정상흐름")
  public void selectRefundByState_ok() {
    // given
    PurchaseItem inputPurchaseItem = PurchaseItemBuilder.makePurchaseItem(231L);
    RefundStateType inputRefundState = RefundStateType.ACCEPT;

    List<Refund> givenAcceptRefunds =
        RefundBuilder.makeList(List.of(1L, 2L, 3L), RefundStateType.ACCEPT, inputPurchaseItem);
    List<Refund> givenCompleteRefunds =
        RefundBuilder.makeList(List.of(4L, 5L, 6L), RefundStateType.COMPLETE, inputPurchaseItem);

    // when
    List<Refund> result = target.selectRefundByState(inputPurchaseItem, inputRefundState);

    // then
    checkRefundSelectResult(givenAcceptRefunds, result);
  }

  @Test
  @DisplayName("selectRefundByState() : 인자 purchaseItem에 환불데이터가 존재하지 않음")
  public void selectRefundByState_EmptyRefund() {
    // given
    PurchaseItem inputPurchaseItem = PurchaseItemBuilder.makePurchaseItem(231L);
    RefundStateType inputRefundState = RefundStateType.ACCEPT;

    // when
    List<Refund> resultRefunds = target.selectRefundByState(inputPurchaseItem, inputRefundState);

    // then
    assertEquals(0, resultRefunds.size());
  }

  @Test
  @DisplayName("refundIsPossible() : 환불이 가능한 구매아이템의 경우")
  public void refundIsPossible_ok() {
    // given
    PurchaseItem inputPurchaseItem = PurchaseItemBuilder.makePurchaseItem(231L);

    RefundBuilder.makeList(List.of(4L, 5L, 6L), RefundStateType.REJECTED, inputPurchaseItem);

    // when
    boolean result = target.refundIsPossible(inputPurchaseItem);

    // then
    assertTrue(result);
  }

  @Test
  @DisplayName("refundIsPossible() : 이미 완료된 환불이 존재하는 경우")
  public void refundIsPossible_complete() {
    // given
    PurchaseItem inputPurchaseItem = PurchaseItemBuilder.makePurchaseItem(231L);

    RefundBuilder.makeList(List.of(4L, 5L, 6L), RefundStateType.REJECTED, inputPurchaseItem);
    RefundBuilder.make(7L, RefundStateType.COMPLETE, inputPurchaseItem);

    // when
    boolean result = target.refundIsPossible(inputPurchaseItem);

    // then
    assertFalse(result);
  }

  @Test
  @DisplayName("refundIsPossible() : 이미 승인된 환불이 존재하는 경우")
  public void refundIsPossible_accept() {
    // given
    PurchaseItem inputPurchaseItem = PurchaseItemBuilder.makePurchaseItem(231L);

    RefundBuilder.makeList(List.of(4L, 5L, 6L), RefundStateType.REJECTED, inputPurchaseItem);
    RefundBuilder.make(7L, RefundStateType.ACCEPT, inputPurchaseItem);

    // when
    boolean result = target.refundIsPossible(inputPurchaseItem);

    // then
    assertFalse(result);
  }

  @Test
  @DisplayName("refundIsPossible() : 이미 신청중인 환불이 존재하는 경우")
  public void refundIsPossible_request() {
    // given
    PurchaseItem inputPurchaseItem = PurchaseItemBuilder.makePurchaseItem(231L);

    RefundBuilder.makeList(List.of(4L, 5L, 6L), RefundStateType.REJECTED, inputPurchaseItem);
    RefundBuilder.make(7L, RefundStateType.REQUEST, inputPurchaseItem);

    // when
    boolean result = target.refundIsPossible(inputPurchaseItem);

    // then
    assertFalse(result);
  }

  @Test
  @DisplayName("refundIsPossible() : 인자 purchaseItem에 환불데이터가 존재하지 않음")
  public void refundIsPossible_EmptyRefund() {
    // given
    PurchaseItem inputPurchaseItem = PurchaseItemBuilder.makePurchaseItem(231L);

    // when
    boolean result = target.refundIsPossible(inputPurchaseItem);

    // then
    assertTrue(result);
  }

  public void checkRefundSelectResult(List<Refund> givenRefunds, List<Refund> target) {
    assertEquals(givenRefunds.size(), target.size());
    for (int i = 0; i < target.size(); i++) {
      assertEquals(givenRefunds.get(i).getId(), target.get(i).getId());
      assertEquals(givenRefunds.get(i).getState(), target.get(i).getState());
    }
  }
}
