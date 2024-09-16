package com.project.shoppingmall.testdata;

import com.project.shoppingmall.dto.basket.BasketItemMakeData;
import java.util.ArrayList;
import java.util.List;

public class BasketItemMakeDataBuilder {
  public static BasketItemMakeData.BasketItemMakeDataBuilder fullData() {
    Long givenMemberId = 4L;
    Long givenProductId = 23L;
    Long givenSingleOption = 4L;
    List<Long> givenMultipleOptionId = new ArrayList<>();
    givenMultipleOptionId.add(15L);
    givenMultipleOptionId.add(16L);
    givenMultipleOptionId.add(36L);

    return BasketItemMakeData.builder()
        .memberId(givenMemberId)
        .productId(givenProductId)
        .singleOptionId(givenSingleOption)
        .multipleOptionId(givenMultipleOptionId);
  }

  public static BasketItemMakeData makeBasketItem(
      long givenOwnerId, long givenProductId, long singleOptionId, List<Long> multiOptionIds) {
    return BasketItemMakeData.builder()
        .memberId(givenOwnerId)
        .productId(givenProductId)
        .singleOptionId(singleOptionId)
        .multipleOptionId(multiOptionIds)
        .build();
  }

  public static BasketItemMakeData makeBasketItem(long givenOwnerId, long givenProductId) {
    return BasketItemMakeData.builder().memberId(givenOwnerId).productId(givenProductId).build();
  }
}
