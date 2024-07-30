package com.project.shoppingmall.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductOption {
  private String optionName;
  private Integer priceChangeAmount;
}
