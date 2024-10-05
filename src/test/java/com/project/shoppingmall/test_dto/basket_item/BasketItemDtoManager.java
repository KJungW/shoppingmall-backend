package com.project.shoppingmall.test_dto.basket_item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.dto.basket.BasketItemDto;
import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.entity.BasketItem;
import com.project.shoppingmall.test_dto.product.ProductForBasketItemDtoManager;
import com.project.shoppingmall.test_dto.product.ProductOptionDtoManager;

public class BasketItemDtoManager {
  public static void check(
      BasketItem givenBasketItem, BasketItemPriceCalcResult givenCalcResult, BasketItemDto target) {
    assertEquals(givenBasketItem.getId(), target.getBasketItemId());
    assertEquals(givenBasketItem.getMember().getId(), target.getMemberId());
    ProductForBasketItemDtoManager.check(givenBasketItem.getProduct(), target.getProduct());
    ProductOptionDtoManager.check(givenCalcResult.getSingleOption(), target.getSingleOption());
    ProductOptionDtoManager.check(givenCalcResult.getMultipleOptions(), target.getMultiOptions());
    assertEquals(givenCalcResult.getPrice(), target.getFinalPrice());
    assertEquals(givenCalcResult.isOptionAvailable(), target.getIsAvailable());
  }
}
