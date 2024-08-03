package com.project.shoppingmall.dto.basket;

import com.project.shoppingmall.entity.ProductMultipleOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class MultiOptionsCalcResult {
  boolean isMultiOptionAvailable;
  List<ProductMultipleOption> multiOptions;
  List<Integer> multiOptionPrices;

  public MultiOptionsCalcResult(
      boolean isMultiOptionAvailable, List<ProductMultipleOption> multiOptions) {
    this.isMultiOptionAvailable = isMultiOptionAvailable;
    if (!isMultiOptionAvailable) {
      this.multiOptions = new ArrayList<>();
      this.multiOptionPrices = new ArrayList<>();
      return;
    }
    if (!multiOptions.isEmpty()) {
      this.multiOptions = multiOptions;
      this.multiOptionPrices =
          multiOptions.stream()
              .map(ProductMultipleOption::getPriceChangeAmount)
              .collect(Collectors.toList());
    } else {
      this.multiOptions = new ArrayList<>();
      this.multiOptionPrices = new ArrayList<>();
    }
  }
}
