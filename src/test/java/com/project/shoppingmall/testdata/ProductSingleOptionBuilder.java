package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.ProductSingleOption;

public class ProductSingleOptionBuilder {
  public static ProductSingleOption.ProductSingleOptionBuilder fullData() {
    return ProductSingleOption.builder().optionName("singleOption1").priceChangeAmount(1000);
  }
}
