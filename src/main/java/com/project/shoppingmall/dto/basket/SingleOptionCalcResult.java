package com.project.shoppingmall.dto.basket;

import com.project.shoppingmall.entity.ProductSingleOption;
import lombok.Getter;

@Getter
public class SingleOptionCalcResult {
  boolean isSingleOptionAvailable;
  ProductSingleOption singleOption;
  int singleOptionPrice;

  public SingleOptionCalcResult(boolean isSingleOptionAvailable, ProductSingleOption singleOption) {
    this.isSingleOptionAvailable = isSingleOptionAvailable;

    if (!isSingleOptionAvailable) {
      this.singleOption = null;
      this.singleOptionPrice = 0;
      return;
    }

    if (singleOption != null) {
      this.singleOption = singleOption;
      this.singleOptionPrice = singleOption.getPriceChangeAmount();
    } else {
      this.singleOption = null;
      this.singleOptionPrice = 0;
    }
  }
}
