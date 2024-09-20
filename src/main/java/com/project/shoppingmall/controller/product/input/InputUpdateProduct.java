package com.project.shoppingmall.controller.product.input;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class InputUpdateProduct {
  @NotNull private Long productId;
  @NotNull private Long productTypeId;

  @NotBlank
  @Length(min = 1, max = 50)
  private String name;

  @Valid private List<InputProductOption> singleOptions;
  @Valid private List<InputProductOption> multiOptions;
  @Valid private List<InputBlockData> blockDataList;
  @NotNull @Positive private Integer price;
  @NotNull @PositiveOrZero private Integer discountAmount;
  @NotNull @PositiveOrZero private Double discountRate;
}
