package com.project.shoppingmall.controller.product.input;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
  @NotNull @Positive private Integer price;
  @NotNull @PositiveOrZero private Integer discountAmount;
  @NotNull @PositiveOrZero private Double discountRate;
}
