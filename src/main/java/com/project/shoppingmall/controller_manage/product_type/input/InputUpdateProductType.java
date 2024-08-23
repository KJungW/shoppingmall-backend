package com.project.shoppingmall.controller_manage.product_type.input;

import com.project.shoppingmall.final_value.RegularExpressions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputUpdateProductType {
  @NotNull private Long productTypeId;

  @NotBlank
  @Pattern(regexp = RegularExpressions.PRODUCT_TYPE_PATTERN)
  private String typeName;
}
