package com.project.shoppingmall.dto.product;

import com.project.shoppingmall.entity.ProductMultipleOption;
import com.project.shoppingmall.entity.ProductSingleOption;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductOptionDto {
  private Long optionId;
  private String optionName;
  private Integer priceChangeAmount;

  public ProductOptionDto(ProductSingleOption option) {
    this.optionId = option.getId();
    this.optionName = option.getOptionName();
    this.priceChangeAmount = option.getPriceChangeAmount();
  }

  public ProductOptionDto(ProductMultipleOption option) {
    this.optionId = option.getId();
    this.optionName = option.getOptionName();
    this.priceChangeAmount = option.getPriceChangeAmount();
  }
}
