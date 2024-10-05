package com.project.shoppingmall.test_entity.product;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductType;
import com.project.shoppingmall.type.ProductSaleType;

public class Product_RealDataBuilder {
  public static Product makeProduct(Member seller, ProductType type) {
    return ProductBuilder.lightData().seller(seller).productType(type).isBan(false).build();
  }

  public static Product makeProduct(Member seller, ProductType type, boolean isBan) {
    return ProductBuilder.lightData().seller(seller).productType(type).isBan(isBan).build();
  }

  public static Product makeProduct(
      Member seller, ProductType type, boolean isBan, ProductSaleType saleType) {
    Product product =
        ProductBuilder.lightData().seller(seller).productType(type).isBan(isBan).build();
    updateSaleTypeInProduct(saleType, product);
    return product;
  }

  public static Product makeProduct(
      String name, Member seller, ProductType type, boolean isBan, ProductSaleType saleType) {
    Product product =
        ProductBuilder.lightData().seller(seller).productType(type).name(name).isBan(isBan).build();
    updateSaleTypeInProduct(saleType, product);
    return product;
  }

  private static void updateSaleTypeInProduct(ProductSaleType saleType, Product product) {
    if (saleType.equals(ProductSaleType.ON_SALE)) product.changeSalesStateToOnSale();
    else if (saleType.equals(ProductSaleType.DISCONTINUED))
      product.changeSalesStateToDiscontinued();
    else new IllegalArgumentException("예상치 못한 ProductSaleType 타입입니다.");
  }
}
