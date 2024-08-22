package com.project.shoppingmall.testdata;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Product;
import java.io.IOException;

public class ProductDataForPurchaseBuilder {
  public static ProductDataForPurchase.ProductDataForPurchaseBuilder fullData(Product product)
      throws IOException {
    return ProductDataForPurchase.builder()
        .productId(product.getId())
        .sellerId(product.getSeller().getId())
        .sellerName(product.getSeller().getNickName())
        .productName(product.getName())
        .productTypeName(product.getProductType().getTypeName())
        .price(product.getPrice())
        .discountAmount(product.getDiscountAmount())
        .discountRate(product.getDiscountRate());
  }
}
