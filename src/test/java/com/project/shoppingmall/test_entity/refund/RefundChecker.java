package com.project.shoppingmall.test_entity.refund;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.type.RefundStateType;
import com.project.shoppingmall.type.RefundStateTypeForPurchaseItem;

public class RefundChecker {
  public static void checkRequestSateRefund(
      PurchaseItem purchaseItem, String requestTitle, String requestContent, Refund target) {
    assertEquals(purchaseItem.getId(), target.getPurchaseItem().getId());
    assertEquals(purchaseItem.getFinalPrice(), target.getRefundPrice());
    assertEquals(requestTitle, target.getRequestTitle());
    assertEquals(requestContent, target.getRequestContent());
    assertEquals("", target.getResponseContent());
    assertEquals(RefundStateType.REQUEST, target.getState());

    assertFalse(target.getPurchaseItem().getIsRefund());
    assertFalse(target.getPurchaseItem().getRefunds().isEmpty());
    assertEquals(
        RefundStateTypeForPurchaseItem.REQUEST, target.getPurchaseItem().getFinalRefundState());
    assertNotNull(target.getPurchaseItem().getFinalRefundCreatedDate());
  }

  public static void checkAcceptStateRefund(
      PurchaseItem purchaseItem, String responseTitle, Refund target) {
    assertEquals(purchaseItem.getId(), target.getPurchaseItem().getId());
    assertEquals(purchaseItem.getFinalPrice(), target.getRefundPrice());
    assertEquals(responseTitle, target.getResponseContent());
    assertEquals(RefundStateType.ACCEPT, target.getState());

    assertFalse(target.getPurchaseItem().getIsRefund());
    assertFalse(target.getPurchaseItem().getRefunds().isEmpty());
    assertEquals(
        RefundStateTypeForPurchaseItem.ACCEPT, target.getPurchaseItem().getFinalRefundState());
    assertNotNull(target.getPurchaseItem().getFinalRefundCreatedDate());
  }

  public static void checkRejectedStateRefund(
      PurchaseItem purchaseItem, String responseTitle, Refund target) {
    assertEquals(purchaseItem.getId(), target.getPurchaseItem().getId());
    assertEquals(purchaseItem.getFinalPrice(), target.getRefundPrice());
    assertEquals(responseTitle, target.getResponseContent());
    assertEquals(RefundStateType.REJECTED, target.getState());

    assertFalse(target.getPurchaseItem().getIsRefund());
    assertFalse(target.getPurchaseItem().getRefunds().isEmpty());
    assertEquals(
        RefundStateTypeForPurchaseItem.REJECTED, target.getPurchaseItem().getFinalRefundState());
    assertNotNull(target.getPurchaseItem().getFinalRefundCreatedDate());
  }

  public static void checkCompleteStateRefund(PurchaseItem purchaseItem, Refund target) {
    assertEquals(purchaseItem.getId(), target.getPurchaseItem().getId());
    assertEquals(purchaseItem.getFinalPrice(), target.getRefundPrice());
    assertEquals(RefundStateType.COMPLETE, target.getState());

    assertTrue(target.getPurchaseItem().getIsRefund());
    assertFalse(target.getPurchaseItem().getRefunds().isEmpty());
    assertEquals(
        RefundStateTypeForPurchaseItem.COMPLETE, target.getPurchaseItem().getFinalRefundState());
    assertNotNull(target.getPurchaseItem().getFinalRefundCreatedDate());
  }
}
