package com.project.shoppingmall.test_entity.product;

import com.project.shoppingmall.entity.ProductMultipleOption;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductMultiOptionBuilder {
  public static ProductMultipleOption.ProductMultipleOptionBuilder fullData() {
    return ProductMultipleOption.builder().optionName("multiOption1").priceChangeAmount(500);
  }

  public static ProductMultipleOption make(long id) {
    ProductMultipleOption option = fullData().build();
    ReflectionTestUtils.setField(option, "id", id);
    return option;
  }

  public static ProductMultipleOption make(long id, int priceChangeAmount) {
    ProductMultipleOption option = fullData().priceChangeAmount(priceChangeAmount).build();
    ReflectionTestUtils.setField(option, "id", id);
    return option;
  }

  public static List<ProductMultipleOption> makeList(List<Long> idList, int priceChangeAmount) {
    return idList.stream().map(id -> make(id, priceChangeAmount)).toList();
  }
}
