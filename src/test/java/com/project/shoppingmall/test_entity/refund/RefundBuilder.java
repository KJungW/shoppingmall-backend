package com.project.shoppingmall.test_entity.refund;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.exception.ServerLogicError;
import com.project.shoppingmall.type.RefundStateType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class RefundBuilder {
  public static Refund.RefundBuilder fullData() {
    return Refund.builder()
        .refundPrice(10000)
        .requestTitle("TestRefundRequestTitle")
        .requestContent("TestRefundRequestContent");
  }

  public static Refund make(long id, PurchaseItem purchaseItem) {
    Refund refund = fullData().refundPrice(purchaseItem.getFinalPrice()).build();
    purchaseItem.addRefund(refund);
    ReflectionTestUtils.setField(refund, "id", id);
    return refund;
  }

  public static Refund make(long id, RefundStateType refundStateType, PurchaseItem purchaseItem) {
    Refund refund = fullData().refundPrice(purchaseItem.getFinalPrice()).build();
    purchaseItem.addRefund(refund);
    ReflectionTestUtils.setField(refund, "id", id);
    switch (refundStateType) {
      case REQUEST -> {}
      case ACCEPT -> refund.acceptRefund("accept");
      case REJECTED -> refund.rejectRefund("reject");
      case COMPLETE -> refund.completeRefund();
      default -> throw new ServerLogicError("처리하지 못한 RefundStateType이 존재합니다.");
    }
    return refund;
  }

  public static List<Refund> makeList(List<Long> idList, PurchaseItem givenPurchaseItem) {
    ArrayList<Refund> refunds = new ArrayList<>();
    idList.forEach(id -> refunds.add(RefundBuilder.make(id, givenPurchaseItem)));
    return refunds;
  }

  public static List<Refund> makeList(
      List<Long> idList, RefundStateType refundStateType, PurchaseItem givenPurchaseItem) {
    ArrayList<Refund> refunds = new ArrayList<>();
    idList.forEach(id -> refunds.add(RefundBuilder.make(id, refundStateType, givenPurchaseItem)));
    return refunds;
  }
}
