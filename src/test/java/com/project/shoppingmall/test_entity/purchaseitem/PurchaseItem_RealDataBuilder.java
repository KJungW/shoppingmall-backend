package com.project.shoppingmall.test_entity.purchaseitem;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.PurchaseItem;
import com.project.shoppingmall.test_entity.product.ProductDataForPurchaseBuilder;
import java.util.List;
import java.util.stream.Stream;

public class PurchaseItem_RealDataBuilder {
  public static PurchaseItem make(Product product) {
    ProductDataForPurchase productOptionObj =
        ProductDataForPurchaseBuilder.fullData(product).build();
    return PurchaseItemBuilder.fullData()
        .productData(productOptionObj)
        .finalPrice(product.getFinalPrice())
        .build();
  }

  public static PurchaseItem make(Product product, int finalPrice) {
    ProductDataForPurchase productOptionObj =
        ProductDataForPurchaseBuilder.fullData(product).build();
    return PurchaseItemBuilder.fullData()
        .productData(productOptionObj)
        .finalPrice(finalPrice)
        .build();
  }

  public static List<PurchaseItem> makeList(int count, Product product) {
    return Stream.generate(() -> make(product)).limit(count).toList();
  }
}
