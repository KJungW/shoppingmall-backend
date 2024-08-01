package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.BasketItem;
import lombok.SneakyThrows;

public class BasketItemBuilder {
  @SneakyThrows
  public static BasketItem.BasketItemBuilder fullData() {
    return BasketItem.builder()
        .member(MemberBuilder.fullData().build())
        .product(ProductBuilder.fullData().build())
        .options("testOptionsJson");
  }
}
