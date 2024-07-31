package com.project.shoppingmall.controller.product.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InputSaveProduct {
  @NotNull private Long productTypeId;
  @NotEmpty private String name;
  private List<InputProductOption> singleOptions;
  private List<InputProductOption> multiOptions;
  private List<InputBlockData> blockDataList;
  @NotNull private Integer price;
  @NotNull private Integer discountAmount;
  @NotNull private Double discountRate;
}
