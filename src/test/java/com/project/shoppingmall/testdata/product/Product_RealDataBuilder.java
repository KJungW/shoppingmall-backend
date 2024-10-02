package com.project.shoppingmall.testdata.product;

import com.project.shoppingmall.entity.Member;
import com.project.shoppingmall.entity.Product;
import com.project.shoppingmall.entity.ProductType;

public class Product_RealDataBuilder {
  public static Product makeProduct(Member seller, ProductType type) {
    return ProductBuilder.lightData().seller(seller).productType(type).isBan(false).build();
  }
}
