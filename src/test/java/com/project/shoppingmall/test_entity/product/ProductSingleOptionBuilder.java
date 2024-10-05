package com.project.shoppingmall.test_entity.product;

import com.project.shoppingmall.entity.ProductSingleOption;
import java.util.List;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductSingleOptionBuilder {
  public static ProductSingleOption.ProductSingleOptionBuilder fullData() {
    return ProductSingleOption.builder().optionName("singleOption1").priceChangeAmount(1000);
  }

  public static ProductSingleOption make(long id) {
    ProductSingleOption option = fullData().build();
    ReflectionTestUtils.setField(option, "id", id);
    return option;
  }

  public static ProductSingleOption make(long id, int priceChangeAmount) {
    ProductSingleOption option = fullData().priceChangeAmount(priceChangeAmount).build();
    ReflectionTestUtils.setField(option, "id", id);
    return option;
  }

  public static List<ProductSingleOption> makeList(List<Long> idList, int priceChangeAmount) {
    return idList.stream().map(id -> make(id, priceChangeAmount)).toList();
  }
}
