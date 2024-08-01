package com.project.shoppingmall.util;

import com.project.shoppingmall.exception.ServerLogicError;
import java.util.List;

public class PriceCalculateUtil {
  public static Integer calculatePrice(
      Integer originPrice, Integer discountAmount, Double discountRate) {
    if (originPrice <= 0 || discountAmount < 0 || discountRate < 0) {
      throw new ServerLogicError("잘못된 가격계산 입력값입니다.");
    }
    return originPrice - discountAmount - ((int) (originPrice * discountRate / 100));
  }

  public static Integer addOptionPrice(Integer originPrice, List<Integer> optionPrices) {
    if (originPrice <= 0 || optionPrices == null) {
      throw new ServerLogicError("잘못된 가격계산 입력값입니다.");
    }
    for (Integer optionPrice : optionPrices) {
      originPrice += optionPrice;
    }
    return originPrice;
  }
}
