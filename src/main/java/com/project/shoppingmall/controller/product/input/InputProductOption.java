package com.project.shoppingmall.controller.product.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputProductOption {
  @NotEmpty private String optionName;
  @NotNull private Integer priceChangeAmount;
}
