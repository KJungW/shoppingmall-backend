package com.project.shoppingmall.test_entity.refund;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.RefundStateType;

public class Refund_RealDataBuilder {
  public static Refund makeRefund(PurchaseItem purchaseItem) {
    Refund refund =
        Refund.builder()
            .refundPrice(purchaseItem.getFinalPrice())
            .requestTitle("TestRefundRequestTitle")
            .requestContent("TestRefundRequestContent")
            .build();
    purchaseItem.addRefund(refund);
    return refund;
  }

  public static Refund makeRefund(RefundStateType refundStateType, PurchaseItem purchaseItem) {
    Refund refund =
        Refund.builder()
            .refundPrice(purchaseItem.getFinalPrice())
            .requestTitle("TestRefundRequestTitle")
            .requestContent("TestRefundRequestContent")
            .build();
    purchaseItem.addRefund(refund);
    switch (refundStateType) {
      case REQUEST -> {}
      case ACCEPT -> refund.acceptRefund("accept");
      case REJECTED -> refund.rejectRefund("reject");
      case COMPLETE -> refund.completeRefund();
      default -> throw new ServerLogicError("처리하지 못한 RefundStateType이 존재합니다.");
    }
    return refund;
  }
}
