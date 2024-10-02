package com.project.shoppingmall.testdata.product;

import com.project.shoppingmall.entity.ProductMultipleOption;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductMultiOptionBuilder {
  public static ProductMultipleOption.ProductMultipleOptionBuilder fullData() {
    return ProductMultipleOption.builder().optionName("multiOption1").priceChangeAmount(500);
  }

  public static ProductMultipleOption makeProductMultiOption(long id) {
    ProductMultipleOption option =
        ProductMultipleOption.builder()
            .optionName("multiOption" + id)
            .priceChangeAmount(1000)
            .build();
    ReflectionTestUtils.setField(option, "id", id);
    return option;
  }

  public static ProductMultipleOption makeProductMultiOption(long id, int priceChangeAmount) {
    ProductMultipleOption option =
        ProductMultipleOption.builder()
            .optionName("multiOption" + id)
            .priceChangeAmount(priceChangeAmount)
            .build();
    ReflectionTestUtils.setField(option, "id", id);
    return option;
  }
}
