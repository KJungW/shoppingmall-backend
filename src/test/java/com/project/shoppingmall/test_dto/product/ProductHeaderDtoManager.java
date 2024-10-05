package com.project.shoppingmall.test_dto.product;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.dto.product.ProductHeaderDto;
import com.project.shoppingmall.entity.Product;
import java.util.List;

public class ProductHeaderDtoManager {

  public static void check(Product product, ProductHeaderDto target) {
    assertEquals(product.getId(), target.getProductId());
    assertEquals(product.getSeller().getId(), target.getSellerId());
    assertEquals(product.getSeller().getNickName(), target.getSellerName());
    assertEquals(product.getProductType().getId(), target.getTypeId());
    assertEquals(product.getProductType().getTypeName(), target.getTypeName());
    assertEquals(
        product.getProductImages().get(0).getDownLoadUrl(), target.getFirstProductImageUrl());
    assertEquals(product.getName(), target.getName());
    assertEquals(product.getPrice(), target.getPrice());
    assertEquals(product.getDiscountAmount(), target.getDiscountAmount());
    assertEquals(product.getDiscountRate(), target.getDiscountRate());
    assertEquals(product.getIsBan(), target.getIsBan());
    assertEquals(product.getScoreAvg(), target.getScoreAvg());
    assertEquals(product.getFinalPrice(), target.getFinalPrice());
    assertEquals(product.getSaleState(), target.getSaleType());
  }

  public static void checkList(List<Product> productList, List<ProductHeaderDto> targetList) {
    assertEquals(productList.size(), targetList.size());
    for (int i = 0; i < targetList.size(); i++) {
      ProductHeaderDtoManager.check(productList.get(i), targetList.get(i));
    }
  }
}
