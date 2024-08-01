package com.project.shoppingmall.util;

import static org.junit.jupiter.api.Assertions.*;

import com.project.shoppingmall.exception.ServerLogicError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PriceCalculateUtilTest {
  @Test
  @DisplayName("calculatePrice() : 정상흐름")
  public void calculatePrice_ok() {
    // given
    Integer givenOriginPrice = 10000;
    Integer givenDiscountAmount = 2000;
    Double givenDiscountRate = 10.0;

    // when
    Integer resultPrice =
        PriceCalculateUtil.calculatePrice(givenOriginPrice, givenDiscountAmount, givenDiscountRate);

    // then
    Integer expectedPrice =
        givenOriginPrice
            - givenDiscountAmount
            - ((int) (givenOriginPrice * givenDiscountRate / 100));
    Assertions.assertEquals(expectedPrice, resultPrice);
  }

  @Test
  @DisplayName("calculatePrice() : 기본 가격이 음수일 경우")
  public void calculatePrice_originPriceIsMinus() {
    // given
    Integer givenOriginPrice = -1000;
    Integer givenDiscountAmount = 2000;
    Double givenDiscountRate = 10.0;

    // when
    assertThrows(
        ServerLogicError.class,
        () ->
            PriceCalculateUtil.calculatePrice(
                givenOriginPrice, givenDiscountAmount, givenDiscountRate));
  }

  @Test
  @DisplayName("calculatePrice() : 할인양이 음수일 경우")
  public void calculatePrice_discountAmountIsMinus() {
    // given
    Integer givenOriginPrice = 1000;
    Integer givenDiscountAmount = -2000;
    Double givenDiscountRate = 10.0;

    // when
    assertThrows(
        ServerLogicError.class,
        () ->
            PriceCalculateUtil.calculatePrice(
                givenOriginPrice, givenDiscountAmount, givenDiscountRate));
  }

  @Test
  @DisplayName("calculatePrice() : 할인율이 음수일 경우")
  public void calculatePrice_discountRateIsMinus() {
    // given
    Integer givenOriginPrice = 1000;
    Integer givenDiscountAmount = 2000;
    Double givenDiscountRate = -10.0;

    // when
    assertThrows(
        ServerLogicError.class,
        () ->
            PriceCalculateUtil.calculatePrice(
                givenOriginPrice, givenDiscountAmount, givenDiscountRate));
  }

  @Test
  @DisplayName("addOptionPrice() : 정상흐름")
  public void addOptionPrice_ok() {
    // given
    Integer givenOriginPrice = 1000;
    List<Integer> givenOptionPrices = new ArrayList<>(Arrays.asList(500, -1000, 2000));

    // when
    Integer resultPrice = PriceCalculateUtil.addOptionPrice(givenOriginPrice, givenOptionPrices);

    // then
    for (Integer optionPrice : givenOptionPrices) {
      givenOriginPrice += optionPrice;
    }
    Integer expectedPrice = givenOriginPrice;
    Assertions.assertEquals(expectedPrice, resultPrice);
  }

  @Test
  @DisplayName("addOptionPrice() : 기본 가격이 음수")
  public void addOptionPrice_originPriceIsMinus() {
    // given
    Integer givenOriginPrice = -1000;
    List<Integer> givenOptionPrices = new ArrayList<>(Arrays.asList(500, -1000, 2000));

    // when
    assertThrows(
        ServerLogicError.class,
        () -> PriceCalculateUtil.addOptionPrice(givenOriginPrice, givenOptionPrices));
  }
}
