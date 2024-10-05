package com.project.shoppingmall.test_entity.product_type;

import com.project.shoppingmall.entity.ProductType;
import org.springframework.test.util.ReflectionTestUtils;

public class ProductTypeBuilder {

  public static ProductType makeProductType(long productId, String productName) {
    ProductType productType = new ProductType(productName);
    ReflectionTestUtils.setField(productType, "id", productId);
    return productType;
  }
}
