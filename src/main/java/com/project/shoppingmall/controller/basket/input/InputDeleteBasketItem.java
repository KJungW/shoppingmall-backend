package com.project.shoppingmall.controller.basket.input;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InputDeleteBasketItem {
  @NotNull List<Long> basketItemIdList;
}
