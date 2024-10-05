package com.project.shoppingmall.test_dto.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.entity.Product;

public class ProductForBasketItemDtoManager {
  public static void check(
      Product product, com.project.shoppingmall.dto.product.ProductForBasketItemDto target) {
    assertEquals(product.getId(), target.getProductId());
    assertEquals(product.getSeller().getId(), target.getSellerId());
    assertEquals(product.getSeller().getNickName(), target.getSellerNickname());
    assertEquals(product.getProductType().getId(), target.getProductTypeId());
    assertEquals(product.getProductType().getTypeName(), target.getProductTypeName());
    assertEquals(
        product.getProductImages().get(0).getDownLoadUrl(), target.getFirstProductImgDownloadUrl());
    assertEquals(product.getName(), target.getName());
    assertEquals(product.getPrice(), target.getPrice());
    assertEquals(product.getDiscountAmount(), target.getDiscountAmount());
    assertEquals(product.getDiscountRate(), target.getDiscountRate());
    assertEquals(product.getIsBan(), target.getIsBan());
    assertEquals(product.getScoreAvg(), target.getScoreAvg());
  }
}
