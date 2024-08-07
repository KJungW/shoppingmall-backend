package com.project.shoppingmall.controller.purchase.output;

import com.project.shoppingmall.dto.delivery.DeliveryDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputReadyPurchase {
  String purchaseUid;
  String purchaseTitle;
  DeliveryDto deliveryInfo;
  Integer totalPrice;
}
