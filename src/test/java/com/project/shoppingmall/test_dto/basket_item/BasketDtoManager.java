package com.project.shoppingmall.test_dto.basket_item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.project.shoppingmall.dto.basket.BasketDto;
import com.project.shoppingmall.dto.basket.BasketItemDto;
import com.project.shoppingmall.dto.basket.BasketItemPriceCalcResult;
import com.project.shoppingmall.entity.BasketItem;
import java.util.List;

public class BasketDtoManager {
  public static void check(
      List<BasketItem> givenBasketItemList,
      BasketItemPriceCalcResult givenCalcResult,
      BasketDto target) {
    List<BasketItemDto> targetItemDtoList = target.getBasketItemDtos();
    assertEquals(givenBasketItemList.size(), targetItemDtoList.size());
    for (int i = 0; i < target.getBasketItemDtos().size(); i++) {
      BasketItemDtoManager.check(
          givenBasketItemList.get(i), givenCalcResult, targetItemDtoList.get(i));
    }
  }
}
