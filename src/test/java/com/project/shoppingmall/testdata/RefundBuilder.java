package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Refund;

public class RefundBuilder {
  public static Refund.RefundBuilder fullData() {
    return Refund.builder()
        .refundPrice(10000)
        .requestTitle("TestRefundRequestTitle")
        .requestContent("TestRefundRequestContent");
  }
}
