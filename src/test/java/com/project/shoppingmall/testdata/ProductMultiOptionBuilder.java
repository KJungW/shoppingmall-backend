package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.ProductMultipleOption;

public class ProductMultiOptionBuilder {
  public static ProductMultipleOption.ProductMultipleOptionBuilder fullData() {
    return ProductMultipleOption.builder().optionName("multiOption1").priceChangeAmount(500);
  }
}
