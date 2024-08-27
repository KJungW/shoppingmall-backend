package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.entity.Refund;
import java.io.IOException;

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

  public static Refund makeRefundWithPurchaseItem() throws IOException {
    Refund refund = RefundBuilder.fullData().build();
    PurchaseItem purchaseItem = PurchaseItemBuilder.fullData().build();
    purchaseItem.addRefund(refund);
    return refund;
  }
}
