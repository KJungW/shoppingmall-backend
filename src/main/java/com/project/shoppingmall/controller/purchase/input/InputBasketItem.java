package com.project.shoppingmall.controller.purchase.input;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputBasketItem {
  @NotNull private Long basketItemId;
  @NotNull private Integer expectedPrice;
}
