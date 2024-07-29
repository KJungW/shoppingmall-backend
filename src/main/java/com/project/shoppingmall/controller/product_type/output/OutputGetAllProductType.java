package com.project.shoppingmall.controller.product_type.output;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputGetAllProductType {
  private List<OutputGetProductType> productTypeList;
}
