package com.project.shoppingmall.testdata.purchaseitem;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.testdata.product.ProductDataForPurchaseBuilder;

public class PurchaseItem_RealDataBuilder {
  public static PurchaseItem makePurchaseItem(Product product) {
    ProductDataForPurchase productOptionObj =
        ProductDataForPurchaseBuilder.fullData(product).build();
    return PurchaseItemBuilder.fullData()
        .productData(productOptionObj)
        .finalPrice(product.getFinalPrice())
        .build();
  }
}
