package com.project.shoppingmall.controller.product.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputProductOption {
  @NotBlank
  @Length(min = 1, max = 80)
  private String optionName;

  @NotNull @PositiveOrZero private Integer priceChangeAmount;
}
