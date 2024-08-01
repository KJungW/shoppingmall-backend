package com.project.shoppingmall.dto.basket;

import com.project.shoppingmall.dto.product.ProductOptionDto;
import com.project.shoppingmall.entity.ProductMultipleOption;
import com.project.shoppingmall.entity.ProductSingleOption;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasketItemPriceCalcResult {
  private Integer price;
  private boolean optionAvailable;
  private ProductOptionDto singleOption;
  private List<ProductOptionDto> multipleOptions = new ArrayList<>();

  public BasketItemPriceCalcResult(Integer price, boolean optionAvailable) {
    this.price = price;
    this.optionAvailable = optionAvailable;
  }

  public BasketItemPriceCalcResult(
      Integer price,
      boolean optionAvailable,
      ProductSingleOption singleOption,
      List<ProductMultipleOption> multipleOptions) {
    this.price = price;
    this.optionAvailable = optionAvailable;
    this.singleOption = new ProductOptionDto(singleOption);
    this.multipleOptions = multipleOptions.stream().map(ProductOptionDto::new).toList();
  }
}
