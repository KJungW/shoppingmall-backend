package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import com.project.shoppingmall.type.RefundStateType;
import java.io.IOException;
import org.springframework.test.util.ReflectionTestUtils;

public class RefundBuilder {
  public static Refund.RefundBuilder fullData() {
    return Refund.builder()
        .refundPrice(10000)
        .requestTitle("TestRefundRequestTitle")
        .requestContent("TestRefundRequestContent");
  }

  public static Refund makeRefund(PurchaseItem purchaseItem) throws IOException {
    Refund refund = RefundBuilder.fullData().build();
    purchaseItem.addRefund(refund);
    return refund;
  }

  public static Refund makeRefund(RefundStateType refundStateType, PurchaseItem purchaseItem)
      throws IOException {
    Refund refund = RefundBuilder.fullData().build();
    purchaseItem.addRefund(refund);
    ReflectionTestUtils.setField(refund, "state", refundStateType);
    return refund;
  }

  public static Refund makeRefundWithPurchaseItem() throws IOException {
    Refund refund = RefundBuilder.fullData().build();
    PurchaseItem purchaseItem = PurchaseItemBuilder.fullData().build();
    purchaseItem.addRefund(refund);
    return refund;
  }
}
