package com.project.shoppingmall.testdata.basketitem;

import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;

public class BasketItem_RealDataBuilder {
  public static BasketItem makeBasketItem(Member owner, Product product) {
    return BasketItem.builder().member(owner).product(product).build();
  }
}
