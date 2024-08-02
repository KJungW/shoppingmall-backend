package com.project.shoppingmall.controller.product.output;

import com.project.shoppingmall.entity.ProductMultipleOption;
import com.project.shoppingmall.entity.ProductSingleOption;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputProductOption {
  private Long optionId;
  private String optionName;
  private Integer priceChangeAmount;

  public OutputProductOption(ProductSingleOption option) {
    this.optionId = option.getId();
    this.optionName = option.getOptionName();
    this.priceChangeAmount = option.getPriceChangeAmount();
  }

  public OutputProductOption(ProductMultipleOption option) {
    this.optionId = option.getId();
    this.optionName = option.getOptionName();
    this.priceChangeAmount = option.getPriceChangeAmount();
  }
}
