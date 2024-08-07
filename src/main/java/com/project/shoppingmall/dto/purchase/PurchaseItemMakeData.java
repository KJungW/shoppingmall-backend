package com.project.shoppingmall.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PurchaseItemMakeData {
  private long basketItemId;
  private int expectedFinalPrice;
}
