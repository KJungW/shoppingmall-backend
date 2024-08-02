package com.project.shoppingmall.controller.product.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputProductOption {
  @NotEmpty private String optionName;
  @NotNull @PositiveOrZero private Integer priceChangeAmount;
}
