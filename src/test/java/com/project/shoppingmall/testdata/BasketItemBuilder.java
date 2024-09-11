package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import java.io.IOException;
import lombok.SneakyThrows;

public class BasketItemBuilder {

  @SneakyThrows
  public static BasketItem.BasketItemBuilder fullData() {
    return BasketItem.builder()
        .member(MemberBuilder.fullData().build())
        .product(ProductBuilder.fullData().build())
        .options("testOptionsJson");
  }

  public static BasketItem makeBasketItem(Member owner, Product product) throws IOException {
    return BasketItem.builder().member(owner).product(product).build();
  }
}
