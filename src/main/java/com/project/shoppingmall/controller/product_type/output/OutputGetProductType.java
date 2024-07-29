package com.project.shoppingmall.controller.product_type.output;

import com.project.shoppingmall.entity.ProductType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OutputGetProductType {
  private Long typeId;
  private String typeName;

  public OutputGetProductType(ProductType productType) {
    this.typeId = productType.getId();
    this.typeName = productType.getTypeName();
  }
}
