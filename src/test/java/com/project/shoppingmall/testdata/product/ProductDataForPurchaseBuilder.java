package com.project.shoppingmall.testdata.product;

import com.project.shoppingmall.dto.purchase.ProductDataForPurchase;
import com.project.shoppingmall.entity.Product;

public class ProductDataForPurchaseBuilder {
  public static ProductDataForPurchase.ProductDataForPurchaseBuilder fullData(Product product) {
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

  public static ProductDataForPurchase makeProductDataForPurchase(Product product) {
    return ProductDataForPurchase.builder()
        .productId(product.getId())
        .sellerId(product.getSeller().getId())
        .sellerName(product.getSeller().getNickName())
        .productName(product.getName())
        .productTypeName(product.getProductType().getTypeName())
        .price(product.getPrice())
        .discountAmount(product.getDiscountAmount())
        .discountRate(product.getDiscountRate())
        .build();
  }
}
