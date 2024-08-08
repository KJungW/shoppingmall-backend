package com.project.shoppingmall.testdata;

import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import java.io.IOException;

public class PurchaseItemBuilder {
  public static PurchaseItem.PurchaseItemBuilder fullData() throws IOException {
    Product givenProduct = ProductBuilder.fullData().build();
    return PurchaseItem.builder()
        .product(givenProduct)
        .productData("TestProductData")
        .finalPrice(10000);
  }
}
