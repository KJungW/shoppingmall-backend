package com.project.shoppingmall.controller.purchase.output;

import com.project.shoppingmall.type.PaymentResultType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OutputCompletePurchase {
  private PaymentResultType paymentResult;
}
