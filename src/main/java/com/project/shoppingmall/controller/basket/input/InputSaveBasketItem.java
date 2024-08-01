package com.project.shoppingmall.controller.basket.input;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputSaveBasketItem {
  @NotNull private Long productId;
  private Long singleOptionId;
  private List<Long> multipleOptionId;
}
