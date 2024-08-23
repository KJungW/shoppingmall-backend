package com.project.shoppingmall.controller.product_type.input;

import com.project.shoppingmall.final_value.RegularExpressions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InputAddProductType {
  @NotBlank
  @Pattern(regexp = RegularExpressions.PRODUCT_TYPE_PATTERN)
  private String typeName;
}
