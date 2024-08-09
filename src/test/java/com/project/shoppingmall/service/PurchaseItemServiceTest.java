package com.project.shoppingmall.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.repository.PurchaseItemRepository;
import com.project.shoppingmall.testdata.PurchaseItemBuilder;
import com.project.shoppingmall.testdata.RefundBuilder;
import com.project.shoppingmall.type.RefundStateType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
  public void selectRefundByState_ok() throws IOException {
    // given
    // - purchaseItem 인자 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    for (int i = 0; i < 3; i++) {
      Refund givenRefund = RefundBuilder.fullData().build();
      ReflectionTestUtils.setField(givenRefund, "id", (long) i);
      ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.ACCEPT);
      givenPurchaseItem.addRefund(givenRefund);
    }
    for (int i = 4; i < 8; i++) {
      Refund givenRefund = RefundBuilder.fullData().build();
      ReflectionTestUtils.setField(givenRefund, "id", (long) i);
      ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.COMPLETE);
      givenPurchaseItem.addRefund(givenRefund);
    }

    // - state 인자 세팅
    RefundStateType givenState = RefundStateType.ACCEPT;

    // when
    List<Refund> resultRefunds = target.selectRefundByState(givenPurchaseItem, givenState);

    // then
    assertEquals(3, resultRefunds.size());
    List<Long> expectedIdList = new ArrayList<>(Arrays.asList(0L, 1L, 2L));
    List<Long> actualIdList = resultRefunds.stream().map(Refund::getId).toList();
    assertArrayEquals(expectedIdList.toArray(), actualIdList.toArray());
    for (Refund refund : resultRefunds) {
      assertEquals(RefundStateType.ACCEPT, refund.getState());
    }
  }

  @Test
  @DisplayName("selectRefundByState() : 인자 purchaseItem에 환불데이터가 존재하지 않음")
  public void selectRefundByState_EmptyRefund() throws IOException {
    // given
    // - purchaseItem 인자 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();

    // - state 인자 세팅
    RefundStateType givenState = RefundStateType.ACCEPT;

    // when
    List<Refund> resultRefunds = target.selectRefundByState(givenPurchaseItem, givenState);

    // then
    assertEquals(0, resultRefunds.size());
  }

  @Test
  @DisplayName("refundIsPossible() : 환불이 가능한 구매아이템의 경우")
  public void refundIsPossible_ok() throws IOException {
    // given
    // - purchaseItem 인자 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    for (int i = 0; i < 3; i++) {
      Refund givenRefund = RefundBuilder.fullData().build();
      ReflectionTestUtils.setField(givenRefund, "id", (long) i);
      ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.REJECTED);
      givenPurchaseItem.addRefund(givenRefund);
    }

    // when
    boolean result = target.refundIsPossible(givenPurchaseItem);

    // then
    assertTrue(result);
  }

  @Test
  @DisplayName("refundIsPossible() : 이미 완료된 환불이 존재하는 경우")
  public void refundIsPossible_complete() throws IOException {
    // given
    // - purchaseItem 인자 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    for (int i = 0; i < 3; i++) {
      Refund givenRefund = RefundBuilder.fullData().build();
      ReflectionTestUtils.setField(givenRefund, "id", (long) i);
      ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.COMPLETE);
      givenPurchaseItem.addRefund(givenRefund);
    }

    // when
    boolean result = target.refundIsPossible(givenPurchaseItem);

    // then
    assertFalse(result);
  }

  @Test
  @DisplayName("refundIsPossible() : 이미 승인된 환불이 존재하는 경우")
  public void refundIsPossible_accept() throws IOException {
    // given
    // - purchaseItem 인자 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    for (int i = 0; i < 3; i++) {
      Refund givenRefund = RefundBuilder.fullData().build();
      ReflectionTestUtils.setField(givenRefund, "id", (long) i);
      ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.ACCEPT);
      givenPurchaseItem.addRefund(givenRefund);
    }

    // when
    boolean result = target.refundIsPossible(givenPurchaseItem);

    // then
    assertFalse(result);
  }

  @Test
  @DisplayName("refundIsPossible() : 이미 신청중인 환불이 존재하는 경우")
  public void refundIsPossible_request() throws IOException {
    // given
    // - purchaseItem 인자 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    for (int i = 0; i < 3; i++) {
      Refund givenRefund = RefundBuilder.fullData().build();
      ReflectionTestUtils.setField(givenRefund, "id", (long) i);
      ReflectionTestUtils.setField(givenRefund, "state", RefundStateType.REQUEST);
      givenPurchaseItem.addRefund(givenRefund);
    }

    // when
    boolean result = target.refundIsPossible(givenPurchaseItem);

    // then
    assertFalse(result);
  }

  @Test
  @DisplayName("refundIsPossible() : 인자 purchaseItem에 환불데이터가 존재하지 않음")
  public void refundIsPossible_EmptyRefund() throws IOException {
    // given
    // - purchaseItem 인자 세팅
    PurchaseItem givenPurchaseItem = PurchaseItemBuilder.fullData().build();
    ReflectionTestUtils.setField(givenPurchaseItem, "refunds", new ArrayList<>());

    // when
    boolean result = target.refundIsPossible(givenPurchaseItem);

    // then
    assertTrue(result);
  }
}
