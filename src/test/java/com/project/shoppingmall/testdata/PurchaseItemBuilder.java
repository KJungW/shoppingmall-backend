package com.project.shoppingmall.testdata;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.PurchaseItem;
import java.io.IOException;

public class PurchaseItemBuilder {
  public static PurchaseItem.PurchaseItemBuilder fullData() throws IOException {
    ProductDataForPurchase productData =
        ProductDataForPurchase.builder()
            .productId(10L)
            .sellerId(20L)
            .sellerName("test name")
            .productName("test product")
            .productTypeName("test product type")
            .price(1000)
            .discountAmount(100)
            .discountRate(10d)
            .build();
    return PurchaseItem.builder().productData(productData).finalPrice(10000);
  }
}
