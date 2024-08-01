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
}
