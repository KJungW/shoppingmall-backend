package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.ProductSingleOption;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductSingleOptionBuilder {
  public static ProductSingleOption.ProductSingleOptionBuilder fullData() {
    return ProductSingleOption.builder().optionName("singleOption1").priceChangeAmount(1000);
  }

  public static ProductSingleOption makeProductSingleOption(long id) {
    ProductSingleOption option =
        ProductSingleOption.builder()
            .optionName("singleOption" + id)
            .priceChangeAmount(1000)
            .build();
    ReflectionTestUtils.setField(option, "id", id);
    return option;
  }

  public static ProductSingleOption makeProductSingleOption(long id, int priceChangeAmount) {
    ProductSingleOption option =
        ProductSingleOption.builder()
            .optionName("singleOption" + id)
            .priceChangeAmount(priceChangeAmount)
            .build();
    ReflectionTestUtils.setField(option, "id", id);
    return option;
  }
}
