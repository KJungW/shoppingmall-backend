package com.project.shoppingmall.test_dto.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.dto.product.ProductOptionDto;
import java.util.List;

public class ProductOptionDtoManager {
  public static void check(ProductOptionDto realOption, ProductOptionDto target) {
    assertEquals(realOption.getOptionId(), target.getOptionId());
    assertEquals(realOption.getOptionName(), target.getOptionName());
    assertEquals(realOption.getPriceChangeAmount(), target.getPriceChangeAmount());
  }

  public static void check(List<ProductOptionDto> realOptions, List<ProductOptionDto> target) {
    assertEquals(target.size(), realOptions.size());
    for (int i = 0; i < target.size(); i++) check(realOptions.get(i), target.get(i));
  }
}
